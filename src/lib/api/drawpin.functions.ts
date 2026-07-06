import { createServerFn } from "@tanstack/react-start";
import { z } from "zod";

import type { OrderStatus, Role } from "@/lib/backend/types";

async function backend() {
  return import("@/lib/backend/store.server");
}

const tokenSchema = z.object({ token: z.string().min(20).optional() });
const pagingSchema = z.object({
  limit: z.number().int().min(1).max(60).default(24),
  cursor: z.string().optional(),
});

const roles: Role[] = ["collector", "creator", "moderator", "admin"];
const orderStatuses: OrderStatus[] = ["draft", "in_progress", "delivered", "in_review", "completed", "cancelled"];

export const register = createServerFn({ method: "POST" })
  .inputValidator(
    z.object({
      name: z.string().min(2).max(80),
      email: z.string().email(),
      password: z.string().min(8).max(128),
      role: z.enum(roles as [Role, ...Role[]]).default("collector"),
      city: z.string().max(80).optional(),
      specialties: z.array(z.string().min(1).max(40)).max(6).default([]),
    }),
  )
  .handler(async ({ data }) => {
    const { createSession, createUser, fail, mutateDb, newId, ok, publicUser } = await backend();

    return mutateDb(async (db) => {
      const email = data.email.toLowerCase();
      if (db.users.some((user) => user.email === email)) {
        return fail("EMAIL_TAKEN", "An account already exists for this email.");
      }

      const user = createUser({ ...data, email });
      db.users.push(user);

      if (user.role === "creator") {
        db.creators.push({
          id: newId("creator"),
          userId: user.id,
          seed: user.avatarSeed,
          name: user.name,
          handle: user.handle,
          city: user.city,
          lat: 19.07,
          lng: 72.87,
          followers: 0,
          rating: 0,
          reviews: 0,
          level: "New",
          rate: 75,
          bio: user.bio || "Creator accepting new commissions.",
          specialties: user.specialties,
        });
      }

      const session = await createSession(user.id);
      return ok({ user: publicUser(user), token: session.token, expiresAt: session.expiresAt });
    });
  });

export const login = createServerFn({ method: "POST" })
  .inputValidator(z.object({ email: z.string().email(), password: z.string().min(1) }))
  .handler(async ({ data }) => {
    const { createSession, fail, getDb, ok, publicUser, verifyPassword } = await backend();
    const db = await getDb();
    const user = db.users.find((item) => item.email === data.email.toLowerCase());
    if (!user || !verifyPassword(data.password, user.passwordHash)) {
      return fail("INVALID_LOGIN", "Email or password is incorrect.");
    }

    const session = await createSession(user.id);
    return ok({ user: publicUser(user), token: session.token, expiresAt: session.expiresAt });
  });

export const getCurrentUser = createServerFn({ method: "POST" })
  .inputValidator(tokenSchema)
  .handler(async ({ data }) => {
    const { requireUser } = await backend();
    return requireUser(data.token);
  });

export const logout = createServerFn({ method: "POST" })
  .inputValidator(z.object({ token: z.string().min(20) }))
  .handler(async ({ data }) => {
    const { mutateDb, ok } = await backend();

    return mutateDb((db) => {
      db.sessions = db.sessions.filter((session) => session.token !== data.token);
      return ok({ loggedOut: true });
    });
  });

export const listPins = createServerFn({ method: "POST" })
  .inputValidator(
    pagingSchema.extend({
      query: z.string().max(80).optional(),
      category: z.string().max(60).optional(),
      creatorId: z.string().optional(),
    }),
  )
  .handler(async ({ data }) => {
    const { getDb, ok } = await backend();
    const db = await getDb();
    const start = data.cursor ? Number(data.cursor) : 0;
    const query = data.query?.toLowerCase();

    const filtered = db.pins
      .filter((pin) => pin.status === "published")
      .filter((pin) => !data.category || pin.category === data.category)
      .filter((pin) => !data.creatorId || pin.creatorId === data.creatorId)
      .filter((pin) => !query || [pin.title, pin.description, pin.tags.join(" ")].join(" ").toLowerCase().includes(query))
      .sort((a, b) => b.likes + b.saves - (a.likes + a.saves));

    const items = filtered.slice(start, start + data.limit).map((pin) => ({
      ...pin,
      creator: db.creators.find((creator) => creator.id === pin.creatorId),
    }));

    return ok({ items, nextCursor: start + data.limit < filtered.length ? String(start + data.limit) : null });
  });

