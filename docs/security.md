# Security

## Authentication

https://levelup.gitconnected.com/system-design-concepts-authentication-in-distributed-systems-6c59bc76308a
https://levelup.gitconnected.com/system-design-concepts-oauth-2-0-for-service-to-service-communication-0265c0c33344
https://medium.com/swlh/all-you-need-to-know-about-authentication-is-here-25c8d8135cd6

### JWT

#### Invalidate/Revoked the JWT

**Option 1. Blacklist JWT**

To invalidate the JWT token upon logout, you can maintain a blacklist or a list of revoked tokens. When a user logs out, add their token to this blacklist. When a request is made with a blacklisted token, it should be rejected. You can store the blacklisted tokens in memory, in a database, or using a distributed cache like Redis.

#### Token storage

1. Cookies
2. Local storage

---

## Authorization

https://levelup.gitconnected.com/system-design-concepts-authorization-in-distributed-systems-1da48088ee83

---

## API Gateway

Acts as the central entry point for requests and enforces security.

---

## Secure Communication

---

## Service Mesh

---

## Secret Management
