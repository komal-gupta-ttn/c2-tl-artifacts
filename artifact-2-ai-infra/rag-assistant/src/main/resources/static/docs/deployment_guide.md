# Deployment Guide

## Deploying to Staging

1. Create a feature branch from `main`.
2. Open a PR; require at least one TL review.
3. CI runs unit + integration tests; must pass.
4. Merge to `main` triggers the staging pipeline.
5. Pipeline builds the Docker image, runs security scans, deploys to staging.
6. Run smoke tests against staging; promote to production on approval.

## Rollback

If staging smoke tests fail:
- Revert the merge commit.
- The previous image remains tagged `staging-previous` for instant rollback.
