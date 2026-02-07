# API Design

https://scalabrix.medium.com/api-design-refresher-ecf55666ba5a

## API versioning

Breaking Changes Without Versioning:

Imagine you launch an API, and many clients integrate with it. Later, you realize the apiResponseWrapper structure is inefficient and decide to change the format.

What happens?
- Existing clients break because their code is expecting the old apiResponseWrapper structure.
- Developers are forced to update their implementations immediately.
- No rollback option, making debugging a nightmare. 

Versioning Strategies:

**Version in URL — /api/v1/products**

This is a common approach, but it has drawbacks:
- Hard to manage multiple versions simultaneously.
- Forces consumers to update their URLs when switching versions.
- Not very RESTful, as resources shouldn’t be tied to versions.

**Version in Query Params — /api/products?version=1**

This method is not widely recommended because:
- Caching mechanisms often ignore query parameters, leading to performance issues.
- Not user-friendly and prone to errors.

**Version in Headers — Accept: application/vnd.myapp.v1+json**

This is a better approach because:
- It keeps the URL clean and static.
- Uses content negotiation, aligning with REST principles.
- Allows seamless upgrades for different clients.

**Version in MIME type (also called content negotiation or media type versioning)**
