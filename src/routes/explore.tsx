import { createFileRoute, Link } from "@tanstack/react-router";
import { ArrowRight } from "lucide-react";
import { AppShell } from "@/components/layout/AppShell";
import { FilterBar } from "@/components/shared/FilterBar";
import { MasonryGrid } from "@/components/shared/MasonryGrid";
import { ArtTile } from "@/components/art/ArtTile";
import { CATEGORIES, PINS } from "@/lib/mock-data";

export const Route = createFileRoute("/explore")({
  head: () => ({ meta: [{ title: "Explore — DrawPin" }, { name: "description", content: "Explore art categories and trending collections." }] }),
  component: Explore,
});

function Explore() {
  return (
    <AppShell>
      <div className="mx-auto max-w-[1600px] px-4 py-6">
        <h1 className="font-display text-2xl font-bold">Explore</h1>
        <p className="mt-1 text-muted-foreground">Discover collections curated across every creative discipline.</p>

        <div className="mt-6 grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-5">
          {CATEGORIES.map((c, i) => (
            <Link key={c.slug} to="/search" className="group relative overflow-hidden rounded-2xl">
              <ArtTile seed={(i + 1) * 21} className="aspect-square transition-transform duration-500 group-hover:scale-110" />
              <div className="absolute inset-0 bg-gradient-to-t from-black/70 to-transparent" />
              <div className="absolute inset-x-3 bottom-3 flex items-center justify-between text-white">
                <span className="font-display font-semibold">{c.name}</span>
                <ArrowRight className="size-4 transition-transform group-hover:translate-x-1" />
              </div>
            </Link>
          ))}
        </div>

        <div className="mt-10 -mx-4 mb-4 px-4">
          <FilterBar />
        </div>
        <MasonryGrid pins={PINS} />
      </div>
    </AppShell>
  );
}
