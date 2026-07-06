import { createFileRoute } from "@tanstack/react-router";
import { Search as SearchIcon, X } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs";
import { AppShell } from "@/components/layout/AppShell";
import { MasonryGrid } from "@/components/shared/MasonryGrid";
import { CreatorCard, ServiceCard } from "@/components/shared/cards";
import { PINS, CREATORS, SERVICES } from "@/lib/mock-data";

export const Route = createFileRoute("/search")({
  head: () => ({ meta: [{ title: "Search results — DrawPin" }, { name: "description", content: "Search artworks, creators and services on DrawPin." }] }),
  component: SearchResults,
});

function SearchResults() {
  return (
    <AppShell>
      <div className="mx-auto max-w-[1600px] px-4 py-6">
        <div className="relative mx-auto max-w-2xl">
          <SearchIcon className="pointer-events-none absolute left-4 top-1/2 size-5 -translate-y-1/2 text-muted-foreground" />
          <Input defaultValue="abstract gradient" className="h-12 rounded-full pl-11 pr-11 text-base" />
          <X className="absolute right-4 top-1/2 size-5 -translate-y-1/2 cursor-pointer text-muted-foreground" />
        </div>
        <div className="mt-4 flex flex-wrap justify-center gap-2">
          {["Trending", "Most saved", "Newest", "3D", "Editorial"].map((t) => (
            <Badge key={t} variant="secondary" className="cursor-pointer rounded-full px-3 py-1.5">{t}</Badge>
          ))}
        </div>

        <Tabs defaultValue="pins" className="mt-8">
          <TabsList className="mx-auto">
            <TabsTrigger value="pins">Pins · {PINS.length}</TabsTrigger>
            <TabsTrigger value="creators">Creators · {CREATORS.length}</TabsTrigger>
            <TabsTrigger value="services">Services · {SERVICES.length}</TabsTrigger>
          </TabsList>
          <TabsContent value="pins" className="mt-6"><MasonryGrid pins={PINS} /></TabsContent>
          <TabsContent value="creators" className="mt-6">
            <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
              {CREATORS.map((c) => <CreatorCard key={c.id} creator={c} />)}
            </div>
          </TabsContent>
          <TabsContent value="services" className="mt-6">
            <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
              {SERVICES.map((s) => <ServiceCard key={s.id} service={s} />)}
            </div>
          </TabsContent>
        </Tabs>
      </div>
    </AppShell>
  );
}
