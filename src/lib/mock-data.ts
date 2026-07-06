// Deterministic mock data for DrawPin — no external assets, all generative.

export const CATEGORIES = [
  { slug: "illustration", name: "Illustration", icon: "PenTool" },
  { slug: "3d", name: "3D Art", icon: "Boxes" },
  { slug: "photography", name: "Photography", icon: "Camera" },
  { slug: "branding", name: "Branding", icon: "Sparkles" },
  { slug: "ui-ux", name: "UI / UX", icon: "LayoutDashboard" },
  { slug: "concept", name: "Concept Art", icon: "Wand2" },
  { slug: "typography", name: "Typography", icon: "Type" },
  { slug: "motion", name: "Motion", icon: "Play" },
  { slug: "abstract", name: "Abstract", icon: "Shapes" },
  { slug: "character", name: "Character", icon: "Smile" },
];

const FIRST = ["Aria", "Kenji", "Luna", "Mateo", "Sofia", "Noah", "Ines", "Theo", "Maya", "Omar", "Zoe", "Ravi", "Nora", "Leo", "Yara", "Finn"];
const LAST = ["Vance", "Tanaka", "Reyes", "Okonkwo", "Bauer", "Costa", "Lindqvist", "Mehta", "Park", "Halver", "Dubois", "Sato"];
const CITIES = [
  { city: "Berlin", lat: 52.52, lng: 13.405 },
  { city: "Lisbon", lat: 38.72, lng: -9.14 },
  { city: "Tokyo", lat: 35.68, lng: 139.69 },
  { city: "New York", lat: 40.71, lng: -74.0 },
  { city: "Mexico City", lat: 19.43, lng: -99.13 },
  { city: "Cape Town", lat: -33.92, lng: 18.42 },
  { city: "Mumbai", lat: 19.07, lng: 72.87 },
  { city: "São Paulo", lat: -23.55, lng: -46.63 },
];

function seeded(n: number) {
  const x = Math.sin(n * 999.13) * 10000;
  return x - Math.floor(x);
}

export interface Pin {
  id: string;
  seed: number;
  title: string;
  category: string;
  author: Creator;
  likes: number;
  saves: number;
  comments: number;
  ratio: number; // height / width for masonry
  tags: string[];
}

export interface Creator {
  id: string;
  seed: number;
  name: string;
  handle: string;
  city: string;
  lat: number;
  lng: number;
  followers: number;
  rating: number;
  reviews: number;
  level: "New" | "Rising" | "Top Rated" | "Pro";
  rate: number;
  bio: string;
  specialties: string[];
}

const TITLES = [
  "Neon Reverie", "Quiet Geometry", "Liquid Chrome", "Dawn Patrol", "Paper Forest",
  "Gradient Dreams", "Static Bloom", "Cobalt Drift", "Velvet Static", "Soft Machine",
  "Aurora Study", "Midnight Garden", "Prism Pulse", "Coral Logic", "Echo Chamber",
  "Solar Wind", "Marble Mind", "Glass Horizon", "Vapor Trail", "Fold & Flow",
  "Crimson Index", "Tidal Forms", "Lunar Mesh", "Pastel Riot", "Iron Lotus",
];

export const CREATORS: Creator[] = Array.from({ length: 24 }, (_, i) => {
  const s = i + 1;
  const fn = FIRST[Math.floor(seeded(s) * FIRST.length)];
  const ln = LAST[Math.floor(seeded(s * 3) * LAST.length)];
  const loc = CITIES[i % CITIES.length];
  const levels: Creator["level"][] = ["New", "Rising", "Top Rated", "Pro"];
  return {
    id: `c${s}`,
    seed: s * 7,
    name: `${fn} ${ln}`,
    handle: `${fn.toLowerCase()}.${ln.toLowerCase()}`,
    city: loc.city,
    lat: loc.lat + (seeded(s * 5) - 0.5) * 0.06,
    lng: loc.lng + (seeded(s * 9) - 0.5) * 0.06,
    followers: Math.floor(seeded(s * 2) * 90000) + 1200,
    rating: Math.round((4.3 + seeded(s * 4) * 0.7) * 10) / 10,
    reviews: Math.floor(seeded(s * 6) * 480) + 12,
    level: levels[Math.floor(seeded(s * 8) * levels.length)],
    rate: Math.floor(seeded(s * 11) * 16) * 25 + 75,
    bio: "Multidisciplinary artist crafting bold visual systems, immersive scenes and brand-defining illustration.",
    specialties: [CATEGORIES[i % CATEGORIES.length].name, CATEGORIES[(i + 3) % CATEGORIES.length].name],
  };
});

export const PINS: Pin[] = Array.from({ length: 48 }, (_, i) => {
  const s = i + 1;
  const ratios = [0.75, 1, 1.25, 1.4, 0.85, 1.1, 1.5];
  return {
    id: `p${s}`,
    seed: s * 13,
    title: TITLES[i % TITLES.length],
    category: CATEGORIES[i % CATEGORIES.length].slug,
    author: CREATORS[i % CREATORS.length],
    likes: Math.floor(seeded(s) * 12000) + 80,
    saves: Math.floor(seeded(s * 2) * 6000) + 30,
    comments: Math.floor(seeded(s * 3) * 320) + 2,
    ratio: ratios[Math.floor(seeded(s * 4) * ratios.length)],
    tags: [CATEGORIES[i % CATEGORIES.length].name, "trending", "editorial"],
  };
});

