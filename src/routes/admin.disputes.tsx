import { createFileRoute } from "@tanstack/react-router";
import { Scale, MessageSquare, Check, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { DashboardShell } from "@/components/layout/DashboardShell";
import { ArtTile } from "@/components/art/ArtTile";
import { GradientAvatar } from "@/components/art/GradientAvatar";
import { ORDERS } from "@/lib/mock-data";

export const Route = createFileRoute("/admin/disputes")({
  head: () => ({ meta: [{ title: "Payment disputes — DrawPin" }, { name: "description", content: "Resolve payment disputes between buyers and creators." }] }),
  component: Disputes,
});

const REASONS = ["Work not delivered", "Quality not as described", "Late delivery", "Unauthorized charge"];

function Disputes() {
  return (
    <DashboardShell variant="admin" title="Payment disputes">
      <Tabs defaultValue="open">
        <TabsList><TabsTrigger value="open">Open · 4</TabsTrigger><TabsTrigger value="resolved">Resolved</TabsTrigger></TabsList>
      </Tabs>
      <div className="mt-5 space-y-4">
        {ORDERS.slice(0, 4).map((o, i) => (
          <div key={o.id} className="rounded-2xl border bg-card p-5">
            <div className="flex flex-wrap items-start justify-between gap-3">
              <div className="flex gap-3">
                <ArtTile seed={o.service.seed} className="size-16 shrink-0" />
                <div>
                  <p className="font-medium">{o.service.title}</p>
                  <p className="text-xs text-muted-foreground">Order {o.id} · ${o.total}</p>
                  <Badge variant="destructive" className="mt-2 gap-1"><Scale className="size-3" /> {REASONS[i]}</Badge>
                </div>
              </div>
              <Badge variant="warning">Awaiting decision</Badge>
            </div>

            <div className="mt-4 grid gap-3 sm:grid-cols-2">
              <div className="rounded-xl bg-muted p-3">
                <div className="flex items-center gap-2 text-xs font-medium"><GradientAvatar seed={o.service.creator.seed + 1} name="Buyer" className="size-5 text-[8px]" /> Buyer claim</div>
                <p className="mt-1.5 text-sm text-muted-foreground">The delivered work did not match the agreed brief and revisions were not addressed.</p>
              </div>
              <div className="rounded-xl bg-muted p-3">
                <div className="flex items-center gap-2 text-xs font-medium"><GradientAvatar seed={o.service.creator.seed} name={o.service.creator.name} className="size-5 text-[8px]" /> Creator response</div>
                <p className="mt-1.5 text-sm text-muted-foreground">All deliverables were provided per the package scope; extra revisions were out of scope.</p>
              </div>
            </div>

            <div className="mt-4 flex flex-wrap gap-2">
              <Button variant="outline" size="sm"><MessageSquare className="size-4" /> View thread</Button>
              <Button variant="brand" size="sm"><Check className="size-4" /> Refund buyer</Button>
              <Button variant="outline" size="sm"><Check className="size-4" /> Release to creator</Button>
              <Button variant="ghost" size="sm" className="text-muted-foreground"><X className="size-4" /> Dismiss</Button>
            </div>
          </div>
        ))}
      </div>
    </DashboardShell>
  );
}
