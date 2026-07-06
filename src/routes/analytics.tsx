import { createFileRoute } from "@tanstack/react-router";
import { Eye, Heart, Users, TrendingUp } from "lucide-react";
import { DashboardShell, StatCard } from "@/components/layout/DashboardShell";
import { Sparkline, BarMini, DonutChart } from "@/components/shared/charts";
import { REVENUE_SERIES, VISITS_SERIES, PINS, fmt } from "@/lib/mock-data";
import { ArtTile } from "@/components/art/ArtTile";

export const Route = createFileRoute("/analytics")({
  head: () => ({ meta: [{ title: "Analytics — DrawPin" }, { name: "description", content: "Deep-dive analytics for your creator studio." }] }),
  component: Analytics,
});

function Analytics() {
  const top = PINS.slice(0, 5);
  return (
    <DashboardShell title="Analytics">
      <div className="space-y-6">
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <StatCard label="Impressions" value="284k" delta="+22%" icon={Eye}><Sparkline data={VISITS_SERIES} /></StatCard>
          <StatCard label="Engagements" value="58.1k" delta="+14%" icon={Heart}><Sparkline data={REVENUE_SERIES} stroke="var(--pink)" /></StatCard>
          <StatCard label="New followers" value="2,940" delta="+8%" icon={Users}><Sparkline data={[120,180,150,220,280,260,310,380,350,420,480,540].map(n=>n/10)} stroke="var(--cyan)" /></StatCard>
          <StatCard label="Conversion" value="4.8%" delta="+0.6%" icon={TrendingUp}><Sparkline data={[2,2.5,3,3.2,3.8,4,4.2,4.5,4.4,4.6,4.7,4.8]} stroke="var(--warning)" /></StatCard>
        </div>

        <div className="grid gap-4 lg:grid-cols-[1.6fr_1fr]">
          <div className="rounded-2xl border bg-card p-5">
            <h3 className="font-display font-semibold">Impressions over time</h3>
            <div className="mt-4"><BarMini data={VISITS_SERIES} className="h-48" /></div>
          </div>
          <div className="rounded-2xl border bg-card p-5">
            <h3 className="font-display font-semibold">Audience by region</h3>
            <div className="mt-4 space-y-3">
              {[["Europe", 42], ["North America", 28], ["Asia", 18], ["Other", 12]].map(([r, v]) => (
                <div key={r as string}><div className="flex justify-between text-sm"><span>{r}</span><span className="text-muted-foreground">{v}%</span></div>
                  <div className="mt-1 h-2 rounded-full bg-muted"><div className="h-full rounded-full brand-gradient" style={{ width: `${v}%` }} /></div>
                </div>
              ))}
            </div>
          </div>
        </div>

        <div className="rounded-2xl border bg-card p-5">
          <h3 className="font-display font-semibold">Top performing pins</h3>
          <div className="mt-4 space-y-2">
            {top.map((p, i) => (
              <div key={p.id} className="flex items-center gap-3 rounded-xl p-2 hover:bg-muted">
                <span className="w-5 text-center font-display font-bold text-muted-foreground">{i + 1}</span>
                <ArtTile seed={p.seed} className="size-11 shrink-0" />
                <div className="min-w-0 flex-1"><p className="truncate text-sm font-medium">{p.title}</p><p className="text-xs text-muted-foreground">{fmt(p.likes)} likes · {fmt(p.saves)} saves</p></div>
                <Sparkline data={REVENUE_SERIES.slice(i, i + 8)} className="w-24" />
              </div>
            ))}
          </div>
        </div>
      </div>
    </DashboardShell>
  );
}
