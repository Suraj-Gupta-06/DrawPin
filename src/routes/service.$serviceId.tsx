import { createFileRoute, Link, useParams } from "@tanstack/react-router";
import { Star, Clock, RefreshCw, Check, Heart, Share2, MessageSquare } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs";
import { AppShell } from "@/components/layout/AppShell";
import { ArtTile } from "@/components/art/ArtTile";
import { GradientAvatar } from "@/components/art/GradientAvatar";
import { SERVICES, fmt } from "@/lib/mock-data";

export const Route = createFileRoute("/service/$serviceId")({
  head: () => ({ meta: [{ title: "Service details — DrawPin" }, { name: "description", content: "Service details, packages and pricing." }] }),
  component: ServiceDetails,
});

const TIERS = [
  { name: "Basic", price: 0.6, items: ["1 concept", "2 revisions", "Source file", "Commercial use"] },
  { name: "Standard", price: 1, items: ["3 concepts", "5 revisions", "Source files", "Commercial use", "Priority support"], popular: true },
  { name: "Premium", price: 2.1, items: ["6 concepts", "Unlimited revisions", "All source files", "Full ownership", "Priority support", "Brand guidelines"] },
];

function ServiceDetails() {
  const { serviceId } = useParams({ from: "/service/$serviceId" });
  const service = SERVICES.find((s) => s.id === serviceId) ?? SERVICES[0];

  return (
    <AppShell>
      <div className="mx-auto max-w-6xl px-4 py-6">
        <div className="grid gap-8 lg:grid-cols-[1.6fr_1fr]">
          <div>
            <h1 className="font-display text-2xl font-bold">{service.title}</h1>
            <div className="mt-3 flex items-center gap-3">
              <Link to="/creator/$creatorId" params={{ creatorId: service.creator.id }} className="flex items-center gap-2">
                <GradientAvatar seed={service.creator.seed} name={service.creator.name} className="size-8 text-xs" />
                <span className="font-medium">{service.creator.name}</span>
              </Link>
              <span className="flex items-center gap-1 text-sm"><Star className="size-4 fill-warning text-warning" /> {service.rating} ({service.reviews})</span>
              <Badge variant="brand">{service.creator.level}</Badge>
            </div>

            <div className="mt-5 overflow-hidden rounded-3xl border">
              <ArtTile seed={service.seed} rounded={false} className="aspect-[16/10]" />
            </div>
            <div className="mt-3 grid grid-cols-4 gap-3">
              {[1, 2, 3, 4].map((n) => <ArtTile key={n} seed={service.seed + n * 9} className="aspect-square" />)}
            </div>

            <h2 className="mt-8 font-display text-xl font-bold">About this service</h2>
            <p className="mt-3 text-muted-foreground">I create premium, on-brand visual work tailored to your vision. With years of experience across editorial, branding and digital, I deliver polished assets ready for production. Every project starts with a discovery call to align on direction and references.</p>

            <div className="mt-6 flex flex-wrap gap-2">
              {service.creator.specialties.map((s) => <Badge key={s} variant="secondary">{s}</Badge>)}
            </div>
          </div>

          <div className="lg:sticky lg:top-20 lg:self-start">
            <Tabs defaultValue="Standard">
              <TabsList className="grid w-full grid-cols-3">
                {TIERS.map((t) => <TabsTrigger key={t.name} value={t.name}>{t.name}</TabsTrigger>)}
              </TabsList>
              {TIERS.map((t) => (
                <TabsContent key={t.name} value={t.name}>
                  <div className="rounded-3xl border bg-card p-6">
                    <div className="flex items-center justify-between">
                      <span className="font-display text-3xl font-bold">${Math.round(service.price * t.price)}</span>
                      {t.popular && <Badge variant="pink">Most popular</Badge>}
                    </div>
                    <div className="mt-4 flex gap-4 text-sm text-muted-foreground">
                      <span className="flex items-center gap-1"><Clock className="size-4" /> {service.delivery}</span>
                      <span className="flex items-center gap-1"><RefreshCw className="size-4" /> {t.name === "Premium" ? "∞" : t.items.length} revisions</span>
                    </div>
                    <ul className="mt-4 space-y-2">
                      {t.items.map((it) => <li key={it} className="flex items-center gap-2 text-sm"><Check className="size-4 text-success" /> {it}</li>)}
                    </ul>
                    <Link to="/checkout"><Button variant="brand" className="mt-5 h-11 w-full rounded-xl">Continue (${Math.round(service.price * t.price)})</Button></Link>
                    <Link to="/messages/$chatId" params={{ chatId: "chat1" }}><Button variant="outline" className="mt-2 h-11 w-full rounded-xl"><MessageSquare className="size-4" /> Contact creator</Button></Link>
                    <div className="mt-3 flex justify-center gap-4 text-sm text-muted-foreground">
                      <button className="flex items-center gap-1 hover:text-foreground"><Heart className="size-4" /> Save</button>
                      <button className="flex items-center gap-1 hover:text-foreground"><Share2 className="size-4" /> Share</button>
                    </div>
                  </div>
                </TabsContent>
              ))}
            </Tabs>
          </div>
        </div>
      </div>
    </AppShell>
  );
}
