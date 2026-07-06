import { createFileRoute } from "@tanstack/react-router";
import { Heart, UserPlus, MessageCircle, ShoppingBag, Bookmark, Star, CheckCheck } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs";
import { AppShell } from "@/components/layout/AppShell";
import { GradientAvatar } from "@/components/art/GradientAvatar";
import { NOTIFICATIONS } from "@/lib/mock-data";

export const Route = createFileRoute("/notifications")({
  head: () => ({ meta: [{ title: "Notifications — DrawPin" }, { name: "description", content: "Your latest activity and notifications." }] }),
  component: Notifications,
});

const ICONS: Record<string, any> = { like: Heart, follow: UserPlus, comment: MessageCircle, order: ShoppingBag, save: Bookmark, review: Star };

function List({ items }: { items: typeof NOTIFICATIONS }) {
  return (
    <div className="space-y-2">
      {items.map((n, i) => {
        const Icon = ICONS[n.type];
        return (
          <div key={n.id} className={`flex items-center gap-3 rounded-2xl border p-3.5 ${i < 2 ? "bg-primary/5" : "bg-card"}`}>
            <div className="relative">
              <GradientAvatar seed={n.actor.seed} name={n.actor.name} className="size-11 text-sm" />
              <span className="absolute -bottom-1 -right-1 grid size-6 place-items-center rounded-full bg-card ring-2 ring-background"><Icon className="size-3 text-primary" /></span>
            </div>
            <p className="flex-1 text-sm"><span className="font-semibold">{n.actor.name}</span> {n.text}</p>
            <span className="text-xs text-muted-foreground">{n.time}</span>
            {i < 2 && <span className="size-2 rounded-full bg-pink" />}
          </div>
        );
      })}
    </div>
  );
}

function Notifications() {
  return (
    <AppShell>
      <div className="mx-auto max-w-2xl px-4 py-6">
        <div className="flex items-center justify-between">
          <h1 className="font-display text-2xl font-bold">Notifications</h1>
          <Button variant="ghost" size="sm"><CheckCheck className="size-4" /> Mark all read</Button>
        </div>
        <Tabs defaultValue="all" className="mt-5">
          <TabsList>
            <TabsTrigger value="all">All</TabsTrigger>
            <TabsTrigger value="social">Social</TabsTrigger>
            <TabsTrigger value="orders">Orders</TabsTrigger>
          </TabsList>
          <TabsContent value="all" className="mt-5"><List items={NOTIFICATIONS} /></TabsContent>
          <TabsContent value="social" className="mt-5"><List items={NOTIFICATIONS.filter((n) => ["like", "follow", "comment", "save"].includes(n.type))} /></TabsContent>
          <TabsContent value="orders" className="mt-5"><List items={NOTIFICATIONS.filter((n) => ["order", "review"].includes(n.type))} /></TabsContent>
        </Tabs>
      </div>
    </AppShell>
  );
}
