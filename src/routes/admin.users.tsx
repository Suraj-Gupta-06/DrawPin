import { createFileRoute } from "@tanstack/react-router";
import { Search, MoreHorizontal, Shield, Ban, CheckCircle2 } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from "@/components/ui/table";
import {
  DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
  Pagination, PaginationContent, PaginationItem, PaginationLink, PaginationNext, PaginationPrevious,
} from "@/components/ui/pagination";
import { DashboardShell } from "@/components/layout/DashboardShell";
import { GradientAvatar } from "@/components/art/GradientAvatar";
import { CREATORS, fmt } from "@/lib/mock-data";

export const Route = createFileRoute("/admin/users")({
  head: () => ({ meta: [{ title: "User management — DrawPin" }, { name: "description", content: "Manage platform users and roles." }] }),
  component: UserManagement,
});

const ROLES = ["User", "Creator", "Creator", "Moderator", "User", "Creator", "Admin", "User"];

function UserManagement() {
  return (
    <DashboardShell variant="admin" title="User management">
      <div className="rounded-2xl border bg-card">
        <div className="flex flex-wrap items-center justify-between gap-3 border-b p-4">
          <div className="relative"><Search className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" /><Input placeholder="Search users…" className="h-10 w-64 rounded-full pl-9" /></div>
          <div className="flex gap-2">
            <Badge variant="secondary" className="cursor-pointer rounded-full px-3 py-1.5">All</Badge>
            <Badge variant="secondary" className="cursor-pointer rounded-full px-3 py-1.5">Creators</Badge>
            <Badge variant="secondary" className="cursor-pointer rounded-full px-3 py-1.5">Suspended</Badge>
          </div>
        </div>
        <Table>
          <TableHeader><TableRow><TableHead>User</TableHead><TableHead>Role</TableHead><TableHead>Followers</TableHead><TableHead>Status</TableHead><TableHead></TableHead></TableRow></TableHeader>
          <TableBody>
            {CREATORS.slice(0, 10).map((c, i) => (
              <TableRow key={c.id}>
                <TableCell><div className="flex items-center gap-3"><GradientAvatar seed={c.seed} name={c.name} className="size-9 text-xs" /><div><p className="text-sm font-medium">{c.name}</p><p className="text-xs text-muted-foreground">@{c.handle}</p></div></div></TableCell>
                <TableCell><Badge variant={ROLES[i % ROLES.length] === "Admin" ? "brand" : ROLES[i % ROLES.length] === "Moderator" ? "cyan" : "secondary"}>{ROLES[i % ROLES.length]}</Badge></TableCell>
                <TableCell className="text-sm text-muted-foreground">{fmt(c.followers)}</TableCell>
                <TableCell><Badge variant={i % 5 === 0 ? "destructive" : "success"}>{i % 5 === 0 ? "Suspended" : "Active"}</Badge></TableCell>
                <TableCell className="text-right">
                  <DropdownMenu>
                    <DropdownMenuTrigger asChild><Button variant="ghost" size="icon"><MoreHorizontal className="size-4" /></Button></DropdownMenuTrigger>
                    <DropdownMenuContent align="end">
                      <DropdownMenuItem><Shield className="size-4" /> Change role</DropdownMenuItem>
                      <DropdownMenuItem><CheckCircle2 className="size-4" /> Verify</DropdownMenuItem>
                      <DropdownMenuItem className="text-destructive"><Ban className="size-4" /> Suspend</DropdownMenuItem>
                    </DropdownMenuContent>
                  </DropdownMenu>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
        <div className="border-t p-3">
          <Pagination>
            <PaginationContent>
              <PaginationItem><PaginationPrevious href="#" /></PaginationItem>
              <PaginationItem><PaginationLink href="#" isActive>1</PaginationLink></PaginationItem>
              <PaginationItem><PaginationLink href="#">2</PaginationLink></PaginationItem>
              <PaginationItem><PaginationLink href="#">3</PaginationLink></PaginationItem>
              <PaginationItem><PaginationNext href="#" /></PaginationItem>
            </PaginationContent>
          </Pagination>
        </div>
      </div>
    </DashboardShell>
  );
}
