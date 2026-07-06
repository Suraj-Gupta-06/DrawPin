import { mkdir, readFile, writeFile } from "node:fs/promises";
import path from "node:path";
import process from "node:process";
import { createHash, randomBytes, scryptSync, timingSafeEqual } from "node:crypto";

import { BOARDS, CREATORS, MESSAGES, ORDERS, PINS, SERVICES } from "@/lib/mock-data";
import type {
  ApiResponse,
  Board,
  Comment,
  Conversation,
  Creator,
  DrawPinDb,
  Message,
  Order,
  Pin,
  PublicUser,
  Report,
  Role,
  Service,
  Session,
  User,
} from "./types";

const DATA_DIR = path.resolve(process.cwd(), ".drawpin-data");
const DB_FILE = path.join(DATA_DIR, "db.json");
const SESSION_DAYS = 14;

let cache: DrawPinDb | undefined;
let writeQueue = Promise.resolve();

function now() {
  return new Date().toISOString();
}

function daysFromNow(days: number) {
  const date = new Date();
  date.setDate(date.getDate() + days);
  return date.toISOString();
}

export function publicUser(user: User): PublicUser {
  const { passwordHash, ...safe } = user;
  return safe;
}

export function ok<T>(data: T): ApiResponse<T> {
  return { ok: true, data };
}

export function fail<T = never>(code: string, message: string): ApiResponse<T> {
  return { ok: false, error: { code, message } };
}

export function newId(prefix: string) {
  return `${prefix}_${randomBytes(8).toString("hex")}`;
}

export function hashPassword(password: string) {
  const salt = randomBytes(16).toString("hex");
  const hash = scryptSync(password, salt, 64).toString("hex");
  return `${salt}:${hash}`;
}

export function verifyPassword(password: string, stored: string) {
  const [salt, original] = stored.split(":");
  if (!salt || !original) return false;

  const candidate = scryptSync(password, salt, 64);
  const originalBuffer = Buffer.from(original, "hex");
  return originalBuffer.length === candidate.length && timingSafeEqual(originalBuffer, candidate);
}

function handleFromEmail(email: string) {
  return email
    .split("@")[0]
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, ".")
    .replace(/^\.+|\.+$/g, "");
}

function seedUsers(): User[] {
  const passwordHash = hashPassword("drawpin123");
  const users = CREATORS.slice(0, 12).map<User>((creator, index) => ({
    id: `u${index + 1}`,
    name: creator.name,
    email: `${creator.handle}@drawpin.local`,
    handle: creator.handle,
    role: index === 0 ? "admin" : "creator",
    city: creator.city,
    bio: creator.bio,
    avatarSeed: creator.seed,
    specialties: creator.specialties,
    createdAt: now(),
    passwordHash,
  }));

  users.push({
    id: "u_collector",
    name: "Demo Collector",
    email: "collector@drawpin.local",
    handle: "demo.collector",
    role: "collector",
    city: "Mumbai",
    bio: "Collects visual systems, concept art, and bold editorial work.",
    avatarSeed: 909,
    specialties: ["Collecting", "Moodboards"],
    createdAt: now(),
    passwordHash,
  });

  return users;
}

