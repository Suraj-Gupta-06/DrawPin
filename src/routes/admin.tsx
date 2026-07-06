import { createFileRoute, Link } from "@tanstack/react-router";
import { Users, ShoppingBag, DollarSign, Flag, ArrowUpRight, AlertTriangle } from "lucide-react";
import { Button } from "@/components/ui/button";
import { DashboardShell, StatCard } from "@/components/layout/DashboardShell";
import { Sparkline, BarMini } from "@/components/shared/charts";
import { GradientAvatar } from "@/components/art/GradientAvatar";
import { StatusBadge } from "@/components/shared/StatusBadge";
import { CREATORS, REVENUE_SERIES, VISITS_SERIES } from "@/lib/mock-data";

export const Route = createFileRoute("/admin")({
  head: () => ({ meta: [{ title: "Admin console — DrawPin" }, { name: "description", content: "Platform overview, moderation and management." }] }),
  component: Admin,
});

function Admin() {
  return (
    <DashboardShell variant="admin" title="Admin overview">
      <div className="space-y-6">
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <StatCard label="Total users" value="142,830" delta="+5.2%" icon={Users}><Sparkline data={VISITS_SERIES} /></StatCard>
          <StatCard label="Active orders" value="3,210" delta="+12%" icon={ShoppingBag}><Sparkline data={REVENUE_SERIES} stroke="var(--cyan)" /></StatCard>
          <StatCard label="GMV (30d)" value="$1.24M" delta="+9%" icon={DollarSign}><Sparkline data={REVENUE_SERIES} stroke="var(--pink)" /></StatCard>
          <StatCard label="Open reports" value="12" delta="urgent" icon={Flag}><Sparkline data={[2,4,3,6,5,8,7,9,11,10,12,12]} stroke="var(--destructive)" /></StatCard>
        </div>

        <div className="grid gap-4 lg:grid-cols-[1.6fr_1fr]">
          <div className="rounded-2xl border bg-card p-5">
            <h3 className="font-display font-semibold">Platform activity</h3>
            <div className="mt-4"><BarMini data={VISITS_SERIES} className="h-44" /></div>
          </div>
          <div className="rounded-2xl border border-warning/30 bg-warning/5 p-5">
            <div className="flex items-center gap-2 text-warning"><AlertTriangle className="size-5" /><h3 className="font-display font-semibold">Needs attention</h3></div>
            <div className="mt-4 space-y-2 text-sm">
              <Link to="/admin/reports" className="flex items-center justify-between rounded-xl bg-card p-3 hover:bg-muted"><span>12 content reports</span><ArrowUpRight className="size-4" /></Link>
              <Link to="/admin/disputes" className="flex items-center justify-between rounded-xl bg-card p-3 hover:bg-muted"><span>4 payment disputes</span><ArrowUpRight className="size-4" /></Link>
              <Link to="/admin/users" className="flex items-center justify-between rounded-xl bg-card p-3 hover:bg-muted"><span>7 verification requests</span><ArrowUpRight className="size-4" /></Link>
            </div>
          </div>
        </div>

        <div className="rounded-2xl border bg-card p-5">
          <div className="flex items-center justify-between"><h3 className="font-display font-semibold">New signups</h3><Link to="/admin/users"><Button variant="ghost" size="sm">View all <ArrowUpRight className="size-4" /></Button></Link></div>
          <div className="mt-4 grid gap-2 sm:grid-cols-2">
            {CREATORS.slice(0, 6).map((c) => (
              <div key={c.id} className="flex items-center gap-3 rounded-xl p-2 hover:bg-muted">
                <GradientAvatar seed={c.seed} name={c.name} className="size-9 text-xs" />
                <div className="min-w-0 flex-1"><p className="truncate text-sm font-medium">{c.name}</p><p className="text-xs text-muted-foreground">@{c.handle}</p></div>
                <StatusBadge status={c.level === "New" ? "Pending" : "Completed"} />
              </div>
            ))}
          </div>
        </div>
      </div>
    </DashboardShell>
  );
}
