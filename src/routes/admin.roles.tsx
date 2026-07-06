import { createFileRoute } from "@tanstack/react-router";
import { Plus, Shield, Check, Pencil } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Switch } from "@/components/ui/switch";
import { DashboardShell } from "@/components/layout/DashboardShell";
import { GradientAvatar } from "@/components/art/GradientAvatar";
import { CREATORS } from "@/lib/mock-data";

export const Route = createFileRoute("/admin/roles")({
  head: () => ({ meta: [{ title: "Role management — DrawPin" }, { name: "description", content: "Manage roles and permissions." }] }),
  component: Roles,
});

const ROLES = [
  { name: "Super Admin", color: "brand", count: 3, desc: "Full platform access" },
  { name: "Moderator", color: "cyan", count: 18, desc: "Content & report moderation" },
  { name: "Creator", color: "pink", count: 12840, desc: "Sell services & post art" },
  { name: "User", color: "secondary", count: 129960, desc: "Browse, save & follow" },
] as const;

const PERMS = ["View dashboard", "Moderate content", "Manage users", "Process disputes", "Manage roles", "Access billing", "Suspend accounts", "Edit platform settings"];

function Roles() {
  return (
    <DashboardShell variant="admin" title="Role management">
      <div className="space-y-6">
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {ROLES.map((r) => (
            <div key={r.name} className="rounded-2xl border bg-card p-5">
              <div className="flex items-center justify-between">
                <Badge variant={r.color as any} className="gap-1"><Shield className="size-3" /> {r.name}</Badge>
                <Button variant="ghost" size="icon"><Pencil className="size-4" /></Button>
              </div>
              <p className="mt-3 font-display text-2xl font-bold">{r.count.toLocaleString()}</p>
              <p className="text-sm text-muted-foreground">{r.desc}</p>
            </div>
          ))}
        </div>

        <div className="grid gap-4 lg:grid-cols-[1fr_1.4fr]">
          <div className="rounded-2xl border bg-card p-5">
            <div className="flex items-center justify-between"><h3 className="font-display font-semibold">Permissions — Moderator</h3></div>
            <div className="mt-4 space-y-1">
              {PERMS.map((p, i) => (
                <label key={p} className="flex items-center justify-between rounded-xl p-2.5 hover:bg-muted">
                  <span className="text-sm">{p}</span>
                  <Switch defaultChecked={i < 4} />
                </label>
              ))}
            </div>
            <Button variant="brand" className="mt-4 w-full rounded-xl"><Check className="size-4" /> Save permissions</Button>
          </div>

          <div className="rounded-2xl border bg-card p-5">
            <div className="flex items-center justify-between"><h3 className="font-display font-semibold">Team members</h3><Button variant="outline" size="sm" className="rounded-full"><Plus className="size-4" /> Invite</Button></div>
            <div className="mt-4 space-y-2">
              {CREATORS.slice(0, 6).map((c, i) => (
                <div key={c.id} className="flex items-center gap-3 rounded-xl p-2 hover:bg-muted">
                  <GradientAvatar seed={c.seed} name={c.name} className="size-9 text-xs" />
                  <div className="min-w-0 flex-1"><p className="truncate text-sm font-medium">{c.name}</p><p className="text-xs text-muted-foreground">@{c.handle}</p></div>
                  <Badge variant={i === 0 ? "brand" : "cyan"}>{i === 0 ? "Super Admin" : "Moderator"}</Badge>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </DashboardShell>
  );
}
