import { createFileRoute } from "@tanstack/react-router";
import { Eye, Trash2, ShieldAlert, Filter } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { DashboardShell } from "@/components/layout/DashboardShell";
import { ArtTile } from "@/components/art/ArtTile";
import { PINS } from "@/lib/mock-data";

export const Route = createFileRoute("/admin/content")({
  head: () => ({ meta: [{ title: "Content moderation — DrawPin" }, { name: "description", content: "Moderate platform content with AI flags." }] }),
  component: ContentModeration,
});

function ContentModeration() {
  return (
    <DashboardShell variant="admin" title="Content moderation">
      <div className="flex items-center justify-between">
        <div className="flex gap-2">
          {["All", "AI-flagged", "Manual review", "Approved"].map((f, i) => (
            <Badge key={f} variant={i === 1 ? "brand" : "secondary"} className="cursor-pointer rounded-full px-3 py-1.5">{f}</Badge>
          ))}
        </div>
        <Button variant="outline" size="sm" className="rounded-full"><Filter className="size-4" /> Filter</Button>
      </div>
      <div className="mt-5 grid gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
        {PINS.slice(0, 12).map((p, i) => {
          const flagged = i % 3 === 0;
          return (
            <div key={p.id} className="overflow-hidden rounded-2xl border bg-card">
              <div className="relative">
                <ArtTile seed={p.seed} rounded={false} className="aspect-square" />
                {flagged && <Badge variant="destructive" className="absolute left-2 top-2 gap-1"><ShieldAlert className="size-3" /> AI flag {Math.round(70 + (p.seed % 28))}%</Badge>}
              </div>
              <div className="p-3">
                <p className="truncate text-sm font-medium">{p.title}</p>
                <p className="text-xs text-muted-foreground">{p.author.name}</p>
                <div className="mt-2 flex gap-1.5">
                  <Button variant="outline" size="sm" className="flex-1 px-0"><Eye className="size-4" /></Button>
                  <Button variant="destructive" size="sm" className="flex-1 px-0"><Trash2 className="size-4" /></Button>
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </DashboardShell>
  );
}
