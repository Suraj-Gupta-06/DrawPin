import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { Mail, Lock } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Checkbox } from "@/components/ui/checkbox";
import { AuthShell, SocialButtons } from "@/components/layout/AuthShell";

export const Route = createFileRoute("/login")({
  head: () => ({ meta: [{ title: "Log in — DrawPin" }, { name: "description", content: "Log in to your DrawPin account." }] }),
  component: Login,
});

function Login() {
  const navigate = useNavigate();
  return (
    <AuthShell
      title="Welcome back"
      subtitle="Log in to continue discovering and creating."
      footer={<>Don't have an account? <Link to="/signup" className="font-semibold text-primary story-link">Sign up</Link></>}
    >
      <SocialButtons />
      <div className="my-6 flex items-center gap-3 text-xs text-muted-foreground">
        <span className="h-px flex-1 bg-border" /> or continue with email <span className="h-px flex-1 bg-border" />
      </div>
      <form className="space-y-4" onSubmit={(e) => { e.preventDefault(); navigate({ to: "/home" }); }}>
        <div className="space-y-1.5">
          <Label htmlFor="email">Email</Label>
          <div className="relative">
            <Mail className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
            <Input id="email" type="email" placeholder="you@example.com" className="h-11 pl-9" required />
          </div>
        </div>
        <div className="space-y-1.5">
          <div className="flex items-center justify-between">
            <Label htmlFor="password">Password</Label>
            <Link to="/forgot-password" className="text-xs text-primary story-link">Forgot password?</Link>
          </div>
          <div className="relative">
            <Lock className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
            <Input id="password" type="password" placeholder="••••••••" className="h-11 pl-9" required />
          </div>
        </div>
        <label className="flex items-center gap-2 text-sm text-muted-foreground">
          <Checkbox /> Remember me for 30 days
        </label>
        <Button type="submit" variant="brand" className="h-11 w-full rounded-xl">Log in</Button>
      </form>
    </AuthShell>
  );
}