export const getPin = createServerFn({ method: "POST" })
  .inputValidator(z.object({ id: z.string().min(1) }))
  .handler(async ({ data }) => {
    const { fail, getDb, ok, publicUser } = await backend();
    const db = await getDb();
    const pin = db.pins.find((item) => item.id === data.id && item.status !== "hidden");
    if (!pin) return fail("NOT_FOUND", "Pin was not found.");

    return ok({
      ...pin,
      creator: db.creators.find((creator) => creator.id === pin.creatorId),
      comments: db.comments
        .filter((comment) => comment.pinId === pin.id)
        .map((comment) => ({ ...comment, user: publicUser(db.users.find((user) => user.id === comment.userId)!) })),
    });
  });

export const createPin = createServerFn({ method: "POST" })
  .inputValidator(
    tokenSchema.extend({
      title: z.string().min(2).max(120),
      description: z.string().max(600).default(""),
      category: z.string().min(1).max(60),
      tags: z.array(z.string().min(1).max(30)).max(8).default([]),
      ratio: z.number().min(0.5).max(2).default(1.25),
      seed: z.number().int().min(1).optional(),
    }),
  )
  .handler(async ({ data }) => {
    const { fail, mutateDb, newId, ok, requireUser, timestamp } = await backend();
    const auth = await requireUser(data.token, ["creator", "moderator", "admin"]);
    if (!auth.ok) return auth;

    return mutateDb((db) => {
      const creator = db.creators.find((item) => item.userId === auth.data.id);
      if (!creator) return fail("CREATOR_PROFILE_REQUIRED", "Create a creator profile before publishing pins.");

      const pin = {
        id: newId("pin"),
        seed: data.seed ?? Math.floor(Math.random() * 10000),
        title: data.title,
        description: data.description,
        category: data.category,
        creatorId: creator.id,
        likes: 0,
        saves: 0,
        comments: 0,
        ratio: data.ratio,
        tags: data.tags,
        status: "published" as const,
        createdAt: timestamp(),
      };

      db.pins.unshift(pin);
      return ok(pin);
    });
  });

export const reactToPin = createServerFn({ method: "POST" })
  .inputValidator(tokenSchema.extend({ pinId: z.string().min(1), action: z.enum(["like", "save"]) }))
  .handler(async ({ data }) => {
    const { fail, mutateDb, ok, requireUser } = await backend();
    const auth = await requireUser(data.token);
    if (!auth.ok) return auth;

    return mutateDb((db) => {
      const pin = db.pins.find((item) => item.id === data.pinId);
      if (!pin) return fail("NOT_FOUND", "Pin was not found.");

      if (data.action === "like") pin.likes += 1;
      if (data.action === "save") pin.saves += 1;
      return ok(pin);
    });
  });

export const commentOnPin = createServerFn({ method: "POST" })
  .inputValidator(tokenSchema.extend({ pinId: z.string().min(1), body: z.string().min(1).max(600) }))
  .handler(async ({ data }) => {
    const { fail, mutateDb, newId, ok, requireUser, timestamp } = await backend();
    const auth = await requireUser(data.token);
    if (!auth.ok) return auth;

    return mutateDb((db) => {
      const pin = db.pins.find((item) => item.id === data.pinId);
      if (!pin) return fail("NOT_FOUND", "Pin was not found.");

      const comment = { id: newId("comment"), pinId: pin.id, userId: auth.data.id, body: data.body, createdAt: timestamp() };
      db.comments.push(comment);
      pin.comments += 1;
      return ok(comment);
    });
  });

export const listCreators = createServerFn({ method: "POST" })
  .inputValidator(pagingSchema.extend({ query: z.string().max(80).optional(), specialty: z.string().optional() }))
  .handler(async ({ data }) => {
    const { getDb, ok } = await backend();
    const db = await getDb();
    const start = data.cursor ? Number(data.cursor) : 0;
    const query = data.query?.toLowerCase();
    const items = db.creators
      .filter((creator) => !data.specialty || creator.specialties.includes(data.specialty))
      .filter((creator) => !query || [creator.name, creator.handle, creator.city, creator.specialties.join(" ")].join(" ").toLowerCase().includes(query))
      .sort((a, b) => b.rating - a.rating || b.followers - a.followers);

    return ok({ items: items.slice(start, start + data.limit), nextCursor: start + data.limit < items.length ? String(start + data.limit) : null });
  });

