// Integrate these exports into a trusted Cloud Functions backend; not deployed by this repository.
const admin = require("firebase-admin");
const functions = require("firebase-functions/v1");
const { onDocumentWritten } = require("firebase-functions/v2/firestore");
const { onRequest } = require("firebase-functions/v2/https");
const { createSendFriendRequestHandler } = require("./send-friend-request-email.cjs");
if (admin.apps.length === 0) admin.initializeApp();

// HTTP is reachable by Android; this handler independently verifies every Firebase ID token.
exports.sendFriendRequestByEmail = onRequest(
  { region: "us-central1", invoker: "public", cors: false, timeoutSeconds: 30 },
  createSendFriendRequestHandler({ auth: admin.auth(), db: admin.firestore() })
);

const normalizeName = (value) => value.normalize("NFD").replace(/\p{M}/gu, "")
  .toLowerCase().trim().replace(/[\s\p{Z}]+/gu, " ");

// Transactions read the current source, rather than a possibly late/out-of-order event snapshot.
exports.syncFriendProfile = onDocumentWritten("users/{uid}", async (event) => {
  const db = admin.firestore();
  const source = db.collection("users").doc(event.params.uid);
  const target = db.collection("publicProfiles").doc(event.params.uid);
  await db.runTransaction(async (transaction) => {
    const snapshot = await transaction.get(source);
    const user = snapshot.data();
    if (!user || user.isDeleted === true || typeof user.name !== "string" ||
        !user.name.trim() || user.name.length > 120 || !["USER", "BUSINESS"].includes(user.accountType)) {
      transaction.delete(target);
      return;
    }
    const profile = {
      uid: snapshot.id, name: user.name, nameNormalized: normalizeName(user.name),
      photoUrl: typeof user.photoUrl === "string" && /^https:\/\//.test(user.photoUrl) && user.photoUrl.length <= 2048 ? user.photoUrl : "",
      accountType: user.accountType, isDiscoverable: user.isDiscoverable === true, isDeleted: false
    };
    const patch = {};
    for (const key of ["nameNormalized", "photoUrl", "isDiscoverable", "isDeleted"]) {
      if (user[key] !== profile[key]) patch[key] = profile[key];
    }
    if (Object.keys(patch).length) transaction.update(source, patch);
    transaction.set(target, profile);
  });
});

// Authentication deletions do not automatically delete Firestore documents.
exports.hideDeletedAuthProfile = functions.auth.user().onDelete(async (user) => {
  const db = admin.firestore();
  const source = db.collection("users").doc(user.uid);
  const target = db.collection("publicProfiles").doc(user.uid);
  await db.runTransaction(async (transaction) => {
    const snapshot = await transaction.get(source);
    if (snapshot.exists) transaction.update(source, { isDeleted: true, isDiscoverable: false });
    transaction.delete(target);
  });
});
