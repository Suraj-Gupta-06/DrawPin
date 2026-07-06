import { createFileRoute, useParams } from "@tanstack/react-router";
import { Share2, MoreHorizontal, Lock } from "lucide-react";
import { Button } from "@/components/ui/button";
import { AppShell } from "@/components/layout/AppShell";
import { MasonryGrid } from "@/components/shared/MasonryGrid";
import { BOARDS, PINS } from "@/lib/mock-data";

export const Route = createFileRoute("/board/$boardId")({
  head: () => ({ meta: [{ title: "Board — DrawPin" }, { name: "description", content: "View a curated board of saved artworks." }] }),
  component: BoardPage,
});

function BoardPage() {
  const { boardId } = useParams({ from: "/board/$boardId" });
  const board = BOARDS.find((b) => b.id === boardId) ?? BOARDS[0];
  return (
    <AppShell>
      <div className="mx-auto max-w-[1600px] px-4 py-8">
        <div className="text-center">
          <h1 className="font-display text-3xl font-bold">{board.name}</h1>
          <p className="mt-2 flex items-center justify-center gap-2 text-muted-foreground"><Lock className="size-3.5" /> {board.count} pins · curated by Aria Vance</p>
          <div className="mt-4 flex justify-center gap-2">
            <Button variant="outline" className="rounded-full"><Share2 className="size-4" /> Share</Button>
            <Button variant="outline" size="icon" className="rounded-full"><MoreHorizontal className="size-4" /></Button>
          </div>
        </div>
        <div className="mt-8"><MasonryGrid pins={PINS} /></div>
      </div>
    </AppShell>
  );
}
