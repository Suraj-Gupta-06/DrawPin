import { createFileRoute, Link } from "@tanstack/react-router";
import { useState } from "react";
import { Mail, CheckCircle2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { AuthShell } from "@/components/layout/AuthShell";

export const Route = createFileRoute("/forgot-password")({
  head: () => ({ meta: [{ title: "Forgot password — DrawPin" }, { name: "description", content: "Reset your DrawPin password." }] }),
  component: Forgot,
});

function Forgot() {
  const [sent, setSent] = useState(false);
  return (
    <AuthShell
      title="Forgot password?"
      subtitle="Enter your email and we'll send you a reset link."
      footer={<>Remembered it? <Link to="/login" className="font-semibold text-primary story-link">Back to log in</Link></>}
    >
      {sent ? (
        <div className="rounded-2xl border bg-card p-6 text-center">
          <CheckCircle2 className="mx-auto size-10 text-success" />
          <p className="mt-3 font-medium">Check your inbox</p>
          <p className="mt-1 text-sm text-muted-foreground">We've sent a reset link to your email address.</p>
          <Link to="/reset-password"><Button variant="brand" className="mt-4 w-full rounded-xl">Open reset page</Button></Link>
        </div>
      ) : (
        <form className="space-y-4" onSubmit={(e) => { e.preventDefault(); setSent(true); }}>
          <div className="space-y-1.5">
            <Label htmlFor="email">Email</Label>
            <div className="relative">
              <Mail className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
              <Input id="email" type="email" placeholder="you@example.com" className="h-11 pl-9" required />
            </div>
          </div>
          <Button type="submit" variant="brand" className="h-11 w-full rounded-xl">Send reset link</Button>
        </form>
      )}
    </AuthShell>
  );
}
