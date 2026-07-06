import { cn } from "@/lib/utils";

export function Sparkline({
  data,
  className,
  stroke = "var(--brand)",
}: {
  data: number[];
  className?: string;
  stroke?: string;
}) {
  const max = Math.max(...data);
  const min = Math.min(...data);
  const range = max - min || 1;
  const w = 100;
  const h = 32;
  const pts = data.map((d, i) => {
    const x = (i / (data.length - 1)) * w;
    const y = h - ((d - min) / range) * h;
    return [x, y];
  });
  const line = pts.map((p, i) => `${i === 0 ? "M" : "L"}${p[0]},${p[1]}`).join(" ");
  const area = `${line} L${w},${h} L0,${h} Z`;
  const id = `sp${Math.round(data[0])}${data.length}`;
  return (
    <svg viewBox={`0 0 ${w} ${h}`} className={cn("w-full", className)} preserveAspectRatio="none">
      <defs>
        <linearGradient id={id} x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stopColor={stroke} stopOpacity="0.3" />
          <stop offset="100%" stopColor={stroke} stopOpacity="0" />
        </linearGradient>
      </defs>
      <path d={area} fill={`url(#${id})`} />
      <path d={line} fill="none" stroke={stroke} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

export function BarMini({ data, className }: { data: number[]; className?: string }) {
  const max = Math.max(...data) || 1;
  return (
    <div className={cn("flex items-end gap-1.5 h-24", className)}>
      {data.map((d, i) => (
        <div
          key={i}
          className="flex-1 rounded-t-md brand-gradient transition-all hover:opacity-80"
          style={{ height: `${(d / max) * 100}%`, minHeight: 4 }}
        />
      ))}
    </div>
  );
}

export function DonutChart({
  segments,
  size = 140,
  className,
}: {
  segments: { value: number; color: string; label: string }[];
  size?: number;
  className?: string;
}) {
  const total = segments.reduce((a, s) => a + s.value, 0) || 1;
  const radius = 56;
  const circ = 2 * Math.PI * radius;
  let offset = 0;
  return (
    <div className={cn("relative inline-flex", className)} style={{ width: size, height: size }}>
      <svg viewBox="0 0 140 140" className="-rotate-90 w-full h-full">
        <circle cx="70" cy="70" r={radius} fill="none" stroke="var(--muted)" strokeWidth="16" />
        {segments.map((s, i) => {
          const len = (s.value / total) * circ;
          const el = (
            <circle
              key={i}
              cx="70"
              cy="70"
              r={radius}
              fill="none"
              stroke={s.color}
              strokeWidth="16"
              strokeDasharray={`${len} ${circ - len}`}
              strokeDashoffset={-offset}
              strokeLinecap="round"
            />
          );
          offset += len;
          return el;
        })}
      </svg>
      <div className="absolute inset-0 flex flex-col items-center justify-center">
        <span className="font-display text-2xl font-bold">{total}</span>
        <span className="text-xs text-muted-foreground">total</span>
      </div>
    </div>
  );
}
