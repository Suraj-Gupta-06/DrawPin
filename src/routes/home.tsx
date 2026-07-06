import { createFileRoute } from "@tanstack/react-router";
import { useState } from "react";
import { TrendingUp, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { AppShell } from "@/components/layout/AppShell";
import { FilterBar } from "@/components/shared/FilterBar";
import { MasonryGrid } from "@/components/shared/MasonryGrid";
import { PINS } from "@/lib/mock-data";

export const Route = createFileRoute("/home")({
  head: () => ({ meta: [{ title: "Home feed — DrawPin" }, { name: "description", content: "Your personalized art discovery feed." }] }),
  component: HomeFeed,
});

function HomeFeed() {
  const [count, setCount] = useState(24);
  const [loading, setLoading] = useState(false);
  const pins = [...PINS, ...PINS].slice(0, count);

  const loadMore = () => {
    setLoading(true);
    setTimeout(() => { setCount((c) => c + 12); setLoading(false); }, 600);
  };

  return (
    <AppShell hideFooter>
      <div className="mx-auto max-w-[1600px] px-4 py-6">
        <div className="sticky top-16 z-30 -mx-4 mb-4 bg-background/80 px-4 py-2 backdrop-blur">
          <FilterBar />
        </div>
        <div className="mb-6 flex items-center gap-2">
          <Badge variant="pink" className="gap-1 rounded-full"><TrendingUp className="size-3" /> Trending</Badge>
          <h1 className="font-display text-xl font-bold">Fresh for you today</h1>
        </div>
        <MasonryGrid pins={pins} />
        <div className="mt-8 flex justify-center">
          <Button variant="glass" size="lg" className="rounded-full" onClick={loadMore} disabled={loading}>
            {loading ? <><Loader2 className="size-4 animate-spin" /> Loading…</> : "Load more"}
          </Button>
        </div>
      </div>
    </AppShell>
  );
}
