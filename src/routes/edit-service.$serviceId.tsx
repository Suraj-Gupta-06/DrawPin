import { createFileRoute, useParams } from "@tanstack/react-router";
import { ServiceForm } from "@/components/shared/ServiceForm";
import { SERVICES } from "@/lib/mock-data";

export const Route = createFileRoute("/edit-service/$serviceId")({
  head: () => ({ meta: [{ title: "Edit service — DrawPin" }, { name: "description", content: "Edit your DrawPin service." }] }),
  component: EditService,
});

function EditService() {
  const { serviceId } = useParams({ from: "/edit-service/$serviceId" });
  const s = SERVICES.find((x) => x.id === serviceId) ?? SERVICES[0];
  return <ServiceForm mode="edit" defaults={{ title: s.title, price: String(s.price) }} />;
}
