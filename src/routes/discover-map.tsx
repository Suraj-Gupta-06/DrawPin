import { createFileRoute, Link } from "@tanstack/react-router";
import { useState } from "react";
import { Search, SlidersHorizontal, Star, MapPin, Navigation } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Slider } from "@/components/ui/slider";
import { AppShell } from "@/components/layout/AppShell";
import { GradientAvatar } from "@/components/art/GradientAvatar";
import { CREATORS, CATEGORIES, fmt } from "@/lib/mock-data";
import { cn } from "@/lib/utils";

export const Route = createFileRoute("/discover-map")({
  head: () => ({ meta: [{ title: "Discover creators map — DrawPin" }, { name: "description", content: "Find talented artists near you on an interactive map." }] }),
  component: DiscoverMap,
});

function DiscoverMap() {
  const nearby = CREATORS.slice(0, 10);
  const [active, setActive] = useState(nearby[0].id);

  return (
    <AppShell hideFooter>
      <div className="grid h-[calc(100vh-4rem)] lg:grid-cols-[380px_1fr]">
        {/* Sidebar */}
        <div className="flex flex-col border-r">
          <div className="border-b p-4">
            <div className="relative">
              <Search className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
              <Input placeholder="Search by city or artist…" className="h-11 rounded-full pl-9" />
            </div>
            <div className="mt-3 flex items-center gap-2 overflow-x-auto no-scrollbar">
              <Button variant="outline" size="sm" className="shrink-0 rounded-full"><SlidersHorizontal className="size-3.5" /> Filters</Button>
              {CATEGORIES.slice(0, 5).map((c) => <Badge key={c.slug} variant="secondary" className="shrink-0 cursor-pointer rounded-full">{c.name}</Badge>)}
            </div>
            <div className="mt-4">
              <div className="flex items-center justify-between text-sm"><span className="font-medium">Distance</span><span className="text-muted-foreground">Within 25 km</span></div>
              <Slider defaultValue={[25]} max={100} step={5} className="mt-2" />
            </div>
          </div>
          <div className="flex-1 space-y-2 overflow-y-auto p-3">
            <p className="px-1 text-sm text-muted-foreground">{nearby.length} creators nearby</p>
            {nearby.map((c) => (
              <Link
                key={c.id}
                to="/creator/$creatorId"
                params={{ creatorId: c.id }}
                onMouseEnter={() => setActive(c.id)}
                className={cn("flex items-center gap-3 rounded-2xl border p-3 transition-colors", active === c.id ? "border-primary bg-primary/5" : "hover:bg-muted")}
              >
                <GradientAvatar seed={c.seed} name={c.name} className="size-12 text-sm" />
                <div className="min-w-0 flex-1">
                  <p className="truncate font-medium">{c.name}</p>
                  <p className="flex items-center gap-1 text-xs text-muted-foreground"><MapPin className="size-3" /> {c.city} · {c.specialties[0]}</p>
                  <p className="mt-0.5 flex items-center gap-1 text-xs"><Star className="size-3 fill-warning text-warning" /> {c.rating} · From ${c.rate}</p>
                </div>
              </Link>
            ))}
          </div>
        </div>

        {/* Map */}
        <div className="relative hidden overflow-hidden bg-muted lg:block">
          <svg className="absolute inset-0 h-full w-full" viewBox="0 0 800 600" preserveAspectRatio="xMidYMid slice">
            <defs>
              <pattern id="grid" width="40" height="40" patternUnits="userSpaceOnUse">
                <path d="M40 0H0V40" fill="none" stroke="oklch(0.5 0.02 265 / 0.15)" strokeWidth="1" />
              </pattern>
            </defs>
            <rect width="800" height="600" fill="oklch(0.22 0.03 265)" />
            <rect width="800" height="600" fill="url(#grid)" />
            <path d="M-50 220 Q200 180 400 260 T850 240" fill="none" stroke="oklch(0.4 0.05 265 / 0.5)" strokeWidth="18" />
            <path d="M120 -50 Q160 200 280 360 T420 650" fill="none" stroke="oklch(0.4 0.05 265 / 0.5)" strokeWidth="14" />
            <circle cx="560" cy="420" r="120" fill="oklch(0.3 0.06 265 / 0.4)" />
          </svg>

          {nearby.map((c, i) => {
            const x = 12 + ((c.seed * 37) % 76);
            const y = 14 + ((c.seed * 53) % 72);
            const isActive = active === c.id;
            return (
              <button
                key={c.id}
                onMouseEnter={() => setActive(c.id)}
                className={cn("absolute -translate-x-1/2 -translate-y-1/2 transition-all", isActive ? "z-20 scale-110" : "z-10")}
                style={{ left: `${x}%`, top: `${y}%` }}
              >
                <div className={cn("flex items-center gap-1.5 rounded-full p-1 pr-3 shadow-lg", isActive ? "brand-gradient text-white" : "glass-strong")}>
                  <GradientAvatar seed={c.seed} name={c.name} className="size-8 text-[10px]" />
                  <span className="text-xs font-semibold">${c.rate}</span>
                </div>
                {isActive && <span className="mx-auto mt-0.5 block size-2 rounded-full bg-primary" />}
              </button>
            );
          })}

          <div className="absolute right-4 top-4 flex flex-col gap-2">
            <Button variant="glass" size="icon" className="rounded-xl">+</Button>
            <Button variant="glass" size="icon" className="rounded-xl">−</Button>
            <Button variant="glass" size="icon" className="rounded-xl"><Navigation className="size-4" /></Button>
          </div>
        </div>
      </div>
    </AppShell>
  );
}
