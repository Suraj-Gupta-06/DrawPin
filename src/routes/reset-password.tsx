import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { Lock } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { AuthShell } from "@/components/layout/AuthShell";

export const Route = createFileRoute("/reset-password")({
  head: () => ({ meta: [{ title: "Reset password — DrawPin" }, { name: "description", content: "Set a new DrawPin password." }] }),
  component: Reset,
});

function Reset() {
  const navigate = useNavigate();
  return (
    <AuthShell
      title="Set a new password"
      subtitle="Choose a strong password you haven't used before."
      footer={<>Back to <Link to="/login" className="font-semibold text-primary story-link">log in</Link></>}
    >
      <form className="space-y-4" onSubmit={(e) => { e.preventDefault(); navigate({ to: "/login" }); }}>
        <div className="space-y-1.5">
          <Label htmlFor="password">New password</Label>
          <div className="relative">
            <Lock className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
            <Input id="password" type="password" placeholder="At least 8 characters" className="h-11 pl-9" required />
          </div>
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="confirm">Confirm password</Label>
          <div className="relative">
            <Lock className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
            <Input id="confirm" type="password" placeholder="Re-enter password" className="h-11 pl-9" required />
          </div>
        </div>
        <Button type="submit" variant="brand" className="h-11 w-full rounded-xl">Reset password</Button>
      </form>
    </AuthShell>
  );
}
