# DrawPin 2.0 Frontend

The frontend entry file is:

```text
src/frontend.ts
```

It points to the TanStack Router setup in:

```text
src/router.tsx
src/routeTree.gen.ts
src/routes/
src/components/
```

`src/routeTree.gen.ts` is generated route metadata. Keep it in the project, but do not edit it manually.

Most pages currently render from `src/lib/mock-data.ts`. The next real implementation step is connecting routes to the
backend functions exported from `src/backend.ts`.
