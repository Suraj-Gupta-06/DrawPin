export type Role = "collector" | "creator" | "moderator" | "admin";

export type OrderStatus =
  | "draft"
  | "in_progress"
  | "delivered"
  | "in_review"
  | "completed"
  | "cancelled";

export type User = {
  id: string;
  name: string;
  email: string;
  handle: string;
  role: Role;
  city: string;
  bio: string;
  avatarSeed: number;
  specialties: string[];
  createdAt: string;
  passwordHash: string;
};

export type PublicUser = Omit<User, "passwordHash">;

export type Session = {
  token: string;
  userId: string;
  expiresAt: string;
  createdAt: string;
};

export type Creator = {
  id: string;
  userId: string;
  seed: number;
  name: string;
  handle: string;
  city: string;
  lat: number;
  lng: number;
  followers: number;
  rating: number;
  reviews: number;
  level: "New" | "Rising" | "Top Rated" | "Pro";
  rate: number;
  bio: string;
  specialties: string[];
};

export type Pin = {
  id: string;
  seed: number;
  title: string;
  description: string;
  category: string;
  creatorId: string;
  likes: number;
  saves: number;
  comments: number;
  ratio: number;
  tags: string[];
  status: "published" | "hidden" | "flagged";
  createdAt: string;
};

export type Comment = {
  id: string;
  pinId: string;
  userId: string;
  body: string;
  createdAt: string;
};

export type Board = {
  id: string;
  ownerId: string;
  name: string;
  description: string;
  pinIds: string[];
  isPrivate: boolean;
  createdAt: string;
};

export type Service = {
  id: string;
  creatorId: string;
  seed: number;
  title: string;
  description: string;
  category: string;
  price: number;
  rating: number;
  reviews: number;
  deliveryDays: number;
  active: boolean;
  createdAt: string;
};

export type Order = {
  id: string;
  buyerId: string;
  creatorId: string;
  serviceId: string;
  status: OrderStatus;
  brief: string;
  total: number;
  dueAt: string;
  createdAt: string;
  updatedAt: string;
};

export type Conversation = {
  id: string;
  participantIds: string[];
  lastMessageAt: string;
};

export type Message = {
  id: string;
  conversationId: string;
  senderId: string;
  body: string;
  createdAt: string;
  readBy: string[];
};

export type Report = {
  id: string;
  reporterId: string;
  targetType: "pin" | "service" | "user";
  targetId: string;
  reason: string;
  status: "open" | "resolved" | "dismissed";
  createdAt: string;
};

export type DrawPinDb = {
  users: User[];
  sessions: Session[];
  creators: Creator[];
  pins: Pin[];
  comments: Comment[];
  boards: Board[];
  services: Service[];
  orders: Order[];
  conversations: Conversation[];
  messages: Message[];
  reports: Report[];
};

export type ApiResult<T> = {
  ok: true;
  data: T;
};

export type ApiError = {
  ok: false;
  error: {
    code: string;
    message: string;
  };
};

export type ApiResponse<T> = ApiResult<T> | ApiError;
