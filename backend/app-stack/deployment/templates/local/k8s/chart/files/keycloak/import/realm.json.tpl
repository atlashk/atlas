{
  "realm": "{{ .Values.keycloak.config.default }}",
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
      "clientId": "{{ .Values.keycloak.config.clientId }}",
      "name": "{{ .Values.keycloak.config.clientId }}",
      "enabled": true,
      "protocol": "openid-connect",
      "publicClient": false,
      "clientAuthenticatorType": "client-secret",
      "secret": "{{ .Values.keycloak.config.clientSecret }}",
      "serviceAccountsEnabled": true,
      "directAccessGrantsEnabled": true,
      "standardFlowEnabled": false,
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
      "username": "service-account-{{ .Values.keycloak.config.clientId }}",
      "enabled": true,
      "serviceAccountClientId": "{{ .Values.keycloak.config.clientId }}",
      "clientRoles": {
        "realm-management": [
          "view-users",
          "manage-users"
        ]
      }
    }
  ]
}
