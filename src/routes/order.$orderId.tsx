import { createFileRoute, Link, useParams } from "@tanstack/react-router";
import { ArrowLeft, MessageSquare, Download, MapPin } from "lucide-react";
import { Button } from "@/components/ui/button";
import { AppShell } from "@/components/layout/AppShell";
import { ArtTile } from "@/components/art/ArtTile";
import { GradientAvatar } from "@/components/art/GradientAvatar";
import { StatusBadge } from "@/components/shared/StatusBadge";
import { ORDERS } from "@/lib/mock-data";

export const Route = createFileRoute("/order/$orderId")({
  head: () => ({ meta: [{ title: "Order details — DrawPin" }, { name: "description", content: "View order details and deliverables." }] }),
  component: OrderDetails,
});

function OrderDetails() {
  const { orderId } = useParams({ from: "/order/$orderId" });
  const order = ORDERS.find((o) => o.id === orderId) ?? ORDERS[0];
  const fee = Math.round(order.total * 0.05);
  return (
    <AppShell>
      <div className="mx-auto max-w-4xl px-4 py-6">
        <Link to="/orders" className="mb-4 inline-flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground"><ArrowLeft className="size-4" /> Back to orders</Link>
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div><h1 className="font-display text-2xl font-bold">Order {order.id}</h1><p className="text-muted-foreground">Placed on {order.date}</p></div>
          <StatusBadge status={order.status} />
        </div>

        <div className="mt-6 grid gap-6 lg:grid-cols-[1.6fr_1fr]">
          <div className="space-y-6">
            <div className="rounded-3xl border bg-card p-5">
              <div className="flex gap-4">
                <ArtTile seed={order.service.seed} className="size-20 shrink-0" />
                <div className="flex-1"><p className="font-medium">{order.service.title}</p>
                  <div className="mt-1 flex items-center gap-2 text-sm text-muted-foreground"><GradientAvatar seed={order.service.creator.seed} name={order.service.creator.name} className="size-5 text-[8px]" /> {order.service.creator.name}</div>
                  <p className="mt-2 text-sm text-muted-foreground">Delivery in {order.service.delivery}</p>
                </div>
              </div>
            </div>
            <div className="rounded-3xl border bg-card p-5">
              <h2 className="font-display font-semibold">Deliverables</h2>
              <div className="mt-3 grid grid-cols-3 gap-3">
                {[1, 2, 3].map((n) => (
                  <div key={n} className="group relative overflow-hidden rounded-xl">
                    <ArtTile seed={order.service.seed + n * 7} className="aspect-square" />
                    <button className="absolute inset-0 grid place-items-center bg-black/40 opacity-0 transition-opacity group-hover:opacity-100"><Download className="size-5 text-white" /></button>
                  </div>
                ))}
              </div>
            </div>
          </div>

          <div className="space-y-4">
            <div className="rounded-3xl border bg-card p-5">
              <h2 className="font-display font-semibold">Payment</h2>
              <div className="mt-3 space-y-2 text-sm">
                <div className="flex justify-between"><span className="text-muted-foreground">Package</span><span>${order.total}</span></div>
                <div className="flex justify-between"><span className="text-muted-foreground">Service fee</span><span>${fee}</span></div>
                <div className="flex justify-between border-t pt-2 font-bold"><span>Total</span><span>${order.total + fee}</span></div>
              </div>
            </div>
            <Link to="/order/$orderId/tracking" params={{ orderId: order.id }}><Button variant="brand" className="w-full rounded-xl"><MapPin className="size-4" /> Track progress</Button></Link>
            <Link to="/messages/$chatId" params={{ chatId: "chat1" }}><Button variant="outline" className="w-full rounded-xl"><MessageSquare className="size-4" /> Message creator</Button></Link>
          </div>
        </div>
      </div>
    </AppShell>
  );
}
