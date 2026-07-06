import { createFileRoute, Link } from "@tanstack/react-router";
import { Search, Filter } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from "@/components/ui/table";
import { AppShell } from "@/components/layout/AppShell";
import { ArtTile } from "@/components/art/ArtTile";
import { GradientAvatar } from "@/components/art/GradientAvatar";
import { StatusBadge } from "@/components/shared/StatusBadge";
import { ORDERS } from "@/lib/mock-data";

export const Route = createFileRoute("/orders")({
  head: () => ({ meta: [{ title: "Orders — DrawPin" }, { name: "description", content: "Manage and track your orders." }] }),
  component: Orders,
});

function Orders() {
  return (
    <AppShell>
      <div className="mx-auto max-w-6xl px-4 py-6">
        <h1 className="font-display text-2xl font-bold">Orders</h1>
        <Tabs defaultValue="all" className="mt-5">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <TabsList><TabsTrigger value="all">All</TabsTrigger><TabsTrigger value="active">Active</TabsTrigger><TabsTrigger value="completed">Completed</TabsTrigger></TabsList>
            <div className="flex gap-2">
              <div className="relative"><Search className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" /><Input placeholder="Search orders…" className="h-10 w-48 rounded-full pl-9" /></div>
              <Button variant="outline" size="icon" className="rounded-full"><Filter className="size-4" /></Button>
            </div>
          </div>
        </Tabs>

        <div className="mt-5 overflow-hidden rounded-2xl border bg-card">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Service</TableHead><TableHead>Creator</TableHead><TableHead>Date</TableHead>
                <TableHead>Status</TableHead><TableHead className="text-right">Total</TableHead><TableHead></TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {ORDERS.map((o) => (
                <TableRow key={o.id}>
                  <TableCell>
                    <div className="flex items-center gap-3">
                      <ArtTile seed={o.service.seed} className="size-10 shrink-0" />
                      <div className="min-w-0"><p className="truncate text-sm font-medium">{o.service.title}</p><p className="text-xs text-muted-foreground">{o.id}</p></div>
                    </div>
                  </TableCell>
                  <TableCell><div className="flex items-center gap-2"><GradientAvatar seed={o.service.creator.seed} name={o.service.creator.name} className="size-7 text-[10px]" /><span className="text-sm">{o.service.creator.name}</span></div></TableCell>
                  <TableCell className="text-sm text-muted-foreground">{o.date}</TableCell>
                  <TableCell><StatusBadge status={o.status} /></TableCell>
                  <TableCell className="text-right font-semibold">${o.total}</TableCell>
                  <TableCell className="text-right"><Link to="/order/$orderId" params={{ orderId: o.id }}><Button variant="ghost" size="sm">View</Button></Link></TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      </div>
    </AppShell>
  );
}
