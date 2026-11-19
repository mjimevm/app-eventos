"use strict";
const test = require("node:test");
const assert = require("node:assert/strict");
const { createSendFriendRequestHandler, normalizeEmail, validEmail, requestId } = require("../../../../firebase/send-friend-request-email.cjs");
const copy = (value) => value === undefined ? undefined : structuredClone(value);

class FakeFirestore {
  constructor() { this.rows = new Map(); this.tail = Promise.resolve(); this.denyWrites = null; }
  collection(name) {
    return { doc: (id) => ({ id, path: `${name}/${id}`, get: async () => this.snapshot(`${name}/${id}`) }) };
  }
  snapshot(path) { const value = copy(this.rows.get(path)); return { exists: value !== undefined, data: () => copy(value) }; }
  runTransaction(operation) {
    const pending = this.tail.then(async () => {
      const changes = [];
      const write = (reference, data, update) => {
        if (reference.path.startsWith(this.denyWrites + "/")) throw Object.assign(new Error("Denied"), { code: 7 });
        changes.push({ reference, data: copy(data), update });
      };
      const value = await operation({
        get: async (reference) => this.snapshot(reference.path),
        set: (reference, data) => write(reference, data, false),
        update: (reference, data) => write(reference, data, true)
      });
      for (const change of changes) this.rows.set(change.reference.path,
        change.update ? { ...this.rows.get(change.reference.path), ...change.data } : change.data);
      return value;
    });
    this.tail = pending.catch(() => {});
    return pending;
  }
  requests() { return [...this.rows.entries()].filter(([key]) => key.startsWith("friendRequests/")); }
}

function fixture() {
  const db = new FakeFirestore();
  db.rows.set("users/me", { name: "Yo", accountType: "USER", email: "me@test.com", phone: "private-phone" });
  db.rows.set("users/other", { name: "Ana", accountType: "USER", email: "ana@test.com", isDiscoverable: false });
  const accounts = new Map([["me@test.com", { uid: "me" }], ["ana@test.com", { uid: "other" }]]);
  const lookups = [], verifications = [];
  const auth = {
    verifyIdToken: async (token, checkRevoked) => {
      verifications.push({ token, checkRevoked });
      if (!["me", "other"].includes(token)) throw Object.assign(new Error("Invalid token"), { code: "auth/invalid-id-token" });
      return { uid: token };
    },
    getUserByEmail: async (email) => {
      lookups.push(email);
      if (!accounts.has(email)) throw Object.assign(new Error("Missing"), { code: "auth/user-not-found" });
      return accounts.get(email);
    }
  };
  let time = Date.UTC(2026, 10, 19, 12);
  const handler = createSendFriendRequestHandler({ auth, db, now: () => time });
  const invoke = async (email, options = {}) => {
    const response = { headers: {}, statusCode: 200, set(key, value) { this.headers[key] = value; },
      status(value) { this.statusCode = value; return this; }, json(body) { this.body = body; return this; } };
    await handler({ method: options.method || "POST", body: options.body || { email },
      get: () => options.noToken ? undefined : `Bearer ${options.token || "me"}`,
      is: () => options.json !== false }, response);
    return response;
  };
  return { db, accounts, auth, lookups, verifications, invoke, advance: (ms) => { time += ms; } };
}

test("unauthenticated or invalid tokens never resolve an email", async () => {
  const f = fixture();
  assert.equal((await f.invoke("ana@test.com", { noToken: true })).statusCode, 401);
  assert.equal((await f.invoke("ana@test.com", { token: "invalid" })).statusCode, 401);
  assert.equal(f.lookups.length, 0);
  assert.equal(f.db.requests().length, 0);
});

test("email validation trims and lowercases without removing internal spaces, dots or tags", async () => {
  assert.equal(normalizeEmail(" I.Name+TAG@Test.COM "), "i.name+tag@test.com");
  assert.equal(validEmail("a @test.com"), false);
  assert.equal(validEmail("a@@test.com"), false);
  assert.equal(validEmail("a..b@test.com"), false);
  const f = fixture();
  assert.equal((await f.invoke("invalid")).statusCode, 400);
  assert.equal(f.lookups.length, 0);
});

test("registered hidden accounts can receive requests; response and projections omit private contacts", async () => {
  const f = fixture();
  const response = await f.invoke("  ANA@Test.COM  ");
  assert.deepEqual(response.body, { result: { outcome: "SENT" } });
  assert.deepEqual(f.lookups, ["ana@test.com"]);
  assert.equal(f.verifications[0].checkRevoked, true);
  assert.equal(response.headers["Cache-Control"], "no-store");
  assert.equal(f.db.requests().length, 1);
  assert.equal(f.db.rows.get(`friendRequests/${requestId("me", "other")}`).status, "PENDING");
  assert.equal(f.db.rows.get("users/me").phone, "private-phone");
  const projected = JSON.stringify(f.db.rows.get("publicProfiles/other"));
  assert.equal(projected.includes("ana@test.com"), false);
  assert.equal(f.db.rows.get("publicProfiles/other").isDiscoverable, false);
  assert.equal(JSON.stringify(response.body).includes("uid"), false);
});

