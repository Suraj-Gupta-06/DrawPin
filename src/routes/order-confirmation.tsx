import { createFileRoute, Link } from "@tanstack/react-router";
import { CheckCircle2, ArrowRight, MessageSquare } from "lucide-react";
import { Button } from "@/components/ui/button";
import { AppShell } from "@/components/layout/AppShell";
import { ArtTile } from "@/components/art/ArtTile";
import { SERVICES } from "@/lib/mock-data";

export const Route = createFileRoute("/order-confirmation")({
  head: () => ({ meta: [{ title: "Order confirmed — DrawPin" }, { name: "description", content: "Your order has been placed successfully." }] }),
  component: Confirmation,
});

function Confirmation() {
  const service = SERVICES[0];
  return (
    <AppShell>
      <div className="mx-auto max-w-lg px-4 py-16 text-center">
        <div className="mx-auto grid size-20 place-items-center rounded-full bg-success/15 animate-scale-in">
          <CheckCircle2 className="size-12 text-success" />
        </div>
        <h1 className="mt-6 font-display text-3xl font-bold">Order confirmed!</h1>
        <p className="mt-2 text-muted-foreground">Order <span className="font-semibold text-foreground">#DP-4826</span> has been placed. The creator has been notified.</p>

        <div className="mt-8 rounded-3xl border bg-card p-5 text-left">
          <div className="flex gap-3">
            <ArtTile seed={service.seed} className="size-16 shrink-0" />
            <div className="flex-1">
              <p className="line-clamp-2 text-sm font-medium">{service.title}</p>
              <p className="mt-1 text-xs text-muted-foreground">by {service.creator.name} · {service.delivery}</p>
            </div>
            <span className="font-display font-bold">${service.price}</span>
          </div>
        </div>

        <div className="mt-6 flex flex-col gap-2 sm:flex-row">
          <Link to="/order/$orderId/tracking" params={{ orderId: "DP-4826" }} className="flex-1"><Button variant="brand" className="w-full rounded-xl">Track order <ArrowRight className="size-4" /></Button></Link>
          <Link to="/messages/$chatId" params={{ chatId: "chat1" }} className="flex-1"><Button variant="outline" className="w-full rounded-xl"><MessageSquare className="size-4" /> Message creator</Button></Link>
        </div>
        <Link to="/orders" className="mt-4 inline-block text-sm text-primary story-link">View all orders</Link>
      </div>
    </AppShell>
  );
}
