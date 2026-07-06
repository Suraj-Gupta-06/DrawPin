import { cn } from "@/lib/utils";

const PAIRS = [
  ["#7C3AED", "#EC4899"],
  ["#06B6D4", "#7C3AED"],
  ["#EC4899", "#F97316"],
  ["#3B82F6", "#A855F7"],
  ["#14B8A6", "#6366F1"],
  ["#F59E0B", "#EC4899"],
];

export function GradientAvatar({
  seed,
  name,
  className,
}: {
  seed: number;
  name: string;
  className?: string;
}) {
  const pair = PAIRS[seed % PAIRS.length];
  const initials = name
    .split(" ")
    .map((n) => n[0])
    .slice(0, 2)
    .join("")
    .toUpperCase();
  return (
    <div
      className={cn(
        "flex items-center justify-center rounded-full font-semibold text-white select-none ring-2 ring-background",
        className,
      )}
      style={{ backgroundImage: `linear-gradient(135deg, ${pair[0]}, ${pair[1]})` }}
      aria-hidden
    >
      <span className="text-[0.7em]">{initials}</span>
    </div>
  );
}
