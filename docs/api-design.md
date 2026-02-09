# API Design Best Practices

A well-designed API is crucial for the success of any software project. It ensures a positive developer experience, reduces integration costs, and allows the system to evolve gracefully. This guide outlines best practices for designing RESTful APIs.

---

## 1. Follow RESTful Principles

REST (Representational State Transfer) is an architectural style that defines a set of constraints for creating web services.

- **Client-Server Architecture:** Separate the user interface concerns from the data storage concerns.
- **Statelessness:** Each request from a client to a server must contain all the information needed to understand and complete the request. The server should not store any client context between requests.
- **Cacheability:** Responses must, implicitly or explicitly, define themselves as cacheable or not to prevent clients from reusing stale or inappropriate data.
- **Layered System:** A client cannot ordinarily tell whether it is connected directly to the end server or to an intermediary along the way.
- **Uniform Interface:** This is the fundamental principle of REST and simplifies the overall system architecture. It includes:
    - **Resource-Based:** Use nouns, not verbs, for resource URIs.
    - **Manipulation of Resources Through Representations:** The client has a representation of a resource and can take actions to modify or delete the resource on the server.
    - **Self-Descriptive Messages:** Each message includes enough information to describe how to process it.
    - **Hypermedia as the Engine of Application State (HATEOAS):** Responses should include links to other related resources, allowing clients to discover the API dynamically.

---

## 2. Use Nouns for Endpoint Paths

Endpoints should refer to resources using nouns, and the HTTP method should define the action. Always use plural nouns for consistency.

| Good Practice | Bad Practice |
| :--- | :--- |
| `GET /users` | `GET /getAllUsers` |
| `POST /users` | `POST /createUser` |
| `GET /users/{id}` | `GET /getUserById/{id}` |

---

## 3. Use HTTP Methods Correctly

Use the appropriate HTTP verb for the action being performed.

| Method | Action | Description | Idempotent |
| :--- | :--- | :--- | :--- |
| **GET** | Read | Retrieves a representation of a resource. | Yes |
| **POST** | Create | Creates a new resource. | No |
| **PUT** | Update/Replace | Replaces an existing resource entirely. If the resource does not exist, it may be created. | Yes |
| **PATCH**| Partial Update | Applies a partial modification to a resource. | No |
| **DELETE**| Delete | Deletes a specified resource. | Yes |

**Idempotency:** An operation is idempotent if making the same request multiple times produces the same result as making it once. `POST` is not idempotent because calling it multiple times will create multiple resources. `PUT` and `DELETE` are idempotent.

---

## 4. Use HTTP Status Codes Appropriately

Return standard HTTP status codes to indicate the outcome of a request. This helps clients handle responses correctly.

| Code Range | Category | Common Codes |
| :--- | :--- | :--- |
| **2xx** | Success | `200 OK`, `201 Created`, `204 No Content` |
| **3xx** | Redirection | `301 Moved Permanently`, `304 Not Modified` |
| **4xx** | Client Error | `400 Bad Request`, `401 Unauthorized`, `403 Forbidden`, `404 Not Found`, `429 Too Many Requests` |
| **5xx** | Server Error | `500 Internal Server Error`, `502 Bad Gateway`, `503 Service Unavailable` |

---

## 5. Provide Clear and Consistent Data Formatting

### JSON Naming Conventions
Use **camelCase** for JSON property keys for consistency with JavaScript conventions.

**Good:**
```json
{
  "firstName": "John",
  "lastName": "Doe"
}
```

**Bad:**
```json
{
  "first_name": "John",
  "LastName": "Doe"
}
```

### Standardized Error Responses
Provide a consistent and detailed error format to help developers debug.

```json
{
  "error": {
    "status": 404,
    "message": "User not found",
    "developerInfo": "The user with id '123' does not exist.",
    "moreInfo": "https://api.example.com/docs/errors/404"
  }
}
```

---

## 6. Support Filtering, Sorting, and Pagination

For collection endpoints, provide mechanisms to limit the amount of data returned.

- **Filtering:** `GET /users?status=active`
- **Sorting:** `GET /users?sort=-createdAt` (descending) or `GET /users?sort=createdAt` (ascending)
- **Pagination:** `GET /users?page=2&limit=50`

These features prevent performance bottlenecks and allow clients to request only the data they need.

---

## 7. API Versioning

When an API needs to evolve, versioning is essential to avoid breaking changes for existing clients.

### Versioning Strategies

| Strategy | Example | Pros | Cons |
| :--- | :--- | :--- | :--- |
| **URL Versioning** | `/api/v1/users` | Simple and explicit. Easy to use in a browser. | Violates the principle that a URI should refer to a unique resource. Can lead to messy URLs. |
| **Query Parameter** | `/api/users?version=1` | Simple to implement. | Can be messy. Caching proxies may not handle query parameters well. |
| **Custom Header** | `Accept: application/vnd.myapi.v1+json` | Keeps URLs clean. Aligns well with REST principles (content negotiation). | Less intuitive for manual testing in a browser. |

**Recommendation:** **Header-based versioning** is generally considered the purest RESTful approach. It keeps resource URIs consistent while allowing different representations of that resource to be served.

---

## 8. Security Best Practices

- **Use HTTPS:** Always encrypt communication between the client and server using TLS/SSL.
- **Authentication:** Verify the identity of the client.
    - **OAuth 2.0:** A robust framework for authorization, ideal for third-party access.
    - **JWT (JSON Web Tokens):** A compact, self-contained way for securely transmitting information between parties as a JSON object.
- **Authorization:** Ensure the authenticated client has permission to access the requested resource.
- **Input Validation:** Validate all incoming data to prevent security vulnerabilities like SQL injection and Cross-Site Scripting (XSS).
- **Rate Limiting:** Protect your API from abuse (both intentional and unintentional) by limiting the number of requests a client can make in a given time frame.

---

## 9. Documentation

Excellent documentation is as important as the API itself.
- **Use OpenAPI (formerly Swagger):** Define your API using the OpenAPI Specification. This allows you to generate interactive documentation, client SDKs, and server stubs automatically.
- **Provide Examples:** Include clear request and response examples for every endpoint.
- **Explain Authentication:** Clearly document how clients should authenticate with your API.
