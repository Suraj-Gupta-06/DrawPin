import { createFileRoute, Link } from "@tanstack/react-router";
import { Search, Edit, MessageSquare } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { AppShell } from "@/components/layout/AppShell";
import { GradientAvatar } from "@/components/art/GradientAvatar";
import { CONVERSATIONS } from "@/lib/mock-data";

export const Route = createFileRoute("/messages")({
  head: () => ({ meta: [{ title: "Messages — DrawPin" }, { name: "description", content: "Your conversations with creators and clients." }] }),
  component: Messages,
});

function Messages() {
  return (
    <AppShell hideFooter>
      <div className="mx-auto max-w-2xl px-4 py-6">
        <div className="flex items-center justify-between">
          <h1 className="font-display text-2xl font-bold">Messages</h1>
          <Button variant="brand" size="icon" className="rounded-full"><Edit className="size-4" /></Button>
        </div>
        <div className="relative mt-5">
          <Search className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
          <Input placeholder="Search conversations…" className="h-11 rounded-full pl-9" />
        </div>
        <div className="mt-5 space-y-1">
          {CONVERSATIONS.map((c) => (
            <Link key={c.id} to="/messages/$chatId" params={{ chatId: c.id }} className="flex items-center gap-3 rounded-2xl p-3 transition-colors hover:bg-muted">
              <div className="relative">
                <GradientAvatar seed={c.creator.seed} name={c.creator.name} className="size-12 text-sm" />
                <span className="absolute bottom-0 right-0 size-3 rounded-full bg-success ring-2 ring-background" />
              </div>
              <div className="min-w-0 flex-1">
                <div className="flex items-center justify-between"><p className="truncate font-medium">{c.creator.name}</p><span className="text-xs text-muted-foreground">{c.time}</span></div>
                <p className={`truncate text-sm ${c.unread ? "font-medium text-foreground" : "text-muted-foreground"}`}>{c.last}</p>
              </div>
              {c.unread > 0 && <Badge variant="brand" className="size-5 justify-center rounded-full p-0">{c.unread}</Badge>}
            </Link>
          ))}
        </div>
      </div>
    </AppShell>
  );
}
