import { createFileRoute, Link } from "@tanstack/react-router";
import { MapPin, LinkIcon, CalendarDays, Settings, Share2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs";
import { AppShell } from "@/components/layout/AppShell";
import { ArtTile } from "@/components/art/ArtTile";
import { GradientAvatar } from "@/components/art/GradientAvatar";
import { MasonryGrid } from "@/components/shared/MasonryGrid";
import { PINS, BOARDS, NOTIFICATIONS, fmt } from "@/lib/mock-data";

export const Route = createFileRoute("/profile")({
  head: () => ({ meta: [{ title: "Aria Vance — DrawPin" }, { name: "description", content: "View profile, saved pins, boards and activity." }] }),
  component: Profile,
});

function Profile() {
  return (
    <AppShell>
      <div className="relative h-48 overflow-hidden md:h-60">
        <ArtTile seed={42} rounded={false} className="absolute inset-0" />
        <div className="absolute inset-0 bg-gradient-to-t from-background to-transparent" />
      </div>
      <div className="mx-auto max-w-[1400px] px-4">
        <div className="relative z-10 -mt-16 flex flex-col items-center text-center">
          <GradientAvatar seed={3} name="Aria Vance" className="size-32 text-3xl ring-4 ring-background" />
          <h1 className="mt-4 font-display text-2xl font-bold">Aria Vance</h1>
          <p className="text-muted-foreground">@aria.vance</p>
          <p className="mt-3 max-w-md text-sm text-muted-foreground">Visual artist & illustrator crafting bold gradient worlds. Available for select commissions.</p>
          <div className="mt-3 flex flex-wrap items-center justify-center gap-4 text-sm text-muted-foreground">
            <span className="flex items-center gap-1"><MapPin className="size-4" /> Berlin</span>
            <span className="flex items-center gap-1"><LinkIcon className="size-4" /> aria.studio</span>
            <span className="flex items-center gap-1"><CalendarDays className="size-4" /> Joined 2022</span>
          </div>
          <div className="mt-4 flex gap-6">
            <Link to="/followers" className="text-center"><div className="font-display text-lg font-bold">12.4k</div><div className="text-xs text-muted-foreground">Followers</div></Link>
            <Link to="/followers" className="text-center"><div className="font-display text-lg font-bold">312</div><div className="text-xs text-muted-foreground">Following</div></Link>
            <div className="text-center"><div className="font-display text-lg font-bold">{PINS.length}</div><div className="text-xs text-muted-foreground">Pins</div></div>
          </div>
          <div className="mt-5 flex gap-2">
            <Button variant="brand" className="rounded-full">Follow</Button>
            <Link to="/profile/edit"><Button variant="outline" className="rounded-full"><Settings className="size-4" /> Edit profile</Button></Link>
            <Button variant="outline" size="icon" className="rounded-full"><Share2 className="size-4" /></Button>
          </div>
        </div>

        <Tabs defaultValue="created" className="mt-8 pb-10">
          <TabsList className="mx-auto">
            <TabsTrigger value="created">Created</TabsTrigger>
            <TabsTrigger value="saved">Saved</TabsTrigger>
            <TabsTrigger value="boards">Boards</TabsTrigger>
            <TabsTrigger value="activity">Activity</TabsTrigger>
          </TabsList>
          <TabsContent value="created" className="mt-6"><MasonryGrid pins={PINS.slice(0, 20)} /></TabsContent>
          <TabsContent value="saved" className="mt-6"><MasonryGrid pins={PINS.slice(12, 32)} /></TabsContent>
          <TabsContent value="boards" className="mt-6">
            <div className="grid grid-cols-2 gap-5 sm:grid-cols-3 lg:grid-cols-4">
              {BOARDS.map((b) => (
                <Link key={b.id} to="/board/$boardId" params={{ boardId: b.id }}>
                  <div className="grid grid-cols-2 gap-1 overflow-hidden rounded-2xl border bg-card p-1">
                    <ArtTile seed={b.seeds[0]} className="col-span-1 row-span-2 aspect-[3/4]" />
                    <ArtTile seed={b.seeds[1]} className="aspect-square" rounded={false} />
                    <ArtTile seed={b.seeds[2]} className="aspect-square" rounded={false} />
                  </div>
                  <p className="mt-2 font-display font-semibold">{b.name}</p>
                  <p className="text-sm text-muted-foreground">{b.count} pins</p>
                </Link>
              ))}
            </div>
          </TabsContent>
          <TabsContent value="activity" className="mt-6">
            <div className="mx-auto max-w-xl space-y-3">
              {NOTIFICATIONS.map((n) => (
                <div key={n.id} className="flex items-center gap-3 rounded-2xl border bg-card p-3">
                  <GradientAvatar seed={n.actor.seed} name={n.actor.name} className="size-10 text-xs" />
                  <p className="flex-1 text-sm"><span className="font-semibold">{n.actor.name}</span> {n.text}</p>
                  <span className="text-xs text-muted-foreground">{n.time}</span>
                </div>
              ))}
            </div>
          </TabsContent>
        </Tabs>
      </div>
    </AppShell>
  );
}