export const listServices = createServerFn({ method: "POST" })
  .inputValidator(pagingSchema.extend({ query: z.string().max(80).optional(), category: z.string().optional(), creatorId: z.string().optional() }))
  .handler(async ({ data }) => {
    const { getDb, ok } = await backend();
    const db = await getDb();
    const start = data.cursor ? Number(data.cursor) : 0;
    const query = data.query?.toLowerCase();
    const filtered = db.services
      .filter((service) => service.active)
      .filter((service) => !data.category || service.category === data.category)
      .filter((service) => !data.creatorId || service.creatorId === data.creatorId)
      .filter((service) => !query || [service.title, service.description, service.category].join(" ").toLowerCase().includes(query))
      .sort((a, b) => b.rating - a.rating || b.reviews - a.reviews)
      .map((service) => ({ ...service, creator: db.creators.find((creator) => creator.id === service.creatorId) }));

    return ok({ items: filtered.slice(start, start + data.limit), nextCursor: start + data.limit < filtered.length ? String(start + data.limit) : null });
  });

export const createService = createServerFn({ method: "POST" })
  .inputValidator(
    tokenSchema.extend({
      title: z.string().min(5).max(140),
      description: z.string().min(20).max(1200),
      category: z.string().min(1).max(60),
      price: z.number().int().min(5).max(50000),
      deliveryDays: z.number().int().min(1).max(120),
    }),
  )
  .handler(async ({ data }) => {
    const { fail, mutateDb, newId, ok, requireUser, timestamp } = await backend();
    const auth = await requireUser(data.token, ["creator", "admin"]);
    if (!auth.ok) return auth;

    return mutateDb((db) => {
      const creator = db.creators.find((item) => item.userId === auth.data.id);
      if (!creator) return fail("CREATOR_PROFILE_REQUIRED", "Create a creator profile before adding services.");

      const service = {
        id: newId("service"),
        creatorId: creator.id,
        seed: Math.floor(Math.random() * 10000),
        title: data.title,
        description: data.description,
        category: data.category,
        price: data.price,
        rating: 0,
        reviews: 0,
        deliveryDays: data.deliveryDays,
        active: true,
        createdAt: timestamp(),
      };

      db.services.unshift(service);
      return ok(service);
    });
  });

export const createOrder = createServerFn({ method: "POST" })
  .inputValidator(tokenSchema.extend({ serviceId: z.string().min(1), brief: z.string().min(10).max(2000) }))
  .handler(async ({ data }) => {
    const { dueDate, fail, mutateDb, ok, requireUser, timestamp } = await backend();
    const auth = await requireUser(data.token);
    if (!auth.ok) return auth;

    return mutateDb((db) => {
      const service = db.services.find((item) => item.id === data.serviceId && item.active);
      if (!service) return fail("NOT_FOUND", "Service was not found.");

      const order = {
        id: `DP-${Math.floor(1000 + Math.random() * 9000)}`,
        buyerId: auth.data.id,
        creatorId: service.creatorId,
        serviceId: service.id,
        status: "in_progress" as const,
        brief: data.brief,
        total: service.price,
        dueAt: dueDate(service.deliveryDays),
        createdAt: timestamp(),
        updatedAt: timestamp(),
      };

      db.orders.unshift(order);
      return ok(order);
    });
  });

export const listOrders = createServerFn({ method: "POST" })
  .inputValidator(tokenSchema.extend({ status: z.enum(orderStatuses as [OrderStatus, ...OrderStatus[]]).optional() }))
  .handler(async ({ data }) => {
    const { getDb, ok, publicUser, requireUser } = await backend();
    const auth = await requireUser(data.token);
    if (!auth.ok) return auth;

    const db = await getDb();
    const creator = db.creators.find((item) => item.userId === auth.data.id);
    const items = db.orders
      .filter((order) => auth.data.role === "admin" || order.buyerId === auth.data.id || order.creatorId === creator?.id)
      .filter((order) => !data.status || order.status === data.status)
      .map((order) => ({
        ...order,
        service: db.services.find((service) => service.id === order.serviceId),
        buyer: publicUser(db.users.find((user) => user.id === order.buyerId)!),
      }));

    return ok(items);
  });

export const listBoards = createServerFn({ method: "POST" })
  .inputValidator(tokenSchema)
  .handler(async ({ data }) => {
    const { getDb, ok, requireUser } = await backend();
    const auth = await requireUser(data.token);
    if (!auth.ok) return auth;

    const db = await getDb();
    const items = db.boards
      .filter((board) => board.ownerId === auth.data.id || !board.isPrivate)
      .map((board) => ({ ...board, pins: board.pinIds.map((pinId) => db.pins.find((pin) => pin.id === pinId)).filter(Boolean) }));

    return ok(items);
  });

