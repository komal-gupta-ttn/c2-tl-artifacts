# Auth Service

## Overview
The auth service handles user authentication, token issuance, and token refresh
for the platform. It issues short-lived access tokens (15 min) and long-lived
refresh tokens (7 days).

## Token Refresh Flow

1. Client presents a valid refresh token to `POST /auth/refresh`.
2. Service validates the refresh token signature and checks it against the
   revocation list.
3. If valid, a new access token is issued; the old refresh token is rotated
   (revoked and replaced) to prevent replay attacks.
4. Refresh tokens are stored hashed in the database; plaintext is never
   persisted.

## Rotation

Refresh token rotation is mandatory. On each refresh:
- The presented refresh token is revoked.
- A new refresh token is issued.
- Both access and refresh tokens are returned to the client.

## Errors

- `401` — refresh token expired or invalid
- `403` — refresh token revoked (possible replay)
- `429` — too many refresh attempts (rate limited per IP)

## Security

- Refresh tokens are hashed with bcrypt before storage.
- Access tokens are JWTs signed with RS256.
- Refresh token endpoints are rate-limited to 10 requests/minute per IP.
