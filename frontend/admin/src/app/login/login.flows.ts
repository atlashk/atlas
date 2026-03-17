import { LoginRequest } from "@/interfaces/identity.interface";

export type FlowResult =
  | { success: true; redirectUrl?: string | null }
  | { success: false; errorMessage: string };

export const resolveAuthorizationBaseUrl = (authorizationApiBaseUrl: string) => {
  try {
    const url = new URL(authorizationApiBaseUrl);
    if (
      url.protocol === "http:" &&
      (url.hostname === "127.0.0.1" || url.hostname === "0.0.0.0")
    ) {
      url.hostname = "localhost";
    }
    return url.toString().replace(/\/$/, "");
  } catch {
    return authorizationApiBaseUrl.replace(/\/$/, "");
  }
};

const createPkceData = async () => {
  const randomBytes = new Uint8Array(32);
  crypto.getRandomValues(randomBytes);
  const verifier = btoa(String.fromCharCode(...randomBytes))
    .replace(/\+/g, "-")
    .replace(/\//g, "_")
    .replace(/=+$/g, "");
  const verifierBuffer = new TextEncoder().encode(verifier);
  const challengeBuffer = await crypto.subtle.digest("SHA-256", verifierBuffer);
  const challengeBytes = Array.from(new Uint8Array(challengeBuffer));
  const challenge = btoa(String.fromCharCode(...challengeBytes))
    .replace(/\+/g, "-")
    .replace(/\//g, "_")
    .replace(/=+$/g, "");
  const stateBytes = new Uint8Array(16);
  crypto.getRandomValues(stateBytes);
  const state = Array.from(
    stateBytes,
    (byte) => byte.toString(16).padStart(2, "0")
  ).join("");
  return { verifier, challenge, state };
};

export const executeJwtLoginFlow = async (
  loginAction: (
    credentials: LoginRequest
  ) => Promise<{ success: boolean; errorMessage?: string }>,
  credentials: LoginRequest,
  resolveRedirectUrl: () => string | null
): Promise<FlowResult> => {
  const response = await loginAction(credentials);
  if (!response.success) {
    return {
      success: false,
      errorMessage:
        response.errorMessage || "Login failed. Please check your credentials.",
    };
  }
  return {
    success: true,
    redirectUrl: resolveRedirectUrl(),
  };
};

export const createOAuth2AuthorizationUrl = async (args: {
  authorizationBaseUrl: string;
  clientId: string;
  scope: string;
  redirectUri: string;
  pkceVerifierStorageKey: string;
  stateStorageKey: string;
}) => {
  const { verifier, challenge, state } = await createPkceData();
  sessionStorage.setItem(args.pkceVerifierStorageKey, verifier);
  sessionStorage.setItem(args.stateStorageKey, state);
  const authorizeUrl = new URL(`${args.authorizationBaseUrl}/oauth2/authorize`);
  authorizeUrl.searchParams.set("response_type", "code");
  authorizeUrl.searchParams.set("client_id", args.clientId);
  authorizeUrl.searchParams.set("scope", args.scope);
  authorizeUrl.searchParams.set("redirect_uri", args.redirectUri);
  authorizeUrl.searchParams.set("code_challenge", challenge);
  authorizeUrl.searchParams.set("code_challenge_method", "S256");
  authorizeUrl.searchParams.set("state", state);
  return authorizeUrl.toString();
};

export const resolveInitialOauth2ErrorMessage = (
  oauth2Error: string | null,
  code: string | null
) => {
  if (oauth2Error) {
    return "OAuth2 login failed. Please try again.";
  }
  if (!code) {
    return "Missing OAuth2 authorization response. Please try login again.";
  }
  return "";
};

export const exchangeAuthorizationCodeForTokens = async (args: {
  authorizationBaseUrl: string;
  clientId: string;
  code: string;
  redirectUri: string;
  codeVerifier: string;
}) => {
  const tokenRequestBody = new URLSearchParams({
    grant_type: "authorization_code",
    client_id: args.clientId,
    code: args.code,
    redirect_uri: args.redirectUri,
    code_verifier: args.codeVerifier,
  });
  const tokenResponse = await fetch(`${args.authorizationBaseUrl}/oauth2/token`, {
    method: "POST",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded",
    },
    body: tokenRequestBody.toString(),
  });
  const tokenData = await tokenResponse.json();
  if (!tokenResponse.ok) {
    throw new Error(tokenData.error_description || "OAuth2 token exchange failed.");
  }
  return {
    accessToken: tokenData.access_token as string | undefined,
    refreshToken: tokenData.refresh_token as string | undefined,
  };
};

export const resolvePkceVerifier = (
  receivedState: string | null,
  stateStorageKey: string,
  verifierStorageKey: string
) => {
  const storedState = sessionStorage.getItem(stateStorageKey);
  const codeVerifier = sessionStorage.getItem(verifierStorageKey);
  if (!storedState || !receivedState || receivedState !== storedState || !codeVerifier) {
    return null;
  }
  return codeVerifier;
};

export const clearPkceState = (stateStorageKey: string, verifierStorageKey: string) => {
  sessionStorage.removeItem(stateStorageKey);
  sessionStorage.removeItem(verifierStorageKey);
};

export const executeOauth2LoginFlow = async (
  loginWithTokensAction: (
    accessToken: string,
    refreshToken?: string | null
  ) => Promise<{ success: boolean; errorMessage?: string }>,
  accessToken: string,
  refreshToken?: string | null
): Promise<FlowResult> => {
  const response = await loginWithTokensAction(accessToken, refreshToken);
  if (!response.success) {
    return {
      success: false,
      errorMessage: response.errorMessage || "OAuth2 login failed.",
    };
  }
  return { success: true };
};
