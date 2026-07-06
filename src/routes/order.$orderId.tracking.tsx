import { createFileRoute, Link, useParams } from "@tanstack/react-router";
import { Check, Clock, MessageSquare } from "lucide-react";
import { Button } from "@/components/ui/button";
import { AppShell } from "@/components/layout/AppShell";
import { ArtTile } from "@/components/art/ArtTile";
import { ORDERS } from "@/lib/mock-data";
import { cn } from "@/lib/utils";

export const Route = createFileRoute("/order/$orderId/tracking")({
  head: () => ({ meta: [{ title: "Track order — DrawPin" }, { name: "description", content: "Track your order progress in real time." }] }),
  component: Tracking,
});

const STEPS = [
  { title: "Order placed", desc: "Your payment is held securely in escrow.", time: "Jun 12, 10:02", done: true },
  { title: "Requirements confirmed", desc: "Creator reviewed your brief and started work.", time: "Jun 12, 14:20", done: true },
  { title: "In progress", desc: "First concepts are being developed.", time: "Jun 13, 09:00", done: true, current: true },
  { title: "Delivered for review", desc: "Review deliverables and request revisions.", time: "Est. Jun 16", done: false },
  { title: "Completed", desc: "Funds released to the creator.", time: "Est. Jun 18", done: false },
];

function Tracking() {
  const { orderId } = useParams({ from: "/order/$orderId/tracking" });
  const order = ORDERS.find((o) => o.id === orderId) ?? ORDERS[0];
  return (
    <AppShell>
      <div className="mx-auto max-w-2xl px-4 py-6">
        <h1 className="font-display text-2xl font-bold">Track order {order.id}</h1>
        <div className="mt-5 flex items-center gap-4 rounded-3xl border bg-card p-4">
          <ArtTile seed={order.service.seed} className="size-16 shrink-0" />
          <div className="flex-1"><p className="text-sm font-medium">{order.service.title}</p><p className="text-xs text-muted-foreground">by {order.service.creator.name}</p></div>
          <Link to="/messages/$chatId" params={{ chatId: "chat1" }}><Button variant="outline" size="sm" className="rounded-full"><MessageSquare className="size-4" /></Button></Link>
        </div>

        <div className="mt-8 relative pl-2">
          {STEPS.map((s, i) => (
            <div key={i} className="relative flex gap-4 pb-8 last:pb-0">
              {i < STEPS.length - 1 && <span className={cn("absolute left-[15px] top-8 h-full w-0.5", s.done ? "bg-primary" : "bg-border")} />}
              <div className={cn(
                "z-10 grid size-8 shrink-0 place-items-center rounded-full",
                s.done ? "brand-gradient text-white" : "border-2 bg-card text-muted-foreground",
                s.current && "ring-4 ring-primary/20",
              )}>
                {s.done ? <Check className="size-4" /> : <Clock className="size-4" />}
              </div>
              <div className="pt-0.5">
                <p className={cn("font-medium", s.current && "text-primary")}>{s.title}</p>
                <p className="text-sm text-muted-foreground">{s.desc}</p>
                <p className="mt-0.5 text-xs text-muted-foreground">{s.time}</p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </AppShell>
  );
}
