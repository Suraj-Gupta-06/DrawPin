import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { Camera } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { AppShell } from "@/components/layout/AppShell";
import { ArtTile } from "@/components/art/ArtTile";
import { GradientAvatar } from "@/components/art/GradientAvatar";

export const Route = createFileRoute("/profile/edit")({
  head: () => ({ meta: [{ title: "Edit profile — DrawPin" }, { name: "description", content: "Edit your DrawPin profile details." }] }),
  component: EditProfile,
});

function EditProfile() {
  const navigate = useNavigate();
  return (
    <AppShell>
      <div className="mx-auto max-w-2xl px-4 py-6">
        <h1 className="font-display text-2xl font-bold">Edit profile</h1>
        <form className="mt-6 space-y-6" onSubmit={(e) => { e.preventDefault(); navigate({ to: "/profile" }); }}>
          <div className="relative h-40 overflow-hidden rounded-2xl">
            <ArtTile seed={42} rounded={false} className="absolute inset-0" />
            <button type="button" className="absolute right-3 top-3 flex items-center gap-1.5 rounded-full glass-strong px-3 py-1.5 text-sm font-medium text-white"><Camera className="size-4" /> Change cover</button>
          </div>
          <div className="relative z-10 -mt-12 flex items-end gap-4 px-4">
            <div className="relative">
              <GradientAvatar seed={3} name="Aria Vance" className="size-24 text-2xl ring-4 ring-background" />
              <button type="button" className="absolute bottom-0 right-0 grid size-8 place-items-center rounded-full brand-gradient text-white"><Camera className="size-4" /></button>
            </div>
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5"><Label htmlFor="fn">Full name</Label><Input id="fn" defaultValue="Aria Vance" className="h-11" /></div>
            <div className="space-y-1.5"><Label htmlFor="un">Username</Label><Input id="un" defaultValue="aria.vance" className="h-11" /></div>
          </div>
          <div className="space-y-1.5"><Label htmlFor="bio">Bio</Label><Textarea id="bio" rows={3} defaultValue="Visual artist & illustrator crafting bold gradient worlds." /></div>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5"><Label htmlFor="loc">Location</Label><Input id="loc" defaultValue="Berlin, Germany" className="h-11" /></div>
            <div className="space-y-1.5"><Label htmlFor="web">Website</Label><Input id="web" defaultValue="aria.studio" className="h-11" /></div>
          </div>
          <div className="flex gap-3">
            <Button type="button" variant="outline" className="flex-1 rounded-xl" onClick={() => navigate({ to: "/profile" })}>Cancel</Button>
            <Button type="submit" variant="brand" className="flex-1 rounded-xl">Save changes</Button>
          </div>
        </form>
      </div>
    </AppShell>
  );
}
