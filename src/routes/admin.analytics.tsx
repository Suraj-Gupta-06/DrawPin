import { createFileRoute } from "@tanstack/react-router";
import { Users, ShoppingBag, DollarSign, Activity } from "lucide-react";
import { DashboardShell, StatCard } from "@/components/layout/DashboardShell";
import { BarMini, Sparkline, DonutChart } from "@/components/shared/charts";
import { REVENUE_SERIES, VISITS_SERIES, CATEGORIES } from "@/lib/mock-data";

export const Route = createFileRoute("/admin/analytics")({
  head: () => ({ meta: [{ title: "Analytics overview — DrawPin" }, { name: "description", content: "Platform-wide analytics and KPIs." }] }),
  component: AdminAnalytics,
});

function AdminAnalytics() {
  return (
    <DashboardShell variant="admin" title="Analytics overview">
      <div className="space-y-6">
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <StatCard label="DAU" value="38,210" delta="+6%" icon={Users}><Sparkline data={VISITS_SERIES} /></StatCard>
          <StatCard label="Orders / day" value="1,042" delta="+11%" icon={ShoppingBag}><Sparkline data={REVENUE_SERIES} stroke="var(--cyan)" /></StatCard>
          <StatCard label="Revenue / day" value="$84.2k" delta="+8%" icon={DollarSign}><Sparkline data={REVENUE_SERIES} stroke="var(--pink)" /></StatCard>
          <StatCard label="Retention" value="62%" delta="+2%" icon={Activity}><Sparkline data={[55,56,58,57,59,60,61,60,62,61,62,62]} stroke="var(--warning)" /></StatCard>
        </div>
        <div className="grid gap-4 lg:grid-cols-[1.6fr_1fr]">
          <div className="rounded-2xl border bg-card p-5"><h3 className="font-display font-semibold">Growth (12 months)</h3><div className="mt-4"><BarMini data={VISITS_SERIES} className="h-52" /></div></div>
          <div className="rounded-2xl border bg-card p-5">
            <h3 className="font-display font-semibold">Top categories</h3>
            <div className="mt-4 space-y-3">
              {CATEGORIES.slice(0, 6).map((c, i) => {
                const v = 90 - i * 13;
                return <div key={c.slug}><div className="flex justify-between text-sm"><span>{c.name}</span><span className="text-muted-foreground">{v}%</span></div><div className="mt-1 h-2 rounded-full bg-muted"><div className="h-full rounded-full brand-gradient" style={{ width: `${v}%` }} /></div></div>;
              })}
            </div>
          </div>
        </div>
      </div>
    </DashboardShell>
  );
}
