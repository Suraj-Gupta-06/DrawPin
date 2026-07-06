import { createFileRoute, Link } from "@tanstack/react-router";
import { Check, Lock, ShieldCheck } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { AppShell } from "@/components/layout/AppShell";
import { ArtTile } from "@/components/art/ArtTile";
import { GradientAvatar } from "@/components/art/GradientAvatar";
import { SERVICES } from "@/lib/mock-data";

export const Route = createFileRoute("/checkout")({
  head: () => ({ meta: [{ title: "Checkout — DrawPin" }, { name: "description", content: "Review your order before payment." }] }),
  component: Checkout,
});

function Steps({ active }: { active: number }) {
  const steps = ["Cart", "Checkout", "Payment", "Done"];
  return (
    <div className="mx-auto flex max-w-md items-center justify-between">
      {steps.map((s, i) => (
        <div key={s} className="flex items-center">
          <div className="flex flex-col items-center">
            <div className={`grid size-8 place-items-center rounded-full text-sm font-semibold ${i <= active ? "brand-gradient text-white" : "bg-muted text-muted-foreground"}`}>
              {i < active ? <Check className="size-4" /> : i + 1}
            </div>
            <span className="mt-1 text-xs text-muted-foreground">{s}</span>
          </div>
          {i < steps.length - 1 && <div className={`mx-2 h-0.5 w-10 ${i < active ? "bg-primary" : "bg-muted"}`} />}
        </div>
      ))}
    </div>
  );
}

function Checkout() {
  const service = SERVICES[0];
  const price = service.price;
  const fee = Math.round(price * 0.05);
  return (
    <AppShell>
      <div className="mx-auto max-w-5xl px-4 py-8">
        <Steps active={1} />
        <div className="mt-8 grid gap-6 lg:grid-cols-[1.5fr_1fr]">
          <div className="space-y-6">
            <div className="rounded-3xl border bg-card p-6">
              <h2 className="font-display text-lg font-semibold">Project brief</h2>
              <div className="mt-4 space-y-1.5"><Label>Project title</Label><Input className="h-11" defaultValue="Brand illustration set for SaaS" /></div>
              <div className="mt-4 space-y-1.5"><Label>Requirements</Label><Textarea rows={4} placeholder="Share references, brand guidelines and goals…" /></div>
            </div>
            <div className="rounded-3xl border bg-card p-6">
              <h2 className="font-display text-lg font-semibold">Contact</h2>
              <div className="mt-4 grid gap-4 sm:grid-cols-2">
                <div className="space-y-1.5"><Label>Name</Label><Input className="h-11" defaultValue="Aria Vance" /></div>
                <div className="space-y-1.5"><Label>Email</Label><Input className="h-11" defaultValue="aria@example.com" /></div>
              </div>
            </div>
          </div>

          <div className="lg:sticky lg:top-20 lg:self-start">
            <div className="rounded-3xl border bg-card p-6">
              <h2 className="font-display text-lg font-semibold">Order summary</h2>
              <div className="mt-4 flex gap-3">
                <ArtTile seed={service.seed} className="size-16 shrink-0" />
                <div><p className="line-clamp-2 text-sm font-medium">{service.title}</p>
                  <div className="mt-1 flex items-center gap-1.5 text-xs text-muted-foreground"><GradientAvatar seed={service.creator.seed} name={service.creator.name} className="size-4 text-[8px]" /> {service.creator.name}</div>
                </div>
              </div>
              <div className="mt-4 space-y-2 border-t pt-4 text-sm">
                <div className="flex justify-between"><span className="text-muted-foreground">Standard package</span><span>${price}</span></div>
                <div className="flex justify-between"><span className="text-muted-foreground">Service fee</span><span>${fee}</span></div>
                <div className="flex justify-between border-t pt-2 font-display text-lg font-bold"><span>Total</span><span>${price + fee}</span></div>
              </div>
              <Link to="/payment"><Button variant="brand" className="mt-4 h-11 w-full rounded-xl"><Lock className="size-4" /> Continue to payment</Button></Link>
              <p className="mt-3 flex items-center justify-center gap-1.5 text-xs text-muted-foreground"><ShieldCheck className="size-4 text-success" /> Secure escrow protection</p>
            </div>
          </div>
        </div>
      </div>
    </AppShell>
  );
}
