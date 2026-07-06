import { useState } from "react";
import { SlidersHorizontal } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { CATEGORIES } from "@/lib/mock-data";
import { cn } from "@/lib/utils";

export function FilterBar() {
  const [active, setActive] = useState("all");
  return (
    <div className="flex items-center gap-2 overflow-x-auto no-scrollbar py-1">
      <button className="flex shrink-0 items-center gap-1.5 rounded-full border bg-card px-3.5 py-1.5 text-sm font-medium">
        <SlidersHorizontal className="size-3.5" /> Filters
      </button>
      <span className="h-6 w-px shrink-0 bg-border" />
      {[{ slug: "all", name: "All" }, ...CATEGORIES].map((c) => (
        <button
          key={c.slug}
          onClick={() => setActive(c.slug)}
          className={cn(
            "shrink-0 rounded-full px-3.5 py-1.5 text-sm font-medium transition-colors",
            active === c.slug ? "brand-gradient text-white" : "bg-muted text-muted-foreground hover:bg-muted/70",
          )}
        >
          {c.name}
        </button>
      ))}
    </div>
  );
}
