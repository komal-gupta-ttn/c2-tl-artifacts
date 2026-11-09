# API Rate Limits

## Payments API

- Default limit: 100 requests per minute per API key.
- Burst allowance: 20 requests.
- Exceeding the limit returns `429 Too Many Requests` with a `Retry-After` header.

## Auth API

- Login: 10 requests per minute per IP.
- Refresh: 10 requests per minute per IP.
- Registration: 5 requests per minute per IP.

## General

All other APIs: 300 requests per minute per API key.

Rate limit headers:
- `X-RateLimit-Limit`
- `X-RateLimit-Remaining`
- `X-RateLimit-Reset`
