# Secrets Management — Production

## Strategy

All production secrets are stored in AWS Secrets Manager and accessed at runtime via the application's IAM role. No secrets are committed to version control.

## Rotation

- Database passwords: rotated every 30 days automatically.
- API keys: rotated every 90 days.
- TLS certificates: managed by ACM, auto-renewed.

## Access

Services access secrets through the Secrets Manager SDK with least-privilege IAM policies scoped to specific secret ARNs.
