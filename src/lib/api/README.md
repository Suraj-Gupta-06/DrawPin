# DrawPin 2.0 Backend

This folder exposes the standalone backend for DrawPin 2.0 through TanStack Start server functions.

## What is included

- Auth: `register`, `login`, `getCurrentUser`, `logout`
- Discovery: `listPins`, `getPin`, `createPin`, `reactToPin`, `commentOnPin`
- Creators and marketplace: `listCreators`, `listServices`, `createService`, `createOrder`
- User workflows: `listOrders`, `listBoards`, `createBoard`, `savePinToBoard`
- Messaging: `listMessages`, `sendMessage`
- Admin/moderation: `adminOverview`, `moderatePin`

## Storage

The backend writes to `.drawpin-data/db.json` inside `DrawPin 2.0`. This makes the app functional without MongoDB,
Postgres, Supabase, or the old backend. The storage layer is isolated in `src/lib/backend/store.server.ts`, so a real
database can replace it later without rewriting the UI-facing API.

## Demo accounts

All seeded users use this password:

```text
drawpin123
```

Examples:

```text
aria.vance@drawpin.local
collector@drawpin.local
```

## Using from routes/components

```ts
import { login, listPins } from "@/lib/api/drawpin.functions";

const auth = await login({ data: { email: "collector@drawpin.local", password: "drawpin123" } });

if (auth.ok) {
  const pins = await listPins({ data: { token: auth.data.token, limit: 24 } });
}
```

## Real-world upgrade path

Replace `store.server.ts` with adapters for:

- Postgres/Prisma or Drizzle for relational data
- S3/R2/Cloudinary for uploaded artwork assets
- Stripe for payments and order escrow
- WebSockets or Pusher for live messages and notifications
- Redis for sessions/rate limits
