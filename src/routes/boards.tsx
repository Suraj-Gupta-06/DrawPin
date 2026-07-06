import { createFileRoute, Link } from "@tanstack/react-router";
import { Plus } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Switch } from "@/components/ui/switch";
import {
  Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger, DialogFooter, DialogDescription,
} from "@/components/ui/dialog";
import { AppShell } from "@/components/layout/AppShell";
import { ArtTile } from "@/components/art/ArtTile";
import { BOARDS } from "@/lib/mock-data";

export const Route = createFileRoute("/boards")({
  head: () => ({ meta: [{ title: "Your boards — DrawPin" }, { name: "description", content: "Organize your saved artworks into boards." }] }),
  component: Boards,
});

function CreateBoardModal() {
  return (
    <Dialog>
      <DialogTrigger asChild>
        <Button variant="brand" className="rounded-full"><Plus className="size-4" /> Create board</Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Create board</DialogTitle>
          <DialogDescription>Boards help you organize and revisit your favorite pins.</DialogDescription>
        </DialogHeader>
        <div className="space-y-4 py-2">
          <div className="space-y-1.5">
            <Label htmlFor="bname">Name</Label>
            <Input id="bname" placeholder="e.g. Brand Inspiration" />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="bdesc">Description</Label>
            <Textarea id="bdesc" placeholder="What's this board about?" rows={3} />
          </div>
          <label className="flex items-center justify-between rounded-xl border p-3">
            <div>
              <p className="text-sm font-medium">Keep private</p>
              <p className="text-xs text-muted-foreground">Only you can see this board</p>
            </div>
            <Switch />
          </label>
        </div>
        <DialogFooter>
          <Button variant="brand" className="w-full rounded-xl">Create board</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function Boards() {
  return (
    <AppShell>
      <div className="mx-auto max-w-[1400px] px-4 py-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="font-display text-2xl font-bold">Your boards</h1>
            <p className="mt-1 text-muted-foreground">{BOARDS.length} boards · 360 pins saved</p>
          </div>
          <CreateBoardModal />
        </div>

        <div className="mt-6 grid grid-cols-2 gap-5 sm:grid-cols-3 lg:grid-cols-4">
          {BOARDS.map((b) => (
            <Link key={b.id} to="/board/$boardId" params={{ boardId: b.id }} className="group">
              <div className="grid grid-cols-2 gap-1 overflow-hidden rounded-2xl border bg-card p-1">
                <ArtTile seed={b.seeds[0]} className="col-span-1 row-span-2 aspect-[3/4]" />
                <ArtTile seed={b.seeds[1]} className="aspect-square" rounded={false} />
                <ArtTile seed={b.seeds[2]} className="aspect-square" rounded={false} />
              </div>
              <p className="mt-2 font-display font-semibold group-hover:text-primary">{b.name}</p>
              <p className="text-sm text-muted-foreground">{b.count} pins</p>
            </Link>
          ))}
        </div>
      </div>
    </AppShell>
  );
}
