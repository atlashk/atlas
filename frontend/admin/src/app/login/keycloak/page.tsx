 "use client";
 
 import { Button } from "@/components/ui/button";
 import { IDP } from "@/config/env.config";
 import { loginWithKeycloak } from "@/lib/keycloak";
 import { useRouter } from "next/navigation";
 import { ArrowRight } from "lucide-react";
 
 export default function KeycloakLoginPage() {
   const router = useRouter();
 
   const handleLogin = async () => {
     if (IDP.toLowerCase() === "keycloak") {
       await loginWithKeycloak(null);
       return;
     }
     router.push("/login");
   };
 
   return (
     <div className="flex items-center justify-center h-screen">
       <Button onClick={handleLogin} className="inline-flex items-center gap-2">
         <ArrowRight className="h-4 w-4" />
         Login with Keycloak
       </Button>
     </div>
   );
 }
