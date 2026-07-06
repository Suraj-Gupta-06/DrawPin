import { cn } from "@/lib/utils";

const PALETTES = [
  ["#7C3AED", "#EC4899", "#06B6D4"],
  ["#06B6D4", "#7C3AED", "#F472B6"],
  ["#EC4899", "#F97316", "#7C3AED"],
  ["#22D3EE", "#3B82F6", "#A855F7"],
  ["#A855F7", "#EC4899", "#FB7185"],
  ["#14B8A6", "#06B6D4", "#6366F1"],
  ["#F59E0B", "#EC4899", "#8B5CF6"],
  ["#8B5CF6", "#06B6D4", "#10B981"],
];

function rng(seed: number) {
  let s = seed % 2147483647;
  if (s <= 0) s += 2147483646;
  return () => (s = (s * 16807) % 2147483647) / 2147483647;
}

export function ArtTile({
  seed,
  className,
  rounded = true,
}: {
  seed: number;
  className?: string;
  rounded?: boolean;
}) {
  const r = rng(seed + 1);
  const pal = PALETTES[seed % PALETTES.length];
  const variant = seed % 4;
  const id = `g${seed}`;

  return (
    <svg
      viewBox="0 0 100 100"
      preserveAspectRatio="xMidYMid slice"
      className={cn("h-full w-full", rounded && "rounded-2xl", className)}
      aria-hidden
    >
      <defs>
        <linearGradient id={`${id}a`} x1="0" y1="0" x2="1" y2="1">
          <stop offset="0%" stopColor={pal[0]} />
          <stop offset="100%" stopColor={pal[1]} />
        </linearGradient>
        <radialGradient id={`${id}b`} cx="30%" cy="25%" r="80%">
          <stop offset="0%" stopColor={pal[2]} stopOpacity="0.9" />
          <stop offset="100%" stopColor={pal[0]} stopOpacity="0" />
        </radialGradient>
      </defs>
      <rect width="100" height="100" fill={`url(#${id}a)`} />
      <rect width="100" height="100" fill={`url(#${id}b)`} />
      {variant === 0 &&
        Array.from({ length: 5 }).map((_, i) => (
          <circle
            key={i}
            cx={r() * 100}
            cy={r() * 100}
            r={8 + r() * 26}
            fill={pal[i % 3]}
            opacity={0.18 + r() * 0.3}
          />
        ))}
      {variant === 1 &&
        Array.from({ length: 6 }).map((_, i) => (
          <rect
            key={i}
            x={r() * 80}
            y={r() * 80}
            width={10 + r() * 40}
            height={6 + r() * 30}
            rx="3"
            fill={pal[i % 3]}
            opacity={0.2 + r() * 0.35}
            transform={`rotate(${r() * 60 - 30} 50 50)`}
          />
        ))}
      {variant === 2 &&
        Array.from({ length: 4 }).map((_, i) => (
          <path
            key={i}
            d={`M${r() * 100} ${r() * 100} Q ${r() * 100} ${r() * 100} ${r() * 100} ${r() * 100}`}
            stroke={pal[i % 3]}
            strokeWidth={2 + r() * 6}
            fill="none"
            opacity={0.4 + r() * 0.4}
            strokeLinecap="round"
          />
        ))}
      {variant === 3 &&
        Array.from({ length: 3 }).map((_, i) => (
          <polygon
            key={i}
            points={`${r() * 100},${r() * 100} ${r() * 100},${r() * 100} ${r() * 100},${r() * 100}`}
            fill={pal[i % 3]}
            opacity={0.25 + r() * 0.4}
          />
        ))}
    </svg>
  );
}
