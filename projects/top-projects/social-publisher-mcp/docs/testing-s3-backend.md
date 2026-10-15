# S3 integration test

[Version française](./testing-s3-backend.fr.md)

`SocialPublisherEndToEndIT` starts PostgreSQL and `adobe/s3mock:5.2.3` with Testcontainers. The application still uses its MinIO Java client; S3Mock supplies the S3 endpoint for this test. The test publishes a text post through the MOCK connector, checks the stored result and confirms that the media-staging bucket exists.

Run the project tests with Docker available:

```bash
mvn -pl app -am verify
```

This checks the S3 operations used by this text-publication path. It does not establish full compatibility with a production MinIO server, media upload behavior or presigned URL security. The image is pinned because a mutable `latest` tag made the earlier test dependent on a registry entry that is no longer public.

References: [Adobe S3Mock](https://github.com/adobe/S3Mock) and its [Docker Hub image](https://hub.docker.com/r/adobe/s3mock/tags).
