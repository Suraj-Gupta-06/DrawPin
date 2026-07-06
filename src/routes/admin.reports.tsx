import { createFileRoute } from "@tanstack/react-router";
import { Check, X, Eye, Flag } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { DashboardShell } from "@/components/layout/DashboardShell";
import { ArtTile } from "@/components/art/ArtTile";
import { GradientAvatar } from "@/components/art/GradientAvatar";
import { PINS, CREATORS } from "@/lib/mock-data";

export const Route = createFileRoute("/admin/reports")({
  head: () => ({ meta: [{ title: "Report moderation — DrawPin" }, { name: "description", content: "Review and resolve reported content." }] }),
  component: Reports,
});

const REASONS = ["Copyright violation", "Inappropriate content", "Spam", "Misleading", "Harassment", "Copyright violation"];

function Reports() {
  return (
    <DashboardShell variant="admin" title="Report moderation">
      <Tabs defaultValue="pending">
        <TabsList><TabsTrigger value="pending">Pending · 12</TabsTrigger><TabsTrigger value="resolved">Resolved</TabsTrigger><TabsTrigger value="dismissed">Dismissed</TabsTrigger></TabsList>
      </Tabs>
      <div className="mt-5 grid gap-4 lg:grid-cols-2">
        {PINS.slice(0, 6).map((p, i) => (
          <div key={p.id} className="rounded-2xl border bg-card p-4">
            <div className="flex gap-4">
              <ArtTile seed={p.seed} className="size-24 shrink-0" />
              <div className="min-w-0 flex-1">
                <div className="flex items-start justify-between gap-2">
                  <p className="font-medium">{p.title}</p>
                  <Badge variant="destructive" className="shrink-0 gap-1"><Flag className="size-3" /> {REASONS[i]}</Badge>
                </div>
                <div className="mt-2 flex items-center gap-2 text-xs text-muted-foreground"><GradientAvatar seed={p.author.seed} name={p.author.name} className="size-5 text-[8px]" /> by {p.author.name}</div>
                <p className="mt-2 text-xs text-muted-foreground">Reported by {CREATORS[i + 2].name} · {i + 1} {i === 0 ? "hour" : "hours"} ago</p>
                <p className="mt-1 rounded-lg bg-muted p-2 text-xs">"This artwork appears to use my original work without credit."</p>
              </div>
            </div>
            <div className="mt-4 flex gap-2">
              <Button variant="outline" size="sm" className="flex-1"><Eye className="size-4" /> Review</Button>
              <Button variant="destructive" size="sm" className="flex-1"><X className="size-4" /> Remove</Button>
              <Button variant="brand" size="sm" className="flex-1"><Check className="size-4" /> Dismiss</Button>
            </div>
          </div>
        ))}
      </div>
    </DashboardShell>
  );
}
