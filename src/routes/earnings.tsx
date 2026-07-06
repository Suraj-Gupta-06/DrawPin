import { createFileRoute } from "@tanstack/react-router";
import { Wallet, TrendingUp, Clock, Download, ArrowDownToLine } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from "@/components/ui/table";
import { DashboardShell, StatCard } from "@/components/layout/DashboardShell";
import { BarMini } from "@/components/shared/charts";
import { StatusBadge } from "@/components/shared/StatusBadge";
import { ORDERS, REVENUE_SERIES } from "@/lib/mock-data";

export const Route = createFileRoute("/earnings")({
  head: () => ({ meta: [{ title: "Earnings — DrawPin" }, { name: "description", content: "Track your earnings, payouts and transactions." }] }),
  component: Earnings,
});

function Earnings() {
  return (
    <DashboardShell title="Earnings">
      <div className="space-y-6">
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <StatCard label="Available balance" value="$4,820" icon={Wallet} />
          <StatCard label="Pending clearance" value="$1,240" icon={Clock} />
          <StatCard label="This month" value="$12,480" delta="+18%" icon={TrendingUp} />
          <StatCard label="Lifetime" value="$96,310" icon={Wallet} />
        </div>

        <div className="grid gap-4 lg:grid-cols-[1.6fr_1fr]">
          <div className="rounded-2xl border bg-card p-5">
            <h3 className="font-display font-semibold">Earnings trend</h3>
            <div className="mt-4"><BarMini data={REVENUE_SERIES} className="h-44" /></div>
          </div>
          <div className="rounded-2xl border bg-card p-5">
            <h3 className="font-display font-semibold">Withdraw funds</h3>
            <p className="mt-2 text-sm text-muted-foreground">Available to withdraw</p>
            <p className="font-display text-3xl font-bold">$4,820</p>
            <Button variant="brand" className="mt-4 w-full rounded-xl"><ArrowDownToLine className="size-4" /> Withdraw to bank</Button>
            <p className="mt-3 text-xs text-muted-foreground">Connected: •••• 4821 · Visa</p>
          </div>
        </div>

        <div className="rounded-2xl border bg-card p-5">
          <div className="flex items-center justify-between"><h3 className="font-display font-semibold">Transactions</h3><Button variant="outline" size="sm" className="rounded-full"><Download className="size-4" /> Export</Button></div>
          <div className="mt-4 overflow-hidden rounded-xl border">
            <Table>
              <TableHeader><TableRow><TableHead>Order</TableHead><TableHead>Date</TableHead><TableHead>Status</TableHead><TableHead className="text-right">Net</TableHead></TableRow></TableHeader>
              <TableBody>
                {ORDERS.map((o) => (
                  <TableRow key={o.id}>
                    <TableCell className="font-medium">{o.id}</TableCell>
                    <TableCell className="text-muted-foreground">{o.date}</TableCell>
                    <TableCell><StatusBadge status={o.status} /></TableCell>
                    <TableCell className="text-right font-semibold">+${Math.round(o.total * 0.9)}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        </div>
      </div>
    </DashboardShell>
  );
}