export const createBoard = createServerFn({ method: "POST" })
  .inputValidator(tokenSchema.extend({ name: z.string().min(2).max(80), description: z.string().max(400).default(""), isPrivate: z.boolean().default(false) }))
  .handler(async ({ data }) => {
    const { mutateDb, newId, ok, requireUser, timestamp } = await backend();
    const auth = await requireUser(data.token);
    if (!auth.ok) return auth;

    return mutateDb((db) => {
      const board = {
        id: newId("board"),
        ownerId: auth.data.id,
        name: data.name,
        description: data.description,
        pinIds: [],
        isPrivate: data.isPrivate,
        createdAt: timestamp(),
      };

      db.boards.unshift(board);
      return ok(board);
    });
  });

export const savePinToBoard = createServerFn({ method: "POST" })
  .inputValidator(tokenSchema.extend({ boardId: z.string().min(1), pinId: z.string().min(1) }))
  .handler(async ({ data }) => {
    const { fail, mutateDb, ok, requireUser } = await backend();
    const auth = await requireUser(data.token);
    if (!auth.ok) return auth;

    return mutateDb((db) => {
      const board = db.boards.find((item) => item.id === data.boardId && item.ownerId === auth.data.id);
      const pin = db.pins.find((item) => item.id === data.pinId);
      if (!board || !pin) return fail("NOT_FOUND", "Board or pin was not found.");

      if (!board.pinIds.includes(pin.id)) {
        board.pinIds.unshift(pin.id);
        pin.saves += 1;
      }

      return ok(board);
    });
  });

export const listMessages = createServerFn({ method: "POST" })
  .inputValidator(tokenSchema.extend({ conversationId: z.string().optional() }))
  .handler(async ({ data }) => {
    const { getDb, ok, requireUser } = await backend();
    const auth = await requireUser(data.token);
    if (!auth.ok) return auth;

    const db = await getDb();
    const conversations = db.conversations.filter((conversation) => conversation.participantIds.includes(auth.data.id));
    const activeId = data.conversationId ?? conversations[0]?.id;
    const messages = db.messages.filter((message) => message.conversationId === activeId);

    return ok({ conversations, activeId, messages });
  });

export const sendMessage = createServerFn({ method: "POST" })
  .inputValidator(tokenSchema.extend({ recipientId: z.string().min(1), body: z.string().min(1).max(2000) }))
  .handler(async ({ data }) => {
    const { fail, mutateDb, newId, ok, requireUser, timestamp } = await backend();
    const auth = await requireUser(data.token);
    if (!auth.ok) return auth;

    return mutateDb((db) => {
      const recipient = db.users.find((user) => user.id === data.recipientId);
      if (!recipient) return fail("NOT_FOUND", "Recipient was not found.");

      let conversation = db.conversations.find(
        (item) => item.participantIds.includes(auth.data.id) && item.participantIds.includes(recipient.id),
      );

      if (!conversation) {
        conversation = { id: newId("chat"), participantIds: [auth.data.id, recipient.id], lastMessageAt: timestamp() };
        db.conversations.unshift(conversation);
      }

      const message = {
        id: newId("msg"),
        conversationId: conversation.id,
        senderId: auth.data.id,
        body: data.body,
        createdAt: timestamp(),
        readBy: [auth.data.id],
      };

      conversation.lastMessageAt = message.createdAt;
      db.messages.push(message);
      return ok(message);
    });
  });

export const adminOverview = createServerFn({ method: "POST" })
  .inputValidator(tokenSchema)
  .handler(async ({ data }) => {
    const { getDb, ok, requireUser } = await backend();
    const auth = await requireUser(data.token, ["admin", "moderator"]);
    if (!auth.ok) return auth;

    const db = await getDb();
    return ok({
      users: db.users.length,
      creators: db.creators.length,
      pins: db.pins.length,
      services: db.services.length,
      orders: db.orders.length,
      openReports: db.reports.filter((report) => report.status === "open").length,
      revenue: db.orders.reduce((sum, order) => sum + order.total, 0),
    });
  });

export const moderatePin = createServerFn({ method: "POST" })
  .inputValidator(tokenSchema.extend({ pinId: z.string().min(1), status: z.enum(["published", "hidden", "flagged"]) }))
  .handler(async ({ data }) => {
    const { fail, mutateDb, ok, requireUser } = await backend();
    const auth = await requireUser(data.token, ["admin", "moderator"]);
    if (!auth.ok) return auth;

    return mutateDb((db) => {
      const pin = db.pins.find((item) => item.id === data.pinId);
      if (!pin) return fail("NOT_FOUND", "Pin was not found.");

      pin.status = data.status;
      return ok(pin);
    });
  });