function createSeedDb(): DrawPinDb {
  const users = seedUsers();
  const creators: Creator[] = CREATORS.map((creator, index) => ({
    ...creator,
    userId: users[index % 12].id,
  }));

  const pins: Pin[] = PINS.map((pin, index) => ({
    id: pin.id,
    seed: pin.seed,
    title: pin.title,
    description: `A curated ${pin.category} artwork prepared for discovery, boards, and creator portfolios.`,
    category: pin.category,
    creatorId: creators[index % creators.length].id,
    likes: pin.likes,
    saves: pin.saves,
    comments: pin.comments,
    ratio: pin.ratio,
    tags: pin.tags,
    status: "published",
    createdAt: daysFromNow(-index),
  }));

  const services: Service[] = SERVICES.map((service, index) => ({
    id: service.id,
    creatorId: creators[index % creators.length].id,
    seed: service.seed,
    title: service.title,
    description: "A fixed-scope creator service with milestones, delivery window, and secure order tracking.",
    category: service.category,
    price: service.price,
    rating: service.rating,
    reviews: service.reviews,
    deliveryDays: Number.parseInt(service.delivery, 10) || 5,
    active: true,
    createdAt: daysFromNow(-index),
  }));

  const boards: Board[] = BOARDS.map((board, index) => ({
    id: board.id,
    ownerId: users[index % users.length].id,
    name: board.name,
    description: `${board.name} board for saved inspiration.`,
    pinIds: pins.slice(index * 3, index * 3 + 6).map((pin) => pin.id),
    isPrivate: false,
    createdAt: daysFromNow(-index),
  }));

  const orders: Order[] = ORDERS.map((order, index) => {
    const service = services[index % services.length];
    return {
      id: order.id,
      buyerId: users.at(-1)!.id,
      creatorId: service.creatorId,
      serviceId: service.id,
      status: order.status.toLowerCase().replaceAll(" ", "_") as Order["status"],
      brief: "Demo order brief for a portfolio-ready creator workflow.",
      total: order.total,
      dueAt: daysFromNow(service.deliveryDays),
      createdAt: daysFromNow(-(index + 4)),
      updatedAt: daysFromNow(-(index + 1)),
    };
  });

  const conversation: Conversation = {
    id: "chat1",
    participantIds: [users[0].id, users.at(-1)!.id],
    lastMessageAt: now(),
  };

  const messages: Message[] = MESSAGES.map((message, index) => ({
    id: message.id,
    conversationId: conversation.id,
    senderId: message.me ? users[0].id : users.at(-1)!.id,
    body: message.text,
    createdAt: daysFromNow(-(MESSAGES.length - index) / 24),
    readBy: [],
  }));

  const reports: Report[] = [
    {
      id: "r1",
      reporterId: users.at(-1)!.id,
      targetType: "pin",
      targetId: pins[5].id,
      reason: "Possible duplicate upload",
      status: "open",
      createdAt: daysFromNow(-1),
    },
  ];

  return {
    users,
    sessions: [],
    creators,
    pins,
    comments: [],
    boards,
    services,
    orders,
    conversations: [conversation],
    messages,
    reports,
  };
}

async function persist(db: DrawPinDb) {
  await mkdir(DATA_DIR, { recursive: true });
  writeQueue = writeQueue.then(() => writeFile(DB_FILE, JSON.stringify(db, null, 2), "utf8"));
  await writeQueue;
}

export async function getDb() {
  if (cache) return cache;

  try {
    cache = JSON.parse(await readFile(DB_FILE, "utf8")) as DrawPinDb;
  } catch {
    cache = createSeedDb();
    await persist(cache);
  }

  return cache;
}

export async function saveDb(db: DrawPinDb) {
  cache = db;
  await persist(db);
}

export async function mutateDb<T>(fn: (db: DrawPinDb) => T | Promise<T>) {
  const db = await getDb();
  const result = await fn(db);
  await saveDb(db);
  return result;
}

export async function createSession(userId: string) {
  return mutateDb<Session>((db) => {
    const session: Session = {
      token: randomBytes(32).toString("hex"),
      userId,
      createdAt: now(),
      expiresAt: daysFromNow(SESSION_DAYS),
    };

    db.sessions = db.sessions.filter((item) => new Date(item.expiresAt) > new Date());
    db.sessions.push(session);
    return session;
  });
}

export async function requireUser(token: string | undefined, roles?: Role[]) {
  if (!token) return fail<PublicUser>("UNAUTHENTICATED", "Sign in to continue.");

  const db = await getDb();
  const session = db.sessions.find((item) => item.token === token);
  if (!session || new Date(session.expiresAt) <= new Date()) {
    return fail<PublicUser>("SESSION_EXPIRED", "Your session has expired.");
  }

  const user = db.users.find((item) => item.id === session.userId);
  if (!user) return fail<PublicUser>("UNAUTHENTICATED", "Session user was not found.");

  if (roles?.length && !roles.includes(user.role)) {
    return fail<PublicUser>("FORBIDDEN", "You do not have permission for this action.");
  }

  return ok(publicUser(user));
}

export function digest(value: string) {
  return createHash("sha256").update(value).digest("hex");
}

export function createUser(input: {
  name: string;
  email: string;
  password: string;
  role?: Role;
  city?: string;
  specialties?: string[];
}) {
  const handle = handleFromEmail(input.email);
  return {
    id: newId("user"),
    name: input.name,
    email: input.email.toLowerCase(),
    handle,
    role: input.role ?? "collector",
    city: input.city ?? "Remote",
    bio: "",
    avatarSeed: Math.floor(Math.random() * 10000),
    specialties: input.specialties ?? [],
    createdAt: now(),
    passwordHash: hashPassword(input.password),
  } satisfies User;
}

export function timestamp() {
  return now();
}

export function dueDate(days: number) {
  return daysFromNow(days);
}

export type { ApiResponse, Comment, PublicUser };
