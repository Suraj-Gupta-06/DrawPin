import { createFileRoute, Link } from "@tanstack/react-router";
import {
  Sparkles, ArrowRight, Search, MapPin, ShoppingBag, Palette,
  Star, TrendingUp, Play, ShieldCheck,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { ArtTile } from "@/components/art/ArtTile";
import { GradientAvatar } from "@/components/art/GradientAvatar";
import { CreatorCard } from "@/components/shared/cards";
import { Footer } from "@/components/layout/Footer";
import { PINS, CREATORS, CATEGORIES, fmt } from "@/lib/mock-data";

export const Route = createFileRoute("/")({
  head: () => ({
    meta: [
      { title: "DrawPin — Discover art, hire creators, explore locally" },
      { name: "description", content: "DrawPin blends Pinterest-style art discovery, a creator marketplace, and map-based local artist discovery in one premium dark-mode platform." },
      { property: "og:title", content: "DrawPin — Discover art, hire creators, explore locally" },
      { property: "og:description", content: "Discover art, hire world-class creators, and find local artists on DrawPin." },
    ],
  }),
  component: Landing,
});

function Nav() {
  return (
    <header className="sticky top-0 z-50 border-b border-border/60 glass-strong">
      <div className="mx-auto flex h-16 max-w-7xl items-center gap-3 px-4">
        <Link to="/" className="flex items-center gap-2">
          <span className="grid size-9 place-items-center rounded-xl brand-gradient font-display text-lg font-bold text-white">D</span>
          <span className="font-display text-xl font-bold">DrawPin</span>
        </Link>
        <nav className="ml-8 hidden items-center gap-6 text-sm text-muted-foreground md:flex">
          <Link to="/explore" className="story-link hover:text-foreground">Explore</Link>
          <Link to="/marketplace" className="story-link hover:text-foreground">Marketplace</Link>
          <Link to="/discover-map" className="story-link hover:text-foreground">Discover map</Link>
        </nav>
        <div className="ml-auto flex items-center gap-2">
          <Link to="/login"><Button variant="ghost" size="sm">Log in</Button></Link>
          <Link to="/signup"><Button variant="brand" size="sm" className="rounded-full">Get started</Button></Link>
        </div>
      </div>
    </header>
  );
}

function Landing() {
  const hero = PINS.slice(0, 9);
  const trending = PINS.slice(9, 15);
  const featured = CREATORS.slice(0, 4);

  return (
    <div className="min-h-screen">
      <Nav />

      {/* Hero */}
      <section className="relative overflow-hidden">
        <div className="pointer-events-none absolute -left-40 top-0 size-[40rem] rounded-full bg-primary/20 blur-3xl" />
        <div className="pointer-events-none absolute -right-40 top-40 size-[36rem] rounded-full bg-pink/20 blur-3xl" />
        <div className="mx-auto grid max-w-7xl items-center gap-12 px-4 py-16 lg:grid-cols-2 lg:py-24">
          <div>
            <Badge variant="glass" className="gap-1.5 rounded-full px-3 py-1.5">
              <Sparkles className="size-3.5 text-pink" /> The creative platform for the next generation
            </Badge>
            <h1 className="mt-6 font-display text-5xl font-extrabold leading-[1.05] tracking-tight md:text-6xl">
              Discover art.<br />Hire creators.<br />
              <span className="text-gradient">Explore locally.</span>
            </h1>
            <p className="mt-6 max-w-md text-lg text-muted-foreground">
              DrawPin blends Pinterest-style discovery, a Fiverr-style marketplace, and a map of local artists — beautifully, in one place.
            </p>
            <div className="mt-8 flex flex-wrap gap-3">
              <Link to="/signup"><Button variant="hero" size="xl" className="rounded-full">Start exploring <ArrowRight className="size-5" /></Button></Link>
              <Link to="/marketplace"><Button variant="glass" size="xl" className="rounded-full"><Play className="size-4" /> Browse marketplace</Button></Link>
            </div>
            <div className="mt-10 flex items-center gap-6 text-sm text-muted-foreground">
              <div className="flex -space-x-3">
                {CREATORS.slice(0, 5).map((c) => (
                  <GradientAvatar key={c.id} seed={c.seed} name={c.name} className="size-9 text-[10px]" />
                ))}
              </div>
              <div>
                <div className="flex items-center gap-1 font-semibold text-foreground">
                  <Star className="size-4 fill-warning text-warning" /> 4.9/5
                </div>
                <span>from 40k+ creatives</span>
              </div>
            </div>
          </div>

          <div className="columns-3 gap-3">
            {hero.map((p, i) => (
              <div key={p.id} className={`mb-3 overflow-hidden rounded-2xl ${i % 2 ? "animate-float-slow" : ""}`} style={{ aspectRatio: `1 / ${p.ratio}` }}>
                <ArtTile seed={p.seed} />
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Feature triad */}
      <section className="mx-auto max-w-7xl px-4 py-12">
        <div className="grid gap-5 md:grid-cols-3">
          {[
            { icon: Palette, title: "Discover", text: "An endless masonry feed of curated art, illustration, 3D and more.", to: "/home" },
            { icon: ShoppingBag, title: "Hire", text: "Book vetted creators with transparent pricing, reviews and secure orders.", to: "/marketplace" },
            { icon: MapPin, title: "Explore locally", text: "Find talented artists near you on an interactive discovery map.", to: "/discover-map" },
          ].map((f) => (
            <Link key={f.title} to={f.to} className="group rounded-3xl border bg-card p-7 transition-all hover:-translate-y-1 hover:shadow-xl">
              <span className="grid size-12 place-items-center rounded-2xl brand-gradient text-white"><f.icon className="size-6" /></span>
              <h3 className="mt-5 font-display text-xl font-semibold">{f.title}</h3>
              <p className="mt-2 text-sm text-muted-foreground">{f.text}</p>
              <span className="mt-4 inline-flex items-center gap-1 text-sm font-medium text-primary">Learn more <ArrowRight className="size-4 transition-transform group-hover:translate-x-1" /></span>
            </Link>
          ))}
        </div>
      </section>

      {/* Categories */}
      <section className="mx-auto max-w-7xl px-4 py-12">
        <h2 className="font-display text-2xl font-bold">Browse by category</h2>
        <div className="mt-6 flex flex-wrap gap-2.5">
          {CATEGORIES.map((c) => (
            <Link key={c.slug} to="/explore">
              <Badge variant="secondary" className="cursor-pointer rounded-full px-4 py-2 text-sm font-medium hover:bg-primary hover:text-primary-foreground">{c.name}</Badge>
            </Link>
          ))}
        </div>
      </section>

      {/* Trending */}
      <section className="mx-auto max-w-7xl px-4 py-12">
        <div className="flex items-end justify-between">
          <div>
            <Badge variant="pink" className="gap-1 rounded-full"><TrendingUp className="size-3" /> Trending now</Badge>
            <h2 className="mt-3 font-display text-2xl font-bold">Today's most-loved artworks</h2>
          </div>
          <Link to="/explore"><Button variant="ghost" size="sm">See all <ArrowRight className="size-4" /></Button></Link>
        </div>
        <div className="mt-6 grid grid-cols-2 gap-4 md:grid-cols-3 lg:grid-cols-6">
          {trending.map((p) => (
            <Link key={p.id} to="/pin/$pinId" params={{ pinId: p.id }} className="group">
              <div className="overflow-hidden rounded-2xl">
                <ArtTile seed={p.seed} className="aspect-[3/4] transition-transform duration-500 group-hover:scale-105" />
              </div>
              <p className="mt-2 truncate text-sm font-medium">{p.title}</p>
              <p className="text-xs text-muted-foreground">❤ {fmt(p.likes)}</p>
            </Link>
          ))}
        </div>
      </section>

      {/* Featured creators */}
      <section className="mx-auto max-w-7xl px-4 py-12">
        <div className="flex items-end justify-between">
          <h2 className="font-display text-2xl font-bold">Featured creators</h2>
          <Link to="/marketplace"><Button variant="ghost" size="sm">Explore all <ArrowRight className="size-4" /></Button></Link>
        </div>
        <div className="mt-6 grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
          {featured.map((c) => <CreatorCard key={c.id} creator={c} />)}
        </div>
      </section>

      {/* CTA */}
      <section className="mx-auto max-w-7xl px-4 py-16">
        <div className="relative overflow-hidden rounded-[2rem] brand-gradient p-10 text-center text-white md:p-16">
          <div className="pointer-events-none absolute inset-0 opacity-20">
            <ArtTile seed={7} rounded={false} />
          </div>
          <div className="relative">
            <ShieldCheck className="mx-auto size-10" />
            <h2 className="mx-auto mt-4 max-w-2xl font-display text-3xl font-extrabold md:text-4xl">Join 40,000+ creatives building their craft on DrawPin</h2>
            <p className="mx-auto mt-3 max-w-lg text-white/85">Create your free account, build boards, sell your work, and get discovered locally.</p>
            <div className="mt-8 flex justify-center gap-3">
              <Link to="/signup"><Button size="xl" variant="glass" className="rounded-full bg-white text-primary hover:bg-white/90">Sign up free</Button></Link>
              <Link to="/login"><Button size="xl" variant="glass" className="rounded-full border-white/40 text-white">Log in</Button></Link>
            </div>
          </div>
        </div>
      </section>

      <Footer />
    </div>
  );
}
