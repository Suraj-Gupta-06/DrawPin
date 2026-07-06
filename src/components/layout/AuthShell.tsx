import type { ReactNode } from "react";
import { Link } from "@tanstack/react-router";
import { ArtTile } from "@/components/art/ArtTile";
import { Star } from "lucide-react";

export function AuthShell({
  title, subtitle, children, footer,
}: {
  title: string; subtitle: string; children: ReactNode; footer: ReactNode;
}) {
  return (
    <div className="grid min-h-screen lg:grid-cols-2">
      <div className="relative hidden overflow-hidden lg:block">
        <ArtTile seed={5} rounded={false} className="absolute inset-0" />
        <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-black/20 to-black/40" />
        <div className="relative flex h-full flex-col justify-between p-12 text-white">
          <Link to="/" className="flex items-center gap-2">
            <span className="grid size-9 place-items-center rounded-xl bg-white/20 font-display text-lg font-bold backdrop-blur">D</span>
            <span className="font-display text-xl font-bold">DrawPin</span>
          </Link>
          <div>
            <div className="flex gap-1">{Array.from({ length: 5 }).map((_, i) => <Star key={i} className="size-5 fill-warning text-warning" />)}</div>
            <p className="mt-4 max-w-md font-display text-2xl font-semibold leading-snug">
              "DrawPin is where my art finally found its audience — and my best clients."
            </p>
            <p className="mt-3 text-white/80">Luna Reyes · Illustrator, Lisbon</p>
          </div>
        </div>
      </div>

      <div className="flex items-center justify-center p-6">
        <div className="w-full max-w-sm">
          <Link to="/" className="mb-8 flex items-center gap-2 lg:hidden">
            <span className="grid size-9 place-items-center rounded-xl brand-gradient font-display text-lg font-bold text-white">D</span>
            <span className="font-display text-xl font-bold">DrawPin</span>
          </Link>
          <h1 className="font-display text-2xl font-bold">{title}</h1>
          <p className="mt-1.5 text-sm text-muted-foreground">{subtitle}</p>
          <div className="mt-7">{children}</div>
          <div className="mt-6 text-center text-sm text-muted-foreground">{footer}</div>
        </div>
      </div>
    </div>
  );
}

export function SocialButtons() {
  const providers = [
    { name: "Google", letter: "G" },
    { name: "Facebook", letter: "f" },
    { name: "Apple", letter: "" },
  ];
  return (
    <div className="grid grid-cols-3 gap-3">
      {providers.map((p) => (
        <button key={p.name} className="flex h-11 items-center justify-center rounded-xl border bg-card text-sm font-medium transition-colors hover:bg-muted" aria-label={`Continue with ${p.name}`}>
          {p.name === "Apple" ? "" : <span className="font-display text-base">{p.letter}</span>}
          {p.name === "Apple" && <span className="text-lg"></span>}
        </button>
      ))}
    </div>
  );
}