export interface ServiceItem {
  id: string;
  seed: number;
  title: string;
  creator: Creator;
  category: string;
  price: number;
  rating: number;
  reviews: number;
  delivery: string;
}

const SERVICE_TITLES = [
  "I will design a premium brand illustration set",
  "I will create a stylized 3D product render",
  "I will craft a custom character design sheet",
  "I will design a modern SaaS landing page",
  "I will produce editorial poster artwork",
  "I will animate a logo reveal in motion",
  "I will paint a concept art environment",
  "I will design a bespoke typographic logo",
  "I will create an NFT-ready art collection",
  "I will illustrate a children's book spread",
  "I will design an album cover artwork",
  "I will build a full social media art kit",
];

export const SERVICES: ServiceItem[] = SERVICE_TITLES.map((title, i) => {
  const s = i + 1;
  return {
    id: `s${s}`,
    seed: s * 17,
    title,
    creator: CREATORS[i % CREATORS.length],
    category: CATEGORIES[i % CATEGORIES.length].slug,
    price: Math.floor(seeded(s) * 12) * 25 + 45,
    rating: Math.round((4.4 + seeded(s * 3) * 0.6) * 10) / 10,
    reviews: Math.floor(seeded(s * 2) * 320) + 8,
    delivery: `${Math.floor(seeded(s * 4) * 6) + 2} days`,
  };
});

export const NOTIFICATIONS = [
  { id: "n1", type: "like", actor: CREATORS[2], text: "liked your pin Neon Reverie", time: "2m" },
  { id: "n2", type: "follow", actor: CREATORS[5], text: "started following you", time: "18m" },
  { id: "n3", type: "comment", actor: CREATORS[8], text: "commented: this is incredible work!", time: "1h" },
  { id: "n4", type: "order", actor: CREATORS[1], text: "placed an order for Brand Illustration Set", time: "3h" },
  { id: "n5", type: "save", actor: CREATORS[11], text: "saved your pin to Inspiration board", time: "6h" },
  { id: "n6", type: "review", actor: CREATORS[4], text: "left a 5-star review on your service", time: "1d" },
];

export const CONVERSATIONS = CREATORS.slice(0, 8).map((c, i) => ({
  id: `chat${i + 1}`,
  creator: c,
  last: [
    "Sounds great — I'll send the first drafts tomorrow!",
    "Can you adjust the color palette a bit?",
    "Thanks for the quick turnaround 🙏",
    "Just sent the final files over.",
    "Loving the direction so far!",
    "Let's hop on a quick call later?",
    "The revisions look perfect.",
    "Invoice attached — thanks again!",
  ][i],
  time: ["now", "5m", "20m", "1h", "2h", "Yesterday", "Tue", "Mon"][i],
  unread: i < 2 ? i + 1 : 0,
}));

export const MESSAGES = [
  { id: "m1", me: false, text: "Hey! Loved your portfolio. Are you available for a brand illustration project?", time: "10:02" },
  { id: "m2", me: true, text: "Hi! Thank you 🙏 Yes, I have capacity starting next week.", time: "10:04" },
  { id: "m3", me: false, text: "Perfect. We need a set of 6 hero illustrations for our SaaS site.", time: "10:05" },
  { id: "m4", me: true, text: "Got it. I can deliver a first concept in 3 days, full set in ~10 days.", time: "10:07" },
  { id: "m5", me: false, text: "That works. What's your rate for the package?", time: "10:08" },
  { id: "m6", me: true, text: "For 6 custom illustrations + 2 revision rounds, $1,450.", time: "10:09" },
];

export const BOARDS = [
  { id: "b1", name: "Inspiration", count: 124, seeds: [13, 26, 39, 52] },
  { id: "b2", name: "Brand Moodboard", count: 56, seeds: [65, 78, 91, 104] },
  { id: "b3", name: "3D & Render", count: 38, seeds: [117, 130, 143, 156] },
  { id: "b4", name: "Type Specimens", count: 72, seeds: [169, 182, 195, 208] },
  { id: "b5", name: "Color Studies", count: 41, seeds: [221, 234, 247, 260] },
  { id: "b6", name: "Editorial", count: 29, seeds: [273, 286, 299, 312] },
];

export const ORDERS = SERVICES.slice(0, 6).map((s, i) => ({
  id: `DP-${4820 + i}`,
  service: s,
  status: (["In progress", "Delivered", "In review", "Completed", "In progress", "Cancelled"] as const)[i],
  date: ["Jun 12", "Jun 08", "Jun 05", "May 28", "May 22", "May 14"][i],
  total: s.price,
}));

export function fmt(n: number) {
  if (n >= 1000) return `${(n / 1000).toFixed(n >= 10000 ? 0 : 1)}k`;
  return `${n}`;
}

export const REVENUE_SERIES = [12, 18, 15, 22, 28, 24, 31, 38, 35, 42, 48, 54];
export const VISITS_SERIES = [320, 410, 380, 520, 610, 590, 720, 810, 760, 880, 940, 1020];