test("retries and crossed requests return PENDING without creating duplicates", async () => {
  const f = fixture();
  assert.equal((await f.invoke("ana@test.com")).body.result.outcome, "SENT");
  assert.equal((await f.invoke("ana@test.com")).body.result.outcome, "PENDING");
  assert.equal((await f.invoke("me@test.com", { token: "other" })).body.result.outcome, "PENDING");
  assert.equal(f.db.requests().length, 1);
});

test("accepted friendships return FRIENDS and rejected requests can be resent with the same ID", async () => {
  const f = fixture();
  const id = requestId("me", "other");
  f.db.rows.set(`friendRequests/${id}`, { id, senderUid: "me", receiverUid: "other", status: "ACCEPTED" });
  assert.equal((await f.invoke("ana@test.com")).body.result.outcome, "FRIENDS");
  f.db.rows.get(`friendRequests/${id}`).status = "REJECTED";
  assert.equal((await f.invoke("ana@test.com")).body.result.outcome, "SENT");
  assert.equal(f.db.requests().length, 1);
});

test("requests to oneself are rejected using the resolved UID", async () => {
  const f = fixture();
  assert.equal((await f.invoke(" ME@Test.COM ")).statusCode, 400);
  assert.equal(f.db.requests().length, 0);
});

test("unknown, disabled, deleted or missing profiles have the same unavailable response", async () => {
  const expected = { error: { status: "NOT_FOUND", message: "No se pudo enviar la solicitud a esta cuenta." } };
  const unknown = fixture();
  assert.deepEqual((await unknown.invoke("unknown@test.com")).body, expected);
  const disabled = fixture();
  disabled.accounts.get("ana@test.com").disabled = true;
  assert.deepEqual((await disabled.invoke("ana@test.com")).body, expected);
  const deleted = fixture();
  deleted.db.rows.get("users/other").isDeleted = true;
  assert.deepEqual((await deleted.invoke("ana@test.com")).body, expected);
  const missing = fixture();
  missing.db.rows.delete("users/other");
  assert.deepEqual((await missing.invoke("ana@test.com")).body, expected);
});

test("unregistered senders cannot use the endpoint to look up other accounts", async () => {
  const f = fixture();
  f.db.rows.delete("users/me");
  assert.equal((await f.invoke("ana@test.com")).statusCode, 412);
  assert.equal(f.lookups.length, 0);
});

test("PERMISSION_DENIED is an error and the request transaction rolls back", async () => {
  const f = fixture();
  f.db.denyWrites = "friendRequests";
  const response = await f.invoke("ana@test.com");
  assert.equal(response.statusCode, 403);
  assert.equal(response.body.error.status, "PERMISSION_DENIED");
  assert.equal(response.body.result, undefined);
  assert.equal(f.db.requests().length, 0);
  assert.equal(f.db.rows.has("publicProfiles/other"), false);
  assert.equal(f.db.rows.get("users/me").nameNormalized, undefined);
});

test("backend Auth permission failures are not mistaken for an unknown email or success", async () => {
  const f = fixture();
  f.auth.getUserByEmail = async () => { throw Object.assign(new Error("Denied"), { code: "auth/insufficient-permission" }); };
  const response = await f.invoke("ana@test.com");
  assert.equal(response.statusCode, 403);
  assert.equal(response.body.error.status, "PERMISSION_DENIED");
});

test("token verification permission failures keep PERMISSION_DENIED", async () => {
  const f = fixture();
  f.auth.verifyIdToken = async () => { throw Object.assign(new Error("Denied"), { code: 7 }); };
  const response = await f.invoke("ana@test.com");
  assert.equal(response.statusCode, 403);
  assert.equal(response.body.error.status, "PERMISSION_DENIED");
  assert.equal(f.lookups.length, 0);
});

test("unknown addresses consume rate limits and do not provide an unlimited enumeration endpoint", async () => {
  const f = fixture();
  for (let n = 0; n < 5; n++) assert.equal((await f.invoke(`unknown${n}@test.com`)).statusCode, 404);
  const response = await f.invoke("ana@test.com");
  assert.equal(response.statusCode, 429);
  assert.equal(response.body.error.status, "RESOURCE_EXHAUSTED");
  assert.equal(f.lookups.length, 5);
  assert.equal(JSON.stringify(f.db.rows.get("friendRequestRateLimits/me")).includes("@"), false);
});

test("daily attempt quota also applies across minute windows", async () => {
  const f = fixture();
  for (let n = 0; n < 20; n++) { await f.invoke("ana@test.com"); f.advance(61000); }
  assert.equal((await f.invoke("ana@test.com")).statusCode, 429);
});

test("spoofed sender UID, non-JSON bodies and lookup GET requests are rejected", async () => {
  const f = fixture();
  assert.equal((await f.invoke(null, { body: { email: "ana@test.com", senderUid: "other" } })).statusCode, 400);
  assert.equal((await f.invoke("ana@test.com", { json: false })).statusCode, 400);
  assert.equal((await f.invoke("ana@test.com", { method: "GET" })).statusCode, 405);
  assert.equal(f.lookups.length, 0);
});

test("concurrent opposing submissions produce only one shared PENDING relationship", async () => {
  const f = fixture();
  const results = await Promise.all([f.invoke("ana@test.com"), f.invoke("me@test.com", { token: "other" })]);
  assert.deepEqual(new Set(results.map((r) => r.body.result.outcome)), new Set(["SENT", "PENDING"]));
  assert.equal(f.db.requests().length, 1);
});
