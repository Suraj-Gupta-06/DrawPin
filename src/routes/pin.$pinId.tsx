import { createFileRoute, Link, useParams } from "@tanstack/react-router";
import { Heart, Bookmark, Share2, MoreHorizontal, Send, MapPin } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import { AppShell } from "@/components/layout/AppShell";
import { ArtTile } from "@/components/art/ArtTile";
import { GradientAvatar } from "@/components/art/GradientAvatar";
import { PinCard } from "@/components/shared/cards";
import { PINS, CREATORS, fmt } from "@/lib/mock-data";

export const Route = createFileRoute("/pin/$pinId")({
  head: () => ({ meta: [{ title: "Artwork — DrawPin" }, { name: "description", content: "View artwork details, comments and related pins." }] }),
  component: PinDetails,
});

const COMMENTS = [
  { c: CREATORS[6], text: "The color transitions here are unreal. How did you build the gradient mesh?", time: "2h" },
  { c: CREATORS[9], text: "Saved to my moodboard immediately 🔥", time: "5h" },
  { c: CREATORS[2], text: "This belongs in a gallery. Stunning composition.", time: "1d" },
];

function PinDetails() {
  const { pinId } = useParams({ from: "/pin/$pinId" });
  const pin = PINS.find((p) => p.id === pinId) ?? PINS[0];
  const related = PINS.filter((p) => p.id !== pin.id).slice(0, 12);

  return (
    <AppShell>
      <div className="mx-auto max-w-6xl px-4 py-6">
        <div className="overflow-hidden rounded-3xl border bg-card lg:grid lg:grid-cols-2">
          <div className="relative">
            <ArtTile seed={pin.seed} rounded={false} className="h-full min-h-[22rem] w-full object-cover" />
          </div>
          <div className="flex flex-col p-6 lg:p-8">
            <div className="flex items-center justify-between">
              <div className="flex gap-2">
                <Button variant="ghost" size="icon" className="rounded-full"><Share2 className="size-5" /></Button>
                <Button variant="ghost" size="icon" className="rounded-full"><MoreHorizontal className="size-5" /></Button>
              </div>
              <Button variant="brand" className="rounded-full"><Bookmark className="size-4" /> Save</Button>
            </div>

            <h1 className="mt-5 font-display text-2xl font-bold">{pin.title}</h1>
            <p className="mt-2 text-muted-foreground">A study in luminous gradients and organic form, exploring the boundary between digital and analog texture.</p>
            <div className="mt-3 flex flex-wrap gap-1.5">
              {pin.tags.map((t) => <Badge key={t} variant="secondary">{t}</Badge>)}
            </div>

            <Link to="/creator/$creatorId" params={{ creatorId: pin.author.id }} className="mt-5 flex items-center justify-between rounded-2xl border p-3 transition-colors hover:bg-muted">
              <div className="flex items-center gap-3">
                <GradientAvatar seed={pin.author.seed} name={pin.author.name} className="size-11" />
                <div>
                  <p className="font-semibold">{pin.author.name}</p>
                  <p className="flex items-center gap-1 text-xs text-muted-foreground"><MapPin className="size-3" /> {pin.author.city} · {fmt(pin.author.followers)} followers</p>
                </div>
              </div>
              <Button variant="outline" size="sm" className="rounded-full">Follow</Button>
            </Link>

            <div className="mt-5 flex items-center gap-5 border-y py-3 text-sm">
              <span className="flex items-center gap-1.5"><Heart className="size-4 text-pink" /> {fmt(pin.likes)}</span>
              <span className="flex items-center gap-1.5"><Bookmark className="size-4 text-accent" /> {fmt(pin.saves)}</span>
              <span>{pin.comments} comments</span>
            </div>

            <div className="mt-4 flex-1 space-y-4 overflow-y-auto">
              <h3 className="font-display font-semibold">Comments</h3>
              {COMMENTS.map((cm, i) => (
                <div key={i} className="flex gap-3">
                  <GradientAvatar seed={cm.c.seed} name={cm.c.name} className="size-8 shrink-0 text-[10px]" />
                  <div>
                    <p className="text-sm"><span className="font-semibold">{cm.c.name}</span> <span className="text-xs text-muted-foreground">· {cm.time}</span></p>
                    <p className="text-sm text-muted-foreground">{cm.text}</p>
                  </div>
                </div>
              ))}
            </div>

            <div className="mt-4 flex items-center gap-2">
              <Input placeholder="Add a comment…" className="h-11 rounded-full" />
              <Button variant="brand" size="icon" className="size-11 shrink-0 rounded-full"><Send className="size-4" /></Button>
            </div>
          </div>
        </div>

        <h2 className="mt-10 font-display text-xl font-bold">More like this</h2>
        <div className="mt-4 columns-2 gap-4 sm:columns-3 lg:columns-4 xl:columns-5 [&>*]:mb-4">
          {related.map((p) => <PinCard key={p.id} pin={p} />)}
        </div>
      </div>
    </AppShell>
  );
}
