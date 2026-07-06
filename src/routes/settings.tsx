import { createFileRoute } from "@tanstack/react-router";
import { User, Shield, Lock, Bell, Palette, CreditCard, Trash2, Sun, Moon } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Switch } from "@/components/ui/switch";
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs";
import { AppShell } from "@/components/layout/AppShell";

export const Route = createFileRoute("/settings")({
  head: () => ({ meta: [{ title: "Settings — DrawPin" }, { name: "description", content: "Manage your account, privacy, security and appearance." }] }),
  component: Settings,
});

function Row({ title, desc, children }: { title: string; desc: string; children: React.ReactNode }) {
  return (
    <div className="flex items-center justify-between gap-4 rounded-2xl border bg-card p-4">
      <div><p className="font-medium">{title}</p><p className="text-sm text-muted-foreground">{desc}</p></div>
      {children}
    </div>
  );
}

function Settings() {
  const toggleTheme = () => document.documentElement.classList.toggle("dark");
  return (
    <AppShell>
      <div className="mx-auto max-w-3xl px-4 py-6">
        <h1 className="font-display text-2xl font-bold">Settings</h1>
        <Tabs defaultValue="account" className="mt-6">
          <TabsList className="flex-wrap">
            <TabsTrigger value="account"><User className="size-4" /> Account</TabsTrigger>
            <TabsTrigger value="privacy"><Shield className="size-4" /> Privacy</TabsTrigger>
            <TabsTrigger value="security"><Lock className="size-4" /> Security</TabsTrigger>
            <TabsTrigger value="appearance"><Palette className="size-4" /> Appearance</TabsTrigger>
          </TabsList>

          <TabsContent value="account" className="mt-6 space-y-4">
            <div className="grid gap-4 sm:grid-cols-2">
              <div className="space-y-1.5"><Label>Email</Label><Input defaultValue="aria@example.com" className="h-11" /></div>
              <div className="space-y-1.5"><Label>Username</Label><Input defaultValue="aria.vance" className="h-11" /></div>
            </div>
            <Row title="Email notifications" desc="Receive updates about your activity"><Switch defaultChecked /></Row>
            <Row title="Marketing emails" desc="Product news and creator tips"><Switch /></Row>
            <div className="rounded-2xl border border-destructive/30 bg-destructive/5 p-4">
              <p className="font-medium text-destructive">Delete account</p>
              <p className="mt-1 text-sm text-muted-foreground">Permanently remove your account and all data.</p>
              <Button variant="destructive" size="sm" className="mt-3 rounded-xl"><Trash2 className="size-4" /> Delete account</Button>
            </div>
          </TabsContent>

          <TabsContent value="privacy" className="mt-6 space-y-4">
            <Row title="Private profile" desc="Only approved followers can see your pins"><Switch /></Row>
            <Row title="Show activity status" desc="Let others see when you're online"><Switch defaultChecked /></Row>
            <Row title="Allow messages" desc="Receive messages from anyone"><Switch defaultChecked /></Row>
            <Row title="Search engine indexing" desc="Allow your profile to appear in search engines"><Switch defaultChecked /></Row>
          </TabsContent>

          <TabsContent value="security" className="mt-6 space-y-4">
            <div className="space-y-1.5"><Label>Current password</Label><Input type="password" className="h-11" /></div>
            <div className="grid gap-4 sm:grid-cols-2">
              <div className="space-y-1.5"><Label>New password</Label><Input type="password" className="h-11" /></div>
              <div className="space-y-1.5"><Label>Confirm</Label><Input type="password" className="h-11" /></div>
            </div>
            <Button variant="brand" className="rounded-xl">Update password</Button>
            <Row title="Two-factor authentication" desc="Add an extra layer of security"><Switch /></Row>
            <Row title="Active sessions" desc="3 devices currently signed in"><Button variant="outline" size="sm" className="rounded-xl">Manage</Button></Row>
          </TabsContent>

          <TabsContent value="appearance" className="mt-6 space-y-4">
            <Row title="Dark mode" desc="Toggle between light and dark theme">
              <Button variant="outline" size="sm" className="rounded-xl" onClick={toggleTheme}><Sun className="size-4 dark:hidden" /><Moon className="hidden size-4 dark:block" /> Toggle</Button>
            </Row>
            <Row title="Reduce motion" desc="Minimize animations across the app"><Switch /></Row>
            <Row title="Compact layout" desc="Show more content per screen"><Switch /></Row>
          </TabsContent>
        </Tabs>
      </div>
    </AppShell>
  );
}
