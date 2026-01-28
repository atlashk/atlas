# Keycloak Setup

## Create Realm

In Keycloak, create a new Realm named `atlas`.

Access to Realm `atlas` and do the following steps:

## Create client

In `Clients` menu, create a new Client:

- Client type: `OpenID Connect`
- Name: `atlas-auth`
- Capability config
    - Client authentication: `On`
    - Authentication flow: Check on `Implicit flow`

## Creating Realm roles

In `Realm roles` menu, create two Realm roles: `admin` and `user`.

## Creating User Profile Attributes

In `Realm settings` menu, navigate to `User profile` tab, create the following User Profile
Attributes: `userId` and `phoneNumber` which can view and edit by above roles.

### Configure Client Scope Mappers

1. In `Clients` menu, click on `atlas-auth` client.
2. Then navigate to `Client scopes` tab, click on `atlas-auth-dedicated` scope.
3. Navigate to `Mappers` tab, click on `Configure a new mapper`.
4. Select `User Attrinbute` to add a new mapper.

- Name: `user_id`
- User Attribute: Select `userId` that created in `Creating User Profile Attributes` step.
- Token Claim Name: `user_id`
- Add to ID token: `On`
- Add to access token: `On`

Do the same to add a mapper for `phoneNumber` attribute.

- Name: `phone_number`
- User Attribute: Select `phoneNumber` that created in `Creating User Profile Attributes` step.
- Token Claim Name: `phone_number`
- Add to ID token: `On`
- Add to access token: `On`
