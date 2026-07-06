import { createFileRoute, Link } from "@tanstack/react-router";
import { RefreshCw, Home, ServerCrash } from "lucide-react";
import { Button } from "@/components/ui/button";

export const Route = createFileRoute("/500")({
  head: () => ({ meta: [{ title: "Server error — DrawPin" }, { name: "description", content: "Something went wrong on our end." }] }),
  component: ServerError,
});

function ServerError() {
  return (
    <div className="relative flex min-h-screen items-center justify-center overflow-hidden px-4">
      <div className="pointer-events-none absolute -left-40 top-0 size-[40rem] rounded-full bg-pink/15 blur-3xl" />
      <div className="pointer-events-none absolute -right-40 bottom-0 size-[36rem] rounded-full bg-primary/15 blur-3xl" />
      <div className="relative max-w-md text-center">
        <span className="mx-auto grid size-20 place-items-center rounded-3xl brand-gradient text-white"><ServerCrash className="size-10" /></span>
        <h1 className="mt-6 font-display text-7xl font-extrabold text-gradient">500</h1>
        <h2 className="mt-2 font-display text-xl font-semibold">Something went wrong</h2>
        <p className="mt-2 text-muted-foreground">Our servers hit an unexpected error. We've been notified and are looking into it.</p>
        <div className="mt-6 flex justify-center gap-3">
          <Button variant="brand" className="rounded-full" onClick={() => location.reload()}><RefreshCw className="size-4" /> Try again</Button>
          <Link to="/"><Button variant="outline" className="rounded-full"><Home className="size-4" /> Go home</Button></Link>
        </div>
      </div>
    </div>
  );
}
