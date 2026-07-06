import { createFileRoute } from "@tanstack/react-router";
import { ServiceForm } from "@/components/shared/ServiceForm";

export const Route = createFileRoute("/create-service")({
  head: () => ({ meta: [{ title: "Create service — DrawPin" }, { name: "description", content: "List a new service on the DrawPin marketplace." }] }),
  component: () => <ServiceForm mode="create" />,
});
