import { createFileRoute, Link, useParams } from "@tanstack/react-router";
import { ArrowLeft, Phone, Video, Info, Send, Paperclip, Smile } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { AppShell } from "@/components/layout/AppShell";
import { GradientAvatar } from "@/components/art/GradientAvatar";
import { CONVERSATIONS, MESSAGES } from "@/lib/mock-data";
import { cn } from "@/lib/utils";

export const Route = createFileRoute("/messages/$chatId")({
  head: () => ({ meta: [{ title: "Chat — DrawPin" }, { name: "description", content: "Conversation thread." }] }),
  component: Chat,
});

function Chat() {
  const { chatId } = useParams({ from: "/messages/$chatId" });
  const convo = CONVERSATIONS.find((c) => c.id === chatId) ?? CONVERSATIONS[0];
  return (
    <AppShell hideFooter>
      <div className="mx-auto flex h-[calc(100vh-4rem)] max-w-3xl flex-col px-4">
        <div className="flex items-center gap-3 border-b py-3">
          <Link to="/messages"><Button variant="ghost" size="icon" className="rounded-full"><ArrowLeft className="size-5" /></Button></Link>
          <GradientAvatar seed={convo.creator.seed} name={convo.creator.name} className="size-10 text-sm" />
          <div className="flex-1"><p className="font-semibold">{convo.creator.name}</p><p className="text-xs text-success">Active now</p></div>
          <Button variant="ghost" size="icon" className="rounded-full"><Phone className="size-5" /></Button>
          <Button variant="ghost" size="icon" className="rounded-full"><Video className="size-5" /></Button>
          <Button variant="ghost" size="icon" className="rounded-full"><Info className="size-5" /></Button>
        </div>

        <div className="flex-1 space-y-3 overflow-y-auto py-4">
          <p className="text-center text-xs text-muted-foreground">Today</p>
          {MESSAGES.map((m) => (
            <div key={m.id} className={cn("flex", m.me ? "justify-end" : "justify-start")}>
              <div className={cn(
                "max-w-[75%] rounded-2xl px-4 py-2.5 text-sm",
                m.me ? "brand-gradient rounded-br-md text-white" : "rounded-bl-md bg-muted",
              )}>
                <p>{m.text}</p>
                <p className={cn("mt-1 text-[10px]", m.me ? "text-white/70" : "text-muted-foreground")}>{m.time}</p>
              </div>
            </div>
          ))}
        </div>

        <div className="flex items-center gap-2 border-t py-3">
          <Button variant="ghost" size="icon" className="rounded-full"><Paperclip className="size-5" /></Button>
          <Input placeholder="Type a message…" className="h-11 rounded-full" />
          <Button variant="ghost" size="icon" className="rounded-full"><Smile className="size-5" /></Button>
          <Button variant="brand" size="icon" className="size-11 shrink-0 rounded-full"><Send className="size-4" /></Button>
        </div>
      </div>
    </AppShell>
  );
}
