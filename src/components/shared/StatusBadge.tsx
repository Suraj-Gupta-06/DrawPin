import { Badge } from "@/components/ui/badge";

const MAP: Record<string, "brand" | "success" | "warning" | "cyan" | "destructive" | "secondary"> = {
  "In progress": "cyan",
  "Delivered": "warning",
  "In review": "warning",
  "Completed": "success",
  "Cancelled": "destructive",
  "Open": "warning",
  "Resolved": "success",
  "Pending": "secondary",
};

export function StatusBadge({ status }: { status: string }) {
  return <Badge variant={MAP[status] ?? "secondary"}>{status}</Badge>;
}
