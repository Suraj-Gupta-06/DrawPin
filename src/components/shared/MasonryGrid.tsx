import type { Pin } from "@/lib/mock-data";
import { PinCard } from "./cards";

export function MasonryGrid({ pins }: { pins: Pin[] }) {
  return (
    <div className="columns-2 gap-4 sm:columns-3 lg:columns-4 xl:columns-5 [&>*]:mb-4">
      {pins.map((p) => <PinCard key={p.id} pin={p} />)}
    </div>
  );
}
