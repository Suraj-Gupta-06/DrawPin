import { useNavigate } from "@tanstack/react-router";
import { Plus, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from "@/components/ui/select";
import { AppShell } from "@/components/layout/AppShell";
import { CATEGORIES } from "@/lib/mock-data";

export function ServiceForm({ mode, defaults }: { mode: "create" | "edit"; defaults?: { title: string; price: string } }) {
  const navigate = useNavigate();
  const tiers = ["Basic", "Standard", "Premium"];
  return (
    <AppShell>
      <div className="mx-auto max-w-3xl px-4 py-6">
        <h1 className="font-display text-2xl font-bold">{mode === "create" ? "Create a service" : "Edit service"}</h1>
        <p className="mt-1 text-muted-foreground">{mode === "create" ? "List a new service on the marketplace." : "Update your service details and pricing."}</p>

        <form className="mt-6 space-y-6" onSubmit={(e) => { e.preventDefault(); navigate({ to: "/dashboard" }); }}>
          <div className="space-y-1.5">
            <Label>Service title</Label>
            <Input defaultValue={defaults?.title} placeholder="I will design a premium brand illustration set" className="h-11" />
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label>Category</Label>
              <Select><SelectTrigger className="h-11"><SelectValue placeholder="Select" /></SelectTrigger>
                <SelectContent>{CATEGORIES.map((c) => <SelectItem key={c.slug} value={c.slug}>{c.name}</SelectItem>)}</SelectContent>
              </Select>
            </div>
            <div className="space-y-1.5"><Label>Delivery time</Label>
              <Select><SelectTrigger className="h-11"><SelectValue placeholder="Select" /></SelectTrigger>
                <SelectContent>{["2 days", "3 days", "5 days", "7 days", "14 days"].map((d) => <SelectItem key={d} value={d}>{d}</SelectItem>)}</SelectContent>
              </Select>
            </div>
          </div>
          <div className="space-y-1.5"><Label>Description</Label><Textarea rows={4} placeholder="Describe what's included…" /></div>

          <div>
            <Label>Pricing tiers</Label>
            <div className="mt-2 grid gap-3 sm:grid-cols-3">
              {tiers.map((t, i) => (
                <div key={t} className="rounded-2xl border p-4">
                  <p className="font-medium">{t}</p>
                  <Input defaultValue={i === 1 ? defaults?.price : undefined} type="number" placeholder="$" className="mt-2 h-10" />
                </div>
              ))}
            </div>
          </div>

          <div>
            <Label>Gallery</Label>
            <div className="mt-2 grid grid-cols-3 gap-3 sm:grid-cols-4">
              {[1, 2, 3].map((n) => <div key={n} className="aspect-square rounded-xl border bg-muted/40" />)}
              <label className="flex aspect-square cursor-pointer items-center justify-center rounded-xl border-2 border-dashed text-muted-foreground hover:border-primary"><Plus className="size-6" /><input type="file" className="hidden" /></label>
            </div>
          </div>

          <div className="flex gap-3">
            <Button type="button" variant="outline" className="flex-1 rounded-xl" onClick={() => navigate({ to: "/dashboard" })}>Cancel</Button>
            <Button type="submit" variant="brand" className="flex-1 rounded-xl">{mode === "create" ? "Publish service" : "Save changes"}</Button>
          </div>
        </form>
      </div>
    </AppShell>
  );
}
