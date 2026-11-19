"use strict";

const { createHash } = require("node:crypto");

class RequestError extends Error {
  constructor(status, code, message) { super(message); this.status = status; this.code = code; }
}

const unavailable = () => new RequestError(404, "NOT_FOUND", "No se pudo enviar la solicitud a esta cuenta.");
const permissionFailure = (error) =>
  [7, "PERMISSION_DENIED", "permission-denied", "auth/insufficient-permission"].includes(error.code) ||
  error.status === 403 || error.httpErrorCode?.status === 403;
const normalizeEmail = (value) => value.trim().toLowerCase();
function validEmail(value) {
  const email = normalizeEmail(value);
  const parts = email.split("@");
  if (email.length > 254 || parts.length !== 2) return false;
  const [local, domain] = parts;
  if (!local.length || local.length > 64 || local.startsWith(".") || local.endsWith(".") || local.includes("..") ||
      !/^[a-z0-9!#$%&'*+/=?^_`{|}~.-]+$/.test(local)) return false;
  const labels = domain.split(".");
  return labels.length >= 2 && labels.at(-1).length >= 2 && /[a-z]/.test(labels.at(-1)) &&
    labels.every((label) => label.length >= 1 && label.length <= 63 &&
      /^[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$/.test(label));
}
const requestId = (a, b) => createHash("sha256").update(a, "utf8").digest("hex") +
  createHash("sha256").update(b, "utf8").digest("hex");
const normalizeName = (value) => value.normalize("NFD").replace(/\p{M}/gu, "").toLowerCase().trim()
  .replace(/[\s\p{Z}]+/gu, " ");

function projection(uid, data) {
  if (!data || data.isDeleted === true || typeof data.name !== "string" || !data.name.trim() ||
      data.name.length > 120 || !["USER", "BUSINESS"].includes(data.accountType)) return null;
  return {
    uid,
    name: data.name,
    nameNormalized: normalizeName(data.name),
    photoUrl: typeof data.photoUrl === "string" && /^https:\/\//.test(data.photoUrl) && data.photoUrl.length <= 2048 ? data.photoUrl : "",
    accountType: data.accountType,
    isDiscoverable: data.isDiscoverable === true,
    isDeleted: false
  };
}

async function consumeRateLimit(db, uid, time) {
  const reference = db.collection("friendRequestRateLimits").doc(uid);
  await db.runTransaction(async (transaction) => {
    const old = (await transaction.get(reference)).data() || {};
    const minute = Math.floor(time / 60000);
    const day = Math.floor(time / 86400000);
    const minuteCount = old.minute === minute ? Number(old.minuteCount) || 0 : 0;
    const dayCount = old.day === day ? Number(old.dayCount) || 0 : 0;
    if (minuteCount >= 5 || dayCount >= 20) {
      throw new RequestError(429, "RESOURCE_EXHAUSTED", "Demasiados intentos. Espera antes de volver a enviar solicitudes.");
    }
    transaction.set(reference, { minute, minuteCount: minuteCount + 1, day, dayCount: dayCount + 1 });
  });
}

function createSendFriendRequestHandler({ auth, db, now = Date.now }) {
  return async (req, res) => {
    res.set("Cache-Control", "no-store");
    res.set("X-Content-Type-Options", "nosniff");
    try {
      if (req.method !== "POST") throw new RequestError(405, "METHOD_NOT_ALLOWED", "Utiliza POST");
      const match = /^Bearer\s+(.+)$/i.exec(req.get("Authorization") || "");
      if (!match) throw new RequestError(401, "UNAUTHENTICATED", "Debes iniciar sesión");
      let identity;
      try {
        identity = await auth.verifyIdToken(match[1], true);
      } catch (error) {
        if (permissionFailure(error)) throw error;
        if (["auth/argument-error", "auth/invalid-id-token", "auth/id-token-expired",
             "auth/id-token-revoked", "auth/user-disabled", "auth/user-not-found"].includes(error.code)) {
          throw new RequestError(401, "UNAUTHENTICATED", "La sesión no es válida. Inicia sesión de nuevo.");
        }
        throw error;
      }
      if (!identity.uid) throw new RequestError(401, "UNAUTHENTICATED", "Sesión no válida");
      if (!req.is("application/json") || !req.body || typeof req.body.email !== "string" ||
          Object.keys(req.body).some((key) => key !== "email") || !validEmail(req.body.email)) {
        throw new RequestError(400, "INVALID_ARGUMENT", "Introduce un correo electrónico válido");
      }
      const email = normalizeEmail(req.body.email);
      await consumeRateLimit(db, identity.uid, now());
      const senderRef = db.collection("users").doc(identity.uid);
      if (!projection(identity.uid, (await senderRef.get()).data())) {
        throw new RequestError(412, "FAILED_PRECONDITION", "Completa tu perfil de GuateVivo antes de enviar solicitudes");
      }
      let receiver;
      try {
        receiver = await auth.getUserByEmail(email);
      } catch (error) {
        if (error.code === "auth/user-not-found") throw unavailable();
        throw error;
      }
      if (receiver.disabled) throw unavailable();
      if (receiver.uid === identity.uid) {
        throw new RequestError(400, "INVALID_ARGUMENT", "No puedes enviarte una solicitud a ti mismo");
      }

      const receiverRef = db.collection("users").doc(receiver.uid);
      const forward = db.collection("friendRequests").doc(requestId(identity.uid, receiver.uid));
      const reverse = db.collection("friendRequests").doc(requestId(receiver.uid, identity.uid));
      const outcome = await db.runTransaction(async (transaction) => {
        const [senderDoc, receiverDoc, first, second] = await Promise.all(
          [senderRef, receiverRef, forward, reverse].map((reference) => transaction.get(reference))
        );
        const senderProfile = projection(identity.uid, senderDoc.data());
        const receiverProfile = projection(receiver.uid, receiverDoc.data());
        if (!senderProfile) throw new RequestError(412, "FAILED_PRECONDITION", "Completa tu perfil de GuateVivo antes de enviar solicitudes");
        if (!receiverProfile) throw unavailable();
        if (senderProfile.accountType !== "USER" || receiverProfile.accountType !== "USER") {
          throw new RequestError(403, "PERMISSION_DENIED", "Solo las cuentas USER pueden usar solicitudes de amistad");
        }
        const records = [first.data(), second.data()].filter(Boolean);
        for (const record of records) {
          if (!["PENDING", "ACCEPTED", "REJECTED"].includes(record.status) ||
              !((record.senderUid === identity.uid && record.receiverUid === receiver.uid) ||
                (record.senderUid === receiver.uid && record.receiverUid === identity.uid))) {
            throw new RequestError(412, "FAILED_PRECONDITION", "Los registros de amistad necesitan revisión administrativa");
          }
        }
        for (const [reference, data, profile] of [
          [senderRef, senderDoc.data(), senderProfile],
          [receiverRef, receiverDoc.data(), receiverProfile]
        ]) {
          const patch = {};
          for (const key of ["uid", "nameNormalized", "photoUrl", "isDiscoverable", "isDeleted"]) {
            if (data[key] !== profile[key]) patch[key] = profile[key];
          }
          if (Object.keys(patch).length) transaction.update(reference, patch);
          transaction.set(db.collection("publicProfiles").doc(profile.uid), profile);
        }
        if (records.some((record) => record.status === "ACCEPTED")) return "FRIENDS";
        if (records.some((record) => record.status === "PENDING")) return "PENDING";
        transaction.set(forward, {
          id: forward.id,
          senderUid: identity.uid,
          receiverUid: receiver.uid,
          senderName: "",
          status: "PENDING",
          updatedAt: now()
        });
        return "SENT";
      });
      return res.status(200).json({ result: { outcome } });
    } catch (error) {
      if (error instanceof RequestError) {
        return res.status(error.status).json({ error: { status: error.code, message: error.message } });
      }
      if (permissionFailure(error)) {
        return res.status(403).json({
          error: { status: "PERMISSION_DENIED", message: "Firebase denegó la operación. Revisa los permisos del backend." }
        });
      }
      return res.status(500).json({ error: { status: "INTERNAL", message: "No se pudo completar la solicitud. Inténtalo de nuevo." } });
    }
  };
}

module.exports = { createSendFriendRequestHandler, normalizeEmail, validEmail, requestId, projection };
