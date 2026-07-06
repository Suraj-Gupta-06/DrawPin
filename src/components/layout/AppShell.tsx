import { useState, type ReactNode } from "react";
import { Link, useRouterState } from "@tanstack/react-router";
import {
  Search, Bell, MessageSquare, Plus, Home, Compass, ShoppingBag, Map,
  Menu, User, Settings, LayoutDashboard, LogOut, Heart, Sun, Moon,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { GradientAvatar } from "@/components/art/GradientAvatar";
import {
  DropdownMenu, DropdownMenuContent, DropdownMenuItem,
  DropdownMenuLabel, DropdownMenuSeparator, DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Footer } from "./Footer";

const NAV = [
  { label: "Home", to: "/home", icon: Home },
  { label: "Explore", to: "/explore", icon: Compass },
  { label: "Marketplace", to: "/marketplace", icon: ShoppingBag },
  { label: "Map", to: "/discover-map", icon: Map },
];

function useTheme() {
  const toggle = () => {
    if (typeof document !== "undefined") document.documentElement.classList.toggle("dark");
  };
  return { toggle };
}

export function AppShell({ children, hideFooter }: { children: ReactNode; hideFooter?: boolean }) {
  const pathname = useRouterState({ select: (s) => s.location.pathname });
  const [mobileOpen, setMobileOpen] = useState(false);
  const { toggle } = useTheme();

  return (
    <div className="flex min-h-screen flex-col">
      <header className="sticky top-0 z-50 border-b border-border/60 glass-strong">
        <div className="mx-auto flex h-16 max-w-[1600px] items-center gap-3 px-4">
          <Link to="/" className="flex shrink-0 items-center gap-2">
            <span className="grid size-9 place-items-center rounded-xl brand-gradient font-display text-lg font-bold text-white">D</span>
            <span className="hidden font-display text-xl font-bold sm:block">DrawPin</span>
          </Link>

          <nav className="hidden items-center gap-1 lg:flex">
            {NAV.map((n) => {
              const active = pathname.startsWith(n.to);
              return (
                <Link key={n.to} to={n.to}>
                  <Button variant={active ? "secondary" : "ghost"} size="sm" className="gap-2">
                    <n.icon className="size-4" /> {n.label}
                  </Button>
                </Link>
              );
            })}
          </nav>

          <div className="relative mx-auto hidden w-full max-w-md md:block">
            <Search className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              placeholder="Search art, creators, services…"
              className="h-10 rounded-full bg-muted/60 pl-9"
            />
          </div>

          <div className="ml-auto flex items-center gap-1">
            <Link to="/create" className="hidden sm:block">
              <Button variant="brand" size="sm" className="gap-1.5 rounded-full">
                <Plus className="size-4" /> Create
              </Button>
            </Link>
            <Button variant="ghost" size="icon" className="rounded-full" onClick={toggle} aria-label="Toggle theme">
              <Sun className="size-5 dark:hidden" />
              <Moon className="hidden size-5 dark:block" />
            </Button>
            <Link to="/notifications">
              <Button variant="ghost" size="icon" className="relative rounded-full" aria-label="Notifications">
                <Bell className="size-5" />
                <span className="absolute right-1.5 top-1.5 size-2 rounded-full bg-pink" />
              </Button>
            </Link>
            <Link to="/messages" className="hidden sm:block">
              <Button variant="ghost" size="icon" className="rounded-full" aria-label="Messages">
                <MessageSquare className="size-5" />
              </Button>
            </Link>
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <button className="rounded-full outline-none focus-visible:ring-2 focus-visible:ring-ring">
                  <GradientAvatar seed={3} name="Aria Vance" className="size-9 text-sm" />
                </button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end" className="w-56">
                <DropdownMenuLabel>
                  <div className="font-semibold">Aria Vance</div>
                  <div className="text-xs font-normal text-muted-foreground">@aria.vance</div>
                </DropdownMenuLabel>
                <DropdownMenuSeparator />
                <DropdownMenuItem asChild><Link to="/profile"><User className="size-4" /> Profile</Link></DropdownMenuItem>
                <DropdownMenuItem asChild><Link to="/boards"><Heart className="size-4" /> Boards</Link></DropdownMenuItem>
                <DropdownMenuItem asChild><Link to="/dashboard"><LayoutDashboard className="size-4" /> Creator studio</Link></DropdownMenuItem>
                <DropdownMenuItem asChild><Link to="/admin"><LayoutDashboard className="size-4" /> Admin console</Link></DropdownMenuItem>
                <DropdownMenuItem asChild><Link to="/settings"><Settings className="size-4" /> Settings</Link></DropdownMenuItem>
                <DropdownMenuSeparator />
                <DropdownMenuItem asChild><Link to="/login"><LogOut className="size-4" /> Log out</Link></DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
            <Button variant="ghost" size="icon" className="rounded-full lg:hidden" onClick={() => setMobileOpen((o) => !o)} aria-label="Menu">
              <Menu className="size-5" />
            </Button>
          </div>
        </div>

        {mobileOpen && (
          <div className="border-t bg-card/95 px-4 py-3 lg:hidden">
            <div className="relative mb-3 md:hidden">
              <Search className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
              <Input placeholder="Search…" className="h-10 rounded-full bg-muted/60 pl-9" />
            </div>
            <div className="grid grid-cols-2 gap-2">
              {NAV.map((n) => (
                <Link key={n.to} to={n.to} onClick={() => setMobileOpen(false)}>
                  <Button variant="secondary" className="w-full justify-start gap-2"><n.icon className="size-4" /> {n.label}</Button>
                </Link>
              ))}
            </div>
          </div>
        )}
      </header>

      <main className="flex-1">{children}</main>

      {/* Mobile bottom nav */}
      <nav className="sticky bottom-0 z-40 grid grid-cols-5 border-t glass-strong lg:hidden">
        {NAV.map((n) => {
          const active = pathname.startsWith(n.to);
          return (
            <Link key={n.to} to={n.to} className={`flex flex-col items-center gap-0.5 py-2 text-[10px] ${active ? "text-primary" : "text-muted-foreground"}`}>
              <n.icon className="size-5" /> {n.label}
            </Link>
          );
        })}
        <Link to="/create" className="flex flex-col items-center gap-0.5 py-2 text-[10px] text-muted-foreground">
          <Plus className="size-5" /> Create
        </Link>
      </nav>

      {!hideFooter && <Footer />}
    </div>
  );
}
