import {
  KEYCLOAK_CLIENT_ID,
  KEYCLOAK_REALM,
  KEYCLOAK_URL
} from "@/config/env.config";
import Keycloak from "keycloak-js";

let keycloakInstance: Keycloak | null = null;
let keycloakInitialized = false;
let keycloakInitPromise: Promise<boolean> | null = null;

const getCallbackRedirectUri = () =>
  `${window.location.origin}/login/callback`;

const getInitPromise = (keycloak: Keycloak) => {
  if (!keycloakInitPromise) {
    keycloakInitPromise = keycloak.init({
      pkceMethod: "S256",
      responseMode: "query",
      checkLoginIframe: false,
      redirectUri: getCallbackRedirectUri()
    });
  }
  return keycloakInitPromise;
};

const getKeycloakInstance = () => {
  if (!keycloakInstance) {
    keycloakInstance = new Keycloak({
      url: KEYCLOAK_URL,
      realm: KEYCLOAK_REALM,
      clientId: KEYCLOAK_CLIENT_ID
    });
  }
  return keycloakInstance;
};

export const loginWithKeycloak = async (redirectPath?: string | null) => {
  const keycloak = getKeycloakInstance();
  if (!keycloakInitialized) {
    keycloakInitialized = await getInitPromise(keycloak);
  }
  if (redirectPath) {
    sessionStorage.setItem("auth_redirect", redirectPath);
  }
  await keycloak.login({
    redirectUri: getCallbackRedirectUri()
  });
};

export const initKeycloakOnCallback = async () => {
  const keycloak = getKeycloakInstance();
  if (!keycloakInitialized) {
    keycloakInitialized = await getInitPromise(keycloak);
  }
  if (!keycloak.authenticated || !keycloak.token) {
    return null;
  }
  return {
    accessToken: keycloak.token,
    refreshToken: keycloak.refreshToken || null
  };
};
