import { IDP } from "@/config/env.config";
import { redirect } from "next/navigation";
import { ReactNode } from "react";

type KeycloakLayoutProps = {
  children: ReactNode;
};

export default function KeycloakLayout({ children }: KeycloakLayoutProps) {
  if (IDP.toLowerCase() !== "keycloak") {
    redirect("/login");
  }

  return <>{children}</>;
}
