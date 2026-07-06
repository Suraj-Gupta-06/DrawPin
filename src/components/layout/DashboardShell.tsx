import { useState, type ReactNode } from "react";
import { Link, useRouterState } from "@tanstack/react-router";
import {
  LayoutDashboard, FolderKanban, Wallet, BarChart3, ShoppingBag, MessageSquare,
  Users, Flag, FileWarning, Shield, CreditCard, ArrowLeft, Menu, Bell,
  type LucideIcon,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { GradientAvatar } from "@/components/art/GradientAvatar";
import { cn } from "@/lib/utils";

type Item = { label: string; to: string; icon: LucideIcon; badge?: string };

const CREATOR_NAV: Item[] = [
  { label: "Overview", to: "/dashboard", icon: LayoutDashboard },
  { label: "Analytics", to: "/analytics", icon: BarChart3 },
  { label: "Portfolio", to: "/portfolio-manager", icon: FolderKanban },
  { label: "Earnings", to: "/earnings", icon: Wallet },
  { label: "Orders", to: "/orders", icon: ShoppingBag, badge: "3" },
  { label: "Messages", to: "/messages", icon: MessageSquare, badge: "2" },
];

const ADMIN_NAV: Item[] = [
  { label: "Overview", to: "/admin", icon: LayoutDashboard },
  { label: "Analytics", to: "/admin/analytics", icon: BarChart3 },
  { label: "Users", to: "/admin/users", icon: Users },
  { label: "Reports", to: "/admin/reports", icon: Flag, badge: "12" },
  { label: "Content", to: "/admin/content", icon: FileWarning },
  { label: "Roles", to: "/admin/roles", icon: Shield },
  { label: "Disputes", to: "/admin/disputes", icon: CreditCard, badge: "4" },
];

export function DashboardShell({
  children,
  variant = "creator",
  title,
}: {
  children: ReactNode;
  variant?: "creator" | "admin";
  title: string;
}) {
  const pathname = useRouterState({ select: (s) => s.location.pathname });
  const nav = variant === "admin" ? ADMIN_NAV : CREATOR_NAV;
  const [open, setOpen] = useState(false);

  const SidebarBody = (
    <div className="flex h-full flex-col">
      <div className="flex h-16 items-center gap-2 px-5">
        <span className="grid size-9 place-items-center rounded-xl brand-gradient font-display text-lg font-bold text-white">D</span>
        <div className="leading-tight">
          <div className="font-display font-bold">DrawPin</div>
          <div className="text-xs text-muted-foreground">{variant === "admin" ? "Admin console" : "Creator studio"}</div>
        </div>
      </div>
      <nav className="flex-1 space-y-1 px-3 py-2">
        {nav.map((item) => {
          const active = pathname === item.to;
          return (
            <Link key={item.to} to={item.to} onClick={() => setOpen(false)}>
              <span
                className={cn(
                  "flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition-colors",
                  active ? "brand-gradient text-white shadow" : "text-muted-foreground hover:bg-muted hover:text-foreground",
                )}
              >
                <item.icon className="size-4.5" />
                <span className="flex-1">{item.label}</span>
                {item.badge && (
                  <Badge variant={active ? "glass" : "secondary"} className="h-5 px-1.5">{item.badge}</Badge>
                )}
              </span>
            </Link>
          );
        })}
      </nav>
      <div className="border-t p-3">
        <Link to="/home">
          <Button variant="ghost" size="sm" className="w-full justify-start gap-2 text-muted-foreground">
            <ArrowLeft className="size-4" /> Back to DrawPin
          </Button>
        </Link>
      </div>
    </div>
  );

  return (
    <div className="flex min-h-screen bg-muted/30">
      <aside className="hidden w-64 shrink-0 border-r bg-sidebar lg:block">
        <div className="sticky top-0 h-screen">{SidebarBody}</div>
      </aside>

      {open && (
        <div className="fixed inset-0 z-50 lg:hidden">
          <div className="absolute inset-0 bg-black/50" onClick={() => setOpen(false)} />
          <aside className="absolute left-0 top-0 h-full w-64 border-r bg-sidebar">{SidebarBody}</aside>
        </div>
      )}

      <div className="flex min-w-0 flex-1 flex-col">
        <header className="sticky top-0 z-40 flex h-16 items-center gap-3 border-b glass-strong px-4 lg:px-6">
          <Button variant="ghost" size="icon" className="lg:hidden" onClick={() => setOpen(true)} aria-label="Open menu">
            <Menu className="size-5" />
          </Button>
          <h1 className="font-display text-lg font-semibold">{title}</h1>
          <div className="ml-auto flex items-center gap-2">
            <Link to="/notifications">
              <Button variant="ghost" size="icon" className="relative rounded-full">
                <Bell className="size-5" />
                <span className="absolute right-1.5 top-1.5 size-2 rounded-full bg-pink" />
              </Button>
            </Link>
            <GradientAvatar seed={3} name="Aria Vance" className="size-9 text-sm" />
          </div>
        </header>
        <main className="flex-1 p-4 lg:p-6">{children}</main>
      </div>
    </div>
  );
}

export function StatCard({
  label, value, delta, icon: Icon, children,
}: {
  label: string; value: string; delta?: string; icon: LucideIcon; children?: ReactNode;
}) {
  return (
    <div className="rounded-2xl border bg-card p-5">
      <div className="flex items-center justify-between">
        <span className="text-sm text-muted-foreground">{label}</span>
        <span className="grid size-9 place-items-center rounded-xl bg-primary/10 text-primary"><Icon className="size-4.5" /></span>
      </div>
      <div className="mt-3 flex items-end justify-between">
        <span className="font-display text-2xl font-bold">{value}</span>
        {delta && <span className="text-xs font-medium text-success">{delta}</span>}
      </div>
      {children && <div className="mt-3">{children}</div>}
    </div>
  );
}
