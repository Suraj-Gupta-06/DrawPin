import { createFileRoute } from "@tanstack/react-router";
import { Link as LinkIcon } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs";
import { AppShell } from "@/components/layout/AppShell";
import { GradientAvatar } from "@/components/art/GradientAvatar";
import { CREATORS, fmt } from "@/lib/mock-data";
import { Search } from "lucide-react";

export const Route = createFileRoute("/followers")({
  head: () => ({ meta: [{ title: "Followers — DrawPin" }, { name: "description", content: "People who follow you and people you follow." }] }),
  component: Followers,
});

function PeopleList({ following }: { following: boolean }) {
  return (
    <div className="space-y-2">
      {CREATORS.map((c, i) => (
        <div key={c.id} className="flex items-center gap-3 rounded-2xl border bg-card p-3">
          <GradientAvatar seed={c.seed} name={c.name} className="size-12 text-sm" />
          <div className="min-w-0 flex-1">
            <p className="truncate font-medium">{c.name}</p>
            <p className="truncate text-sm text-muted-foreground">@{c.handle} · {fmt(c.followers)} followers</p>
          </div>
          <Button variant={following || i % 3 === 0 ? "outline" : "brand"} size="sm" className="rounded-full">
            {following || i % 3 === 0 ? "Following" : "Follow"}
          </Button>
        </div>
      ))}
    </div>
  );
}

function Followers() {
  return (
    <AppShell>
      <div className="mx-auto max-w-2xl px-4 py-6">
        <h1 className="font-display text-2xl font-bold">Aria Vance</h1>
        <Tabs defaultValue="followers" className="mt-5">
          <TabsList>
            <TabsTrigger value="followers">12.4k Followers</TabsTrigger>
            <TabsTrigger value="following">312 Following</TabsTrigger>
          </TabsList>
          <div className="relative mt-5">
            <Search className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
            <Input placeholder="Search people…" className="h-11 rounded-full pl-9" />
          </div>
          <TabsContent value="followers" className="mt-5"><PeopleList following={false} /></TabsContent>
          <TabsContent value="following" className="mt-5"><PeopleList following /></TabsContent>
        </Tabs>
      </div>
    </AppShell>
  );
}
