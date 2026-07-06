import { Link } from "@tanstack/react-router";

const COLS = [
  {
    title: "Discover",
    links: [
      ["Home feed", "/home"],
      ["Explore", "/explore"],
      ["Marketplace", "/marketplace"],
      ["Discover map", "/discover-map"],
    ],
  },
  {
    title: "Creators",
    links: [
      ["Become a creator", "/signup"],
      ["Creator dashboard", "/dashboard"],
      ["Pricing", "/marketplace"],
      ["Success stories", "/explore"],
    ],
  },
  {
    title: "Company",
    links: [
      ["About", "/"],
      ["Careers", "/"],
      ["Blog", "/"],
      ["Contact", "/"],
    ],
  },
];

export function Footer() {
  return (
    <footer className="border-t bg-card/40">
      <div className="mx-auto max-w-7xl px-4 py-14">
        <div className="grid gap-10 md:grid-cols-[1.5fr_repeat(3,1fr)]">
          <div>
            <Link to="/" className="flex items-center gap-2">
              <span className="grid size-9 place-items-center rounded-xl brand-gradient font-display text-lg font-bold text-white">
                D
              </span>
              <span className="font-display text-xl font-bold">DrawPin</span>
            </Link>
            <p className="mt-4 max-w-xs text-sm text-muted-foreground">
              Discover art, hire world-class creators, and explore local artists — all in one premium creative platform.
            </p>
          </div>
          {COLS.map((col) => (
            <div key={col.title}>
              <h4 className="font-display text-sm font-semibold">{col.title}</h4>
              <ul className="mt-4 space-y-2.5 text-sm text-muted-foreground">
                {col.links.map(([label, href]) => (
                  <li key={label}>
                    <Link to={href} className="story-link transition-colors hover:text-foreground">
                      {label}
                    </Link>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>
        <div className="mt-12 flex flex-col items-center justify-between gap-4 border-t pt-6 text-sm text-muted-foreground sm:flex-row">
          <p>© {new Date().getFullYear()} DrawPin. All rights reserved.</p>
          <div className="flex gap-6">
            <Link to="/" className="hover:text-foreground">Privacy</Link>
            <Link to="/" className="hover:text-foreground">Terms</Link>
            <Link to="/" className="hover:text-foreground">Cookies</Link>
          </div>
        </div>
      </div>
    </footer>
  );
}
