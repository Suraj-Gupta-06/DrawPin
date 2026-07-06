import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { UploadCloud, ImagePlus, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from "@/components/ui/select";
import { AppShell } from "@/components/layout/AppShell";
import { CATEGORIES } from "@/lib/mock-data";

export const Route = createFileRoute("/create")({
  head: () => ({ meta: [{ title: "Create pin — DrawPin" }, { name: "description", content: "Upload and publish your artwork to DrawPin." }] }),
  component: CreatePin,
});

function CreatePin() {
  const navigate = useNavigate();
  return (
    <AppShell>
      <div className="mx-auto max-w-5xl px-4 py-6">
        <h1 className="font-display text-2xl font-bold">Create a pin</h1>
        <p className="mt-1 text-muted-foreground">Share your artwork with the DrawPin community.</p>

        <form className="mt-6 grid gap-6 lg:grid-cols-[1fr_1.2fr]" onSubmit={(e) => { e.preventDefault(); navigate({ to: "/profile" }); }}>
          <label className="group flex aspect-[3/4] cursor-pointer flex-col items-center justify-center rounded-3xl border-2 border-dashed bg-muted/40 p-8 text-center transition-colors hover:border-primary hover:bg-muted/60">
            <span className="grid size-16 place-items-center rounded-2xl brand-gradient text-white"><UploadCloud className="size-8" /></span>
            <p className="mt-4 font-semibold">Drag & drop or click to upload</p>
            <p className="mt-1 text-sm text-muted-foreground">PNG, JPG, GIF up to 20MB · Recommended 2:3</p>
            <input type="file" className="hidden" accept="image/*" />
          </label>

          <div className="space-y-5">
            <div className="space-y-1.5">
              <Label htmlFor="title">Title</Label>
              <Input id="title" placeholder="Give your pin a catchy title" className="h-11" />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="desc">Description</Label>
              <Textarea id="desc" placeholder="Tell the story behind your work…" rows={4} />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <Label>Category</Label>
                <Select>
                  <SelectTrigger className="h-11"><SelectValue placeholder="Select" /></SelectTrigger>
                  <SelectContent>
                    {CATEGORIES.map((c) => <SelectItem key={c.slug} value={c.slug}>{c.name}</SelectItem>)}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-1.5">
                <Label>Board</Label>
                <Select>
                  <SelectTrigger className="h-11"><SelectValue placeholder="Choose board" /></SelectTrigger>
                  <SelectContent>
                    <SelectItem value="inspiration">Inspiration</SelectItem>
                    <SelectItem value="brand">Brand Moodboard</SelectItem>
                    <SelectItem value="3d">3D & Render</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="tags">Tags</Label>
              <div className="flex flex-wrap gap-2 rounded-xl border p-2">
                {["abstract", "gradient"].map((t) => (
                  <span key={t} className="flex items-center gap-1 rounded-full bg-muted px-2.5 py-1 text-xs">{t} <X className="size-3 cursor-pointer" /></span>
                ))}
                <input className="flex-1 bg-transparent px-2 text-sm outline-none" placeholder="Add tags…" />
              </div>
            </div>
            <div className="flex gap-3">
              <Button type="button" variant="outline" className="flex-1 rounded-xl">Save draft</Button>
              <Button type="submit" variant="brand" className="flex-1 rounded-xl"><ImagePlus className="size-4" /> Publish pin</Button>
            </div>
          </div>
        </form>
      </div>
    </AppShell>
  );
}
