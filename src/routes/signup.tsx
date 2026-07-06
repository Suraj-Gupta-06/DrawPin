import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { Mail, Lock, User } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Checkbox } from "@/components/ui/checkbox";
import { AuthShell, SocialButtons } from "@/components/layout/AuthShell";

export const Route = createFileRoute("/signup")({
  head: () => ({ meta: [{ title: "Sign up — DrawPin" }, { name: "description", content: "Create your free DrawPin account." }] }),
  component: Signup,
});

function Signup() {
  const navigate = useNavigate();
  return (
    <AuthShell
      title="Create your account"
      subtitle="Join 40,000+ creatives on DrawPin — it's free."
      footer={<>Already have an account? <Link to="/login" className="font-semibold text-primary story-link">Log in</Link></>}
    >
      <SocialButtons />
      <div className="my-6 flex items-center gap-3 text-xs text-muted-foreground">
        <span className="h-px flex-1 bg-border" /> or sign up with email <span className="h-px flex-1 bg-border" />
      </div>
      <form className="space-y-4" onSubmit={(e) => { e.preventDefault(); navigate({ to: "/home" }); }}>
        <div className="space-y-1.5">
          <Label htmlFor="name">Full name</Label>
          <div className="relative">
            <User className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
            <Input id="name" placeholder="Aria Vance" className="h-11 pl-9" required />
          </div>
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="email">Email</Label>
          <div className="relative">
            <Mail className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
            <Input id="email" type="email" placeholder="you@example.com" className="h-11 pl-9" required />
          </div>
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="password">Password</Label>
          <div className="relative">
            <Lock className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
            <Input id="password" type="password" placeholder="At least 8 characters" className="h-11 pl-9" required />
          </div>
        </div>
        <label className="flex items-start gap-2 text-xs text-muted-foreground">
          <Checkbox className="mt-0.5" /> I agree to DrawPin's Terms of Service and Privacy Policy.
        </label>
        <Button type="submit" variant="brand" className="h-11 w-full rounded-xl">Create account</Button>
      </form>
    </AuthShell>
  );
}
