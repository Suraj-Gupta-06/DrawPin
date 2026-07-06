import { Link } from "@tanstack/react-router";
import { Heart, Bookmark, MessageCircle, Star, MapPin, Clock } from "lucide-react";
import { ArtTile } from "@/components/art/ArtTile";
import { GradientAvatar } from "@/components/art/GradientAvatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { fmt, type Pin, type Creator, type ServiceItem } from "@/lib/mock-data";

export function PinCard({ pin }: { pin: Pin }) {
  return (
    <div className="group relative mb-4 break-inside-avoid">
      <Link
        to="/pin/$pinId"
        params={{ pinId: pin.id }}
        className="block overflow-hidden rounded-2xl"
        style={{ aspectRatio: `1 / ${pin.ratio}` }}
      >
        <ArtTile seed={pin.seed} className="transition-transform duration-500 group-hover:scale-105" />
        <div className="absolute inset-0 rounded-2xl bg-gradient-to-t from-black/60 via-transparent to-transparent opacity-0 transition-opacity group-hover:opacity-100" />
        <div className="absolute right-3 top-3 flex gap-2 opacity-0 translate-y-1 transition-all group-hover:opacity-100 group-hover:translate-y-0">
          <Button size="sm" variant="brand" className="rounded-full">
            <Bookmark className="size-4" /> Save
          </Button>
        </div>
        <div className="absolute inset-x-3 bottom-3 flex items-center justify-between opacity-0 transition-opacity group-hover:opacity-100">
          <span className="glass-strong rounded-full px-2.5 py-1 text-xs font-medium text-white">
            ❤ {fmt(pin.likes)}
          </span>
        </div>
      </Link>
      <div className="mt-2 flex items-center gap-2 px-1">
        <GradientAvatar seed={pin.author.seed} name={pin.author.name} className="size-6 text-[10px]" />
        <span className="truncate text-sm font-medium">{pin.title}</span>
      </div>
    </div>
  );
}

export function CreatorCard({ creator }: { creator: Creator }) {
  return (
    <div className="group overflow-hidden rounded-3xl border bg-card transition-all hover:-translate-y-1 hover:shadow-xl">
      <Link to="/creator/$creatorId" params={{ creatorId: creator.id }} className="block">
        <div className="relative h-28">
          <ArtTile seed={creator.seed + 99} rounded={false} />
        </div>
        <div className="px-5 pb-5">
          <div className="relative z-10 -mt-8 flex items-end justify-between">
            <GradientAvatar seed={creator.seed} name={creator.name} className="size-16 text-lg" />
            <Badge variant="glass" className="mb-1 gap-1">
              <Star className="size-3 fill-warning text-warning" /> {creator.rating}
            </Badge>
          </div>
          <h3 className="mt-3 font-display text-lg font-semibold">{creator.name}</h3>
          <p className="flex items-center gap-1 text-sm text-muted-foreground">
            <MapPin className="size-3.5" /> {creator.city}
          </p>
          <div className="mt-3 flex flex-wrap gap-1.5">
            {creator.specialties.map((s) => (
              <Badge key={s} variant="secondary" className="font-normal">
                {s}
              </Badge>
            ))}
          </div>
          <div className="mt-4 flex items-center justify-between border-t pt-3 text-sm">
            <span className="text-muted-foreground">{fmt(creator.followers)} followers</span>
            <span className="font-semibold">From ${creator.rate}</span>
          </div>
        </div>
      </Link>
    </div>
  );
}

export function ServiceCard({ service }: { service: ServiceItem }) {
  return (
    <div className="group overflow-hidden rounded-2xl border bg-card transition-all hover:-translate-y-1 hover:shadow-xl">
      <Link to="/service/$serviceId" params={{ serviceId: service.id }} className="block">
        <div className="relative aspect-[4/3] overflow-hidden">
          <ArtTile seed={service.seed} rounded={false} className="transition-transform duration-500 group-hover:scale-105" />
        </div>
        <div className="p-4">
          <div className="flex items-center gap-2">
            <GradientAvatar seed={service.creator.seed} name={service.creator.name} className="size-6 text-[10px]" />
            <span className="text-xs font-medium text-muted-foreground">{service.creator.name}</span>
          </div>
          <p className="mt-2 line-clamp-2 min-h-[2.5rem] text-sm font-medium">{service.title}</p>
          <div className="mt-2 flex items-center gap-1 text-sm">
            <Star className="size-3.5 fill-warning text-warning" />
            <span className="font-semibold">{service.rating}</span>
            <span className="text-muted-foreground">({service.reviews})</span>
          </div>
          <div className="mt-3 flex items-center justify-between border-t pt-3">
            <span className="flex items-center gap-1 text-xs text-muted-foreground">
              <Clock className="size-3.5" /> {service.delivery}
            </span>
            <span className="text-sm">
              <span className="text-xs text-muted-foreground">From </span>
              <span className="font-display font-bold">${service.price}</span>
            </span>
          </div>
        </div>
      </Link>
    </div>
  );
}
