import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { CreditCard, Lock, ShieldCheck } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { AppShell } from "@/components/layout/AppShell";
import { SERVICES } from "@/lib/mock-data";

export const Route = createFileRoute("/payment")({
  head: () => ({ meta: [{ title: "Payment — DrawPin" }, { name: "description", content: "Complete your secure payment." }] }),
  component: Payment,
});

function Payment() {
  const navigate = useNavigate();
  const service = SERVICES[0];
  const total = service.price + Math.round(service.price * 0.05);
  return (
    <AppShell>
      <div className="mx-auto max-w-5xl px-4 py-8">
        <div className="grid gap-6 lg:grid-cols-[1.5fr_1fr]">
          <form className="rounded-3xl border bg-card p-6" onSubmit={(e) => { e.preventDefault(); navigate({ to: "/order-confirmation" }); }}>
            <h2 className="font-display text-lg font-semibold">Payment method</h2>
            <div className="mt-4 grid grid-cols-3 gap-3">
              {["Card", "PayPal", "Apple Pay"].map((m, i) => (
                <button key={m} type="button" className={`rounded-xl border p-3 text-sm font-medium ${i === 0 ? "border-primary bg-primary/5" : ""}`}>{m}</button>
              ))}
            </div>
            <div className="mt-5 space-y-4">
              <div className="space-y-1.5"><Label>Card number</Label>
                <div className="relative"><CreditCard className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" /><Input className="h-11 pl-9" placeholder="4242 4242 4242 4242" /></div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-1.5"><Label>Expiry</Label><Input className="h-11" placeholder="MM / YY" /></div>
                <div className="space-y-1.5"><Label>CVC</Label><Input className="h-11" placeholder="123" /></div>
              </div>
              <div className="space-y-1.5"><Label>Name on card</Label><Input className="h-11" defaultValue="Aria Vance" /></div>
            </div>
            <Button type="submit" variant="brand" className="mt-6 h-11 w-full rounded-xl"><Lock className="size-4" /> Pay ${total}</Button>
            <p className="mt-3 flex items-center justify-center gap-1.5 text-xs text-muted-foreground"><ShieldCheck className="size-4 text-success" /> Payments are encrypted and held in escrow</p>
          </form>

          <div className="rounded-3xl border bg-card p-6 lg:sticky lg:top-20 lg:self-start">
            <h2 className="font-display text-lg font-semibold">Summary</h2>
            <div className="mt-4 space-y-2 text-sm">
              <div className="flex justify-between"><span className="text-muted-foreground">Package</span><span>${service.price}</span></div>
              <div className="flex justify-between"><span className="text-muted-foreground">Service fee</span><span>${Math.round(service.price * 0.05)}</span></div>
              <div className="flex justify-between border-t pt-2 font-display text-lg font-bold"><span>Total</span><span>${total}</span></div>
            </div>
            <Link to="/checkout"><Button variant="ghost" size="sm" className="mt-4 w-full">Back to checkout</Button></Link>
          </div>
        </div>
      </div>
    </AppShell>
  );
}
