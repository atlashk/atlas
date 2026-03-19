{
  "realm": "{{ .Values.keycloak.config.realm }}",
  "enabled": true,
  "sslRequired": "none",
  "attributes": {
    "userProfileEnabled": "true"
  },
  "roles": {
    "realm": [
      {
        "name": "admin",
        "description": "Administrator role"
      },
      {
        "name": "user",
        "description": "User role"
      }
    ]
  },
  "components": {
    "org.keycloak.userprofile.UserProfileProvider": [
      {
        "providerId": "declarative-user-profile",
        "config": {
          "kc.user.profile.config": [
            "{\"attributes\":[{\"name\":\"username\",\"displayName\":\"${username}\",\"validations\":{\"length\":{\"min\":3,\"max\":255},\"username-prohibited-characters\":{},\"up-username-not-idn-homograph\":{}},\"permissions\":{\"view\":[\"admin\",\"user\"],\"edit\":[\"admin\"]}},{\"name\":\"email\",\"displayName\":\"${email}\",\"validations\":{\"email\":{},\"length\":{\"max\":255}},\"permissions\":{\"view\":[\"admin\",\"user\"],\"edit\":[\"admin\",\"user\"]}},{\"name\":\"firstName\",\"displayName\":\"${firstName}\",\"validations\":{\"length\":{\"max\":255},\"person-name-prohibited-characters\":{}},\"permissions\":{\"view\":[\"admin\",\"user\"],\"edit\":[\"admin\",\"user\"]}},{\"name\":\"lastName\",\"displayName\":\"${lastName}\",\"validations\":{\"length\":{\"max\":255},\"person-name-prohibited-characters\":{}},\"permissions\":{\"view\":[\"admin\",\"user\"],\"edit\":[\"admin\",\"user\"]}},{\"name\":\"phoneNumber\",\"displayName\":\"Phone number\",\"validations\":{\"length\":{\"max\":20}},\"permissions\":{\"view\":[\"admin\",\"user\"],\"edit\":[\"admin\",\"user\"]}}],\"groups\":[]}"
          ]
        }
      }
    ]
  },
  "clients": [
    {
      "clientId": "{{ .Values.keycloak.config.clients.adminClient.clientId }}",
      "name": "{{ .Values.keycloak.config.clients.adminClient.clientId }}",
      "enabled": true,
      "protocol": "openid-connect",
      "publicClient": false,
      "clientAuthenticatorType": "client-secret",
      "secret": "{{ .Values.keycloak.config.clients.adminClient.clientSecret }}",
      "serviceAccountsEnabled": true,
      "directAccessGrantsEnabled": false,
      "standardFlowEnabled": false,
      "defaultClientScopes": [
        "basic",
        "profile",
        "email",
        "phone",
        "roles"
      ]
    },
    {
      "clientId": "{{ .Values.keycloak.config.clients.webStorefrontClient.clientId }}",
      "name": "{{ .Values.keycloak.config.clients.webStorefrontClient.clientId }}",
      "enabled": true,
      "protocol": "openid-connect",
      "publicClient": true,
      "standardFlowEnabled": true,
      "directAccessGrantsEnabled": false,
      "serviceAccountsEnabled": false,
      "redirectUris": {{ .Values.keycloak.config.clients.webStorefrontClient.redirectUris | toJson }},
      "webOrigins": {{ .Values.keycloak.config.clients.webStorefrontClient.webOrigins | toJson }},
      "attributes": {
        "pkce.code.challenge.method": "S256",
        "post.logout.redirect.uris": {{ join " " .Values.keycloak.config.clients.webStorefrontClient.postLogoutRedirectUris | quote }}
      },
      "defaultClientScopes": [
        "basic",
        "profile",
        "email",
        "phone",
        "roles"
      ]
    },
    {
      "clientId": "{{ .Values.keycloak.config.clients.webAdminClient.clientId }}",
      "name": "{{ .Values.keycloak.config.clients.webAdminClient.clientId }}",
      "enabled": true,
      "protocol": "openid-connect",
      "publicClient": true,
      "standardFlowEnabled": true,
      "directAccessGrantsEnabled": false,
      "serviceAccountsEnabled": false,
      "redirectUris": {{ .Values.keycloak.config.clients.webAdminClient.redirectUris | toJson }},
      "webOrigins": {{ .Values.keycloak.config.clients.webAdminClient.webOrigins | toJson }},
      "attributes": {
        "pkce.code.challenge.method": "S256",
        "post.logout.redirect.uris": {{ join " " .Values.keycloak.config.clients.webAdminClient.postLogoutRedirectUris | quote }}
      },
      "defaultClientScopes": [
        "basic",
        "profile",
        "email",
        "phone",
        "roles"
      ]
    }
  ],
  "users": [
    {
      "username": "service-account-{{ .Values.keycloak.config.clients.adminClient.clientId }}",
      "enabled": true,
      "serviceAccountClientId": "{{ .Values.keycloak.config.clients.adminClient.clientId }}",
      "clientRoles": {
        "realm-management": [
          "view-users",
          "manage-users"
        ]
      }
    }
  ]
}
