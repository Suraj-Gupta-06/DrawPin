import { createFileRoute, Link } from "@tanstack/react-router";
import { DollarSign, Eye, ShoppingBag, Star, ArrowUpRight, MoreHorizontal, Plus } from "lucide-react";
import { Button } from "@/components/ui/button";
import { DashboardShell, StatCard } from "@/components/layout/DashboardShell";
import { Sparkline, BarMini, DonutChart } from "@/components/shared/charts";
import { GradientAvatar } from "@/components/art/GradientAvatar";
import { ArtTile } from "@/components/art/ArtTile";
import { StatusBadge } from "@/components/shared/StatusBadge";
import { ORDERS, REVENUE_SERIES, VISITS_SERIES, fmt } from "@/lib/mock-data";

export const Route = createFileRoute("/dashboard")({
  head: () => ({ meta: [{ title: "Creator dashboard — DrawPin" }, { name: "description", content: "Your creator studio overview, revenue and analytics." }] }),
  component: Dashboard,
});

function Dashboard() {
  return (
    <DashboardShell title="Dashboard">
      <div className="space-y-6">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div><h2 className="font-display text-xl font-bold">Welcome back, Aria 👋</h2><p className="text-sm text-muted-foreground">Here's how your studio is performing this month.</p></div>
          <Link to="/create-service"><Button variant="brand" className="rounded-full"><Plus className="size-4" /> New service</Button></Link>
        </div>

        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <StatCard label="Revenue" value="$12,480" delta="+18%" icon={DollarSign}><Sparkline data={REVENUE_SERIES} /></StatCard>
          <StatCard label="Profile views" value="48.2k" delta="+9%" icon={Eye}><Sparkline data={VISITS_SERIES} stroke="var(--pink)" /></StatCard>
          <StatCard label="Active orders" value="14" delta="+3" icon={ShoppingBag}><Sparkline data={[4,6,5,8,7,9,11,10,12,11,13,14]} stroke="var(--cyan)" /></StatCard>
          <StatCard label="Avg. rating" value="4.9" delta="+0.1" icon={Star}><Sparkline data={[4.5,4.6,4.6,4.7,4.8,4.8,4.9,4.9,4.9,4.9,4.9,4.9]} stroke="var(--warning)" /></StatCard>
        </div>

        <div className="grid gap-4 lg:grid-cols-[1.6fr_1fr]">
          <div className="rounded-2xl border bg-card p-5">
            <div className="flex items-center justify-between"><h3 className="font-display font-semibold">Revenue overview</h3><Button variant="ghost" size="icon"><MoreHorizontal className="size-4" /></Button></div>
            <div className="mt-4"><BarMini data={REVENUE_SERIES} className="h-40" /></div>
            <div className="mt-2 flex justify-between text-xs text-muted-foreground"><span>Jan</span><span>Apr</span><span>Aug</span><span>Dec</span></div>
          </div>
          <div className="rounded-2xl border bg-card p-5">
            <h3 className="font-display font-semibold">Traffic sources</h3>
            <div className="mt-4 flex items-center gap-6">
              <DonutChart segments={[
                { value: 48, color: "var(--brand)", label: "Search" },
                { value: 32, color: "var(--pink)", label: "Direct" },
                { value: 20, color: "var(--cyan)", label: "Social" },
              ]} />
              <div className="space-y-2 text-sm">
                {[["Search", "var(--brand)", "48%"], ["Direct", "var(--pink)", "32%"], ["Social", "var(--cyan)", "20%"]].map(([l, c, v]) => (
                  <div key={l} className="flex items-center gap-2"><span className="size-3 rounded-full" style={{ background: c }} /><span className="flex-1">{l}</span><span className="font-medium">{v}</span></div>
                ))}
              </div>
            </div>
          </div>
        </div>

        <div className="rounded-2xl border bg-card p-5">
          <div className="flex items-center justify-between"><h3 className="font-display font-semibold">Recent orders</h3><Link to="/orders"><Button variant="ghost" size="sm">View all <ArrowUpRight className="size-4" /></Button></Link></div>
          <div className="mt-4 space-y-2">
            {ORDERS.slice(0, 4).map((o) => (
              <Link key={o.id} to="/order/$orderId" params={{ orderId: o.id }} className="flex items-center gap-3 rounded-xl p-2 transition-colors hover:bg-muted">
                <ArtTile seed={o.service.seed} className="size-11 shrink-0" />
                <div className="min-w-0 flex-1"><p className="truncate text-sm font-medium">{o.service.title}</p><p className="text-xs text-muted-foreground">{o.id} · {o.date}</p></div>
                <StatusBadge status={o.status} />
                <span className="font-semibold">${o.total}</span>
              </Link>
            ))}
          </div>
        </div>
      </div>
    </DashboardShell>
  );
}
