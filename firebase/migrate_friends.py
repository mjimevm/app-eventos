"""Admin-only, paginated migration. Dry-run by default; never called by the Android app."""
import argparse
import hashlib
import re
import time
import unicodedata


def normalize_name(value):
    text = "".join(c for c in unicodedata.normalize("NFD", value) if not unicodedata.combining(c))
    return re.sub(r"\s+", " ", text.lower().strip())


def request_id(sender, receiver):
    return hashlib.sha256(sender.encode("utf-8")).hexdigest() + hashlib.sha256(receiver.encode("utf-8")).hexdigest()


def pages(collection):
    query = collection.order_by("__name__").limit(200)
    while True:
        page = list(query.stream())
        yield from page
        if len(page) < 200:
            return
        query = collection.order_by("__name__").start_after(page[-1]).limit(200)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--project", required=True)
    parser.add_argument("--apply", action="store_true", help="Write to the explicitly named project")
    args = parser.parse_args()
    import firebase_admin
    from firebase_admin import auth, firestore
    firebase_admin.initialize_app(options={"projectId": args.project})
    db = firestore.client()
    marker = db.collection("_migrations").document("friends_v1")
    import_legacy_mirrors = not marker.get().exists
    active = set()
    users = []
    counters = {"profiles": 0, "deleted_or_missing_auth": 0, "relationships": 0, "obsolete_requests": 0, "invalid_names": 0}
    for doc in pages(db.collection("users")):
        data = doc.to_dict()
        try:
            auth.get_user(doc.id)
            alive = data.get("isDeleted", False) is not True
        except auth.UserNotFoundError:
            alive = False
        directory = db.collection("publicProfiles").document(doc.id)
        if not alive:
            counters["deleted_or_missing_auth"] += 1
            if args.apply:
                batch = db.batch()
                batch.update(doc.reference, {"isDeleted": True, "isDiscoverable": False})
                batch.delete(directory)
                batch.commit()
            continue
        name = data.get("name", "")
        if not isinstance(name, str) or not name.strip() or len(name) > 120:
            counters["invalid_names"] += 1
            if args.apply:
                directory.delete()
            continue
        photo = data.get("photoUrl", "")
        if not isinstance(photo, str) or not photo.startswith("https://") or len(photo) > 2048:
            photo = ""
        profile = {"uid": doc.id, "name": name, "nameNormalized": normalize_name(name), "photoUrl": photo,
                   "accountType": data.get("accountType", "USER"),
                   "isDiscoverable": data.get("isDiscoverable") is True, "isDeleted": False}
        if args.apply:
            batch = db.batch()
            batch.update(doc.reference, {k: profile[k] for k in ("uid", "nameNormalized", "photoUrl", "isDiscoverable", "isDeleted")})
            batch.set(directory, profile)
            batch.commit()
        active.add(doc.id)
        users.append(doc)
        counters["profiles"] += 1

    if args.apply and counters["invalid_names"]:
        print({"project": args.project, **counters})
        raise SystemExit("Repair invalid names before migrating relationships. Profile reconciliation was partially applied; no relationship or completion marker was written.")

    grouped = {}
    for doc in pages(db.collection("friendRequests")):
        data = doc.to_dict()
        a, b = data.get("senderUid"), data.get("receiverUid")
        if a in active and b in active and a != b and data.get("status") in ("PENDING", "ACCEPTED", "REJECTED"):
            grouped.setdefault(tuple(sorted((a, b))), []).append((doc, data))

    # Only reciprocal legacy mirrors become accepted friendships; one-sided records are not trusted.
    mirrors = set()
    if import_legacy_mirrors:
        for user in users:
            for friend in pages(user.reference.collection("friends")):
                if friend.id in active and friend.id != user.id:
                    mirrors.add((user.id, friend.id))
    for a, b in sorted(mirrors):
        if (b, a) in mirrors:
            pair = tuple(sorted((a, b)))
            grouped.setdefault(pair, [])

    priority = {"ACCEPTED": 0, "PENDING": 1, "REJECTED": 2}
    for pair, records in grouped.items():
        records.sort(key=lambda item: (priority[item[1]["status"]], item[1]["senderUid"], item[0].id))
        mirrored = (pair[0], pair[1]) in mirrors and (pair[1], pair[0]) in mirrors
        if records:
            chosen = dict(records[0][1])
        else:
            chosen = {"senderUid": pair[0], "receiverUid": pair[1], "status": "ACCEPTED"}
        if mirrored:
            chosen["status"] = "ACCEPTED"
        identifier = request_id(chosen["senderUid"], chosen["receiverUid"])
        value = {"id": identifier, "senderUid": chosen["senderUid"], "receiverUid": chosen["receiverUid"],
                 "senderName": "", "status": chosen["status"], "updatedAt": chosen.get("updatedAt", 0)}
        if not isinstance(value["updatedAt"], int):
            value["updatedAt"] = int(time.time() * 1000)
        obsolete = [doc for doc, _ in records if doc.id != identifier]
        if args.apply:
            db.collection("friendRequests").document(identifier).set(value)
            for start in range(0, len(obsolete), 400):
                batch = db.batch()
                for old in obsolete[start:start + 400]:
                    batch.delete(old.reference)
                batch.commit()
        counters["relationships"] += 1
        counters["obsolete_requests"] += len(obsolete)
    if args.apply:
        marker.set({"legacyMirrorsImported": True, "completedAt": firestore.SERVER_TIMESTAMP})
    print({"project": args.project, "applied": args.apply, "include_legacy_mirrors": import_legacy_mirrors, **counters})


if __name__ == "__main__":
    main()
