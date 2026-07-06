import { createFileRoute, Link, useParams } from "@tanstack/react-router";
import { MapPin, Star, MessageSquare, Share2, BadgeCheck, Clock } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs";
import { AppShell } from "@/components/layout/AppShell";
import { ArtTile } from "@/components/art/ArtTile";
import { GradientAvatar } from "@/components/art/GradientAvatar";
import { ServiceCard } from "@/components/shared/cards";
import { CREATORS, SERVICES, PINS, fmt } from "@/lib/mock-data";

export const Route = createFileRoute("/creator/$creatorId")({
  head: () => ({ meta: [{ title: "Creator profile — DrawPin" }, { name: "description", content: "Portfolio, services, reviews and pricing." }] }),
  component: CreatorProfile,
});

const REVIEWS = [
  { c: CREATORS[3], rating: 5, text: "Exceptional work and communication. Delivered ahead of schedule and exceeded expectations.", time: "2 weeks ago" },
  { c: CREATORS[7], rating: 5, text: "One of the best creators I've worked with on DrawPin. Will hire again!", time: "1 month ago" },
  { c: CREATORS[10], rating: 4, text: "Great quality and very responsive throughout the project.", time: "2 months ago" },
];

function CreatorProfile() {
  const { creatorId } = useParams({ from: "/creator/$creatorId" });
  const creator = CREATORS.find((c) => c.id === creatorId) ?? CREATORS[0];
  const services = SERVICES.filter((_, i) => i % 2 === 0).slice(0, 4);

  return (
    <AppShell>
      <div className="relative h-44 overflow-hidden md:h-56">
        <ArtTile seed={creator.seed + 50} rounded={false} className="absolute inset-0" />
        <div className="absolute inset-0 bg-gradient-to-t from-background to-transparent" />
      </div>
      <div className="mx-auto max-w-[1400px] px-4">
        <div className="relative z-10 -mt-16 flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
          <div className="flex items-end gap-4">
            <GradientAvatar seed={creator.seed} name={creator.name} className="size-28 text-2xl ring-4 ring-background" />
            <div className="mb-2">
              <h1 className="flex items-center gap-2 font-display text-2xl font-bold">{creator.name} <BadgeCheck className="size-5 text-accent" /></h1>
              <p className="flex items-center gap-2 text-sm text-muted-foreground"><MapPin className="size-4" /> {creator.city} · @{creator.handle}</p>
              <div className="mt-1 flex items-center gap-3 text-sm">
                <span className="flex items-center gap-1"><Star className="size-4 fill-warning text-warning" /> {creator.rating} ({creator.reviews})</span>
                <Badge variant="brand">{creator.level}</Badge>
              </div>
            </div>
          </div>
          <div className="mb-2 flex gap-2">
            <Link to="/messages/$chatId" params={{ chatId: "chat1" }}><Button variant="outline" className="rounded-full"><MessageSquare className="size-4" /> Message</Button></Link>
            <Button variant="brand" className="rounded-full">Follow</Button>
            <Button variant="outline" size="icon" className="rounded-full"><Share2 className="size-4" /></Button>
          </div>
        </div>

        <p className="mt-5 max-w-2xl text-muted-foreground">{creator.bio}</p>
        <div className="mt-4 flex flex-wrap gap-2">
          {creator.specialties.map((s) => <Badge key={s} variant="secondary">{s}</Badge>)}
        </div>

        <Tabs defaultValue="portfolio" className="mt-8 pb-10">
          <TabsList>
            <TabsTrigger value="portfolio">Portfolio</TabsTrigger>
            <TabsTrigger value="services">Services</TabsTrigger>
            <TabsTrigger value="reviews">Reviews</TabsTrigger>
          </TabsList>

          <TabsContent value="portfolio" className="mt-6">
            <div className="columns-2 gap-4 sm:columns-3 lg:columns-4 [&>*]:mb-4">
              {PINS.slice(0, 12).map((p) => (
                <Link key={p.id} to="/pin/$pinId" params={{ pinId: p.id }} className="mb-4 block overflow-hidden rounded-2xl" style={{ aspectRatio: `1 / ${p.ratio}` }}>
                  <ArtTile seed={p.seed} />
                </Link>
              ))}
            </div>
          </TabsContent>

          <TabsContent value="services" className="mt-6">
            <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
              {services.map((s) => <ServiceCard key={s.id} service={s} />)}
            </div>
          </TabsContent>

          <TabsContent value="reviews" className="mt-6">
            <div className="grid gap-6 lg:grid-cols-[260px_1fr]">
              <div className="rounded-2xl border bg-card p-6 text-center">
                <div className="font-display text-5xl font-bold">{creator.rating}</div>
                <div className="mt-2 flex justify-center gap-0.5">{Array.from({ length: 5 }).map((_, i) => <Star key={i} className="size-4 fill-warning text-warning" />)}</div>
                <p className="mt-2 text-sm text-muted-foreground">{creator.reviews} reviews</p>
              </div>
              <div className="space-y-4">
                {REVIEWS.map((r, i) => (
                  <div key={i} className="rounded-2xl border bg-card p-4">
                    <div className="flex items-center gap-3">
                      <GradientAvatar seed={r.c.seed} name={r.c.name} className="size-10 text-xs" />
                      <div className="flex-1"><p className="font-medium">{r.c.name}</p><p className="text-xs text-muted-foreground">{r.time}</p></div>
                      <div className="flex gap-0.5">{Array.from({ length: r.rating }).map((_, j) => <Star key={j} className="size-3.5 fill-warning text-warning" />)}</div>
                    </div>
                    <p className="mt-3 text-sm text-muted-foreground">{r.text}</p>
                  </div>
                ))}
              </div>
            </div>
          </TabsContent>
        </Tabs>
      </div>
    </AppShell>
  );
}
