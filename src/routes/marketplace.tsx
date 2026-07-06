import { createFileRoute, Link } from "@tanstack/react-router";
import { Search, Sparkles, ArrowRight, Star } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { AppShell } from "@/components/layout/AppShell";
import { ServiceCard, CreatorCard } from "@/components/shared/cards";
import { ArtTile } from "@/components/art/ArtTile";
import { SERVICES, CREATORS, CATEGORIES } from "@/lib/mock-data";

export const Route = createFileRoute("/marketplace")({
  head: () => ({ meta: [{ title: "Marketplace — DrawPin" }, { name: "description", content: "Hire vetted creators for illustration, 3D, branding and more." }] }),
  component: Marketplace,
});

function Marketplace() {
  return (
    <AppShell>
      <section className="relative overflow-hidden border-b">
        <ArtTile seed={11} rounded={false} className="absolute inset-0 opacity-30" />
        <div className="absolute inset-0 bg-gradient-to-b from-background/60 to-background" />
        <div className="relative mx-auto max-w-4xl px-4 py-16 text-center">
          <Badge variant="glass" className="gap-1 rounded-full"><Sparkles className="size-3 text-pink" /> 12,000+ vetted creators</Badge>
          <h1 className="mt-4 font-display text-4xl font-extrabold">Hire the perfect creator</h1>
          <p className="mt-3 text-muted-foreground">From illustration to 3D, branding to motion — find world-class talent.</p>
          <div className="relative mx-auto mt-6 max-w-xl">
            <Search className="pointer-events-none absolute left-4 top-1/2 size-5 -translate-y-1/2 text-muted-foreground" />
            <Input placeholder="What service are you looking for?" className="h-13 rounded-full pl-11 pr-28 text-base" />
            <Button variant="brand" className="absolute right-1.5 top-1.5 rounded-full">Search</Button>
          </div>
          <div className="mt-4 flex flex-wrap justify-center gap-2">
            {CATEGORIES.slice(0, 6).map((c) => <Badge key={c.slug} variant="secondary" className="cursor-pointer rounded-full px-3 py-1.5">{c.name}</Badge>)}
          </div>
        </div>
      </section>

      <div className="mx-auto max-w-[1400px] px-4 py-10">
        <div className="flex items-end justify-between">
          <h2 className="font-display text-2xl font-bold">Top rated services</h2>
          <Link to="/search"><Button variant="ghost" size="sm">See all <ArrowRight className="size-4" /></Button></Link>
        </div>
        <div className="mt-5 grid gap-5 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
          {SERVICES.map((s) => <ServiceCard key={s.id} service={s} />)}
        </div>

        <h2 className="mt-12 font-display text-2xl font-bold">Featured creators</h2>
        <div className="mt-5 grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
          {CREATORS.slice(0, 8).map((c) => <CreatorCard key={c.id} creator={c} />)}
        </div>
      </div>
    </AppShell>
  );
}
