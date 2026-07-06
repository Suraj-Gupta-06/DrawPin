import { createFileRoute, Link } from "@tanstack/react-router";
import { Plus, Pencil, Trash2, Eye, GripVertical } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs";
import { DashboardShell } from "@/components/layout/DashboardShell";
import { ArtTile } from "@/components/art/ArtTile";
import { StatusBadge } from "@/components/shared/StatusBadge";
import { PINS, SERVICES, fmt } from "@/lib/mock-data";

export const Route = createFileRoute("/portfolio-manager")({
  head: () => ({ meta: [{ title: "Portfolio manager — DrawPin" }, { name: "description", content: "Manage your portfolio pieces and services." }] }),
  component: PortfolioManager,
});

function PortfolioManager() {
  return (
    <DashboardShell title="Portfolio">
      <Tabs defaultValue="works">
        <div className="flex items-center justify-between">
          <TabsList><TabsTrigger value="works">Works</TabsTrigger><TabsTrigger value="services">Services</TabsTrigger></TabsList>
          <div className="flex gap-2">
            <Link to="/create"><Button variant="outline" className="rounded-full"><Plus className="size-4" /> Add work</Button></Link>
            <Link to="/create-service"><Button variant="brand" className="rounded-full"><Plus className="size-4" /> New service</Button></Link>
          </div>
        </div>

        <TabsContent value="works" className="mt-6">
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
            {PINS.slice(0, 12).map((p) => (
              <div key={p.id} className="group overflow-hidden rounded-2xl border bg-card">
                <div className="relative">
                  <ArtTile seed={p.seed} rounded={false} className="aspect-[4/3]" />
                  <div className="absolute inset-0 flex items-center justify-center gap-2 bg-black/50 opacity-0 transition-opacity group-hover:opacity-100">
                    <Button variant="glass" size="icon" className="rounded-full"><Pencil className="size-4" /></Button>
                    <Button variant="glass" size="icon" className="rounded-full"><Eye className="size-4" /></Button>
                    <Button variant="glass" size="icon" className="rounded-full text-destructive"><Trash2 className="size-4" /></Button>
                  </div>
                </div>
                <div className="p-3"><p className="truncate text-sm font-medium">{p.title}</p><p className="text-xs text-muted-foreground">{fmt(p.likes)} likes</p></div>
              </div>
            ))}
          </div>
        </TabsContent>

        <TabsContent value="services" className="mt-6">
          <div className="space-y-2">
            {SERVICES.map((s, i) => (
              <div key={s.id} className="flex items-center gap-3 rounded-2xl border bg-card p-3">
                <GripVertical className="size-4 shrink-0 cursor-grab text-muted-foreground" />
                <ArtTile seed={s.seed} className="size-12 shrink-0" />
                <div className="min-w-0 flex-1"><p className="truncate text-sm font-medium">{s.title}</p><p className="text-xs text-muted-foreground">${s.price} · {s.reviews} reviews</p></div>
                <StatusBadge status={i % 4 === 0 ? "Pending" : "Completed"} />
                <Link to="/edit-service/$serviceId" params={{ serviceId: s.id }}><Button variant="ghost" size="icon"><Pencil className="size-4" /></Button></Link>
                <Button variant="ghost" size="icon" className="text-destructive"><Trash2 className="size-4" /></Button>
              </div>
            ))}
          </div>
        </TabsContent>
      </Tabs>
    </DashboardShell>
  );
}
