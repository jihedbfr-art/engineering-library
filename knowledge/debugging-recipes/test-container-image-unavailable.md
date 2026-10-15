# Test container image unavailable before assertions

> An integration test fails while Testcontainers starts, with `404 pull access denied` for `minio/minio:latest`. The test has not made an S3 request yet.

Version française : [image-testconteneurs-introuvable.md](image-testconteneurs-introuvable.md).

## Likely causes

1. The image repository, tag, or registry changed; `latest` is no guarantee that an image remains available.
2. The registry rejects access (`401`) or rate limits pulls. This needs a different fix from a `404`.
3. The runner cannot reach the registry; the log then shows a timeout or network error.

## Diagnose step by step

```bash
# Copy the exact reference from the failed job, then test its availability.
docker manifest inspect minio/minio:latest

# For a candidate checked against its publisher's page:
docker manifest inspect adobe/s3mock:5.2.3
docker pull adobe/s3mock:5.2.3
docker image inspect adobe/s3mock:5.2.3 --format '{{json .RepoDigests}}'
```

Record the exact registry response. Check the repository owner, published tag, CI architecture, and digest. Never paste registry credentials into a public issue or log.

## Fix verified for this case

The `social-publisher-mcp` `SocialPublisherEndToEndIT` test used `minio/minio:latest`. The [fix](https://github.com/jihedbfr-art/engineering-library/pull/45) uses the versioned `adobe/s3mock:5.2.3` image from [Adobe S3Mock](https://github.com/adobe/S3Mock). After publishing text, it checks that the `media-staging` bucket exists. The application's Java MinIO client remains in use. The Maven suite passed locally with Docker and in CI. See the [test scope and limits](https://github.com/jihedbfr-art/engineering-library/blob/main/projects/top-projects/social-publisher-mcp/docs/testing-s3-backend.md).

This confirms the exercised path and bucket creation. It does not establish media upload or full production MinIO compatibility. Before replacing a different image, choose a substitute for the operations the test uses and add an assertion that fails when the service behaves incorrectly.

## If it still fails

If the pull succeeds but the test still fails, inspect container readiness, the S3 endpoint, and the assertions. If the registry returns `401`, check access rights without exposing secrets. For a container that starts and exits, see [docker-conteneur-exit.md](docker-conteneur-exit.md).
