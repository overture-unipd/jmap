package it.unipd.overture.adapter.out;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import it.unipd.overture.port.out.AttachmentPort;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AttachmentRepositoryTest {
  private final MinIOContainer container = new MinIOContainer("minio/minio:RELEASE.2024-02-24T17-11-14Z");
  private MinioClient conn;
  private final String bucket = "jmap";

  @BeforeAll
  void setUp() {
    container.start();
    conn = MinioClient
        .builder()
        .endpoint(container.getS3URL())
        .credentials(container.getUserName(), container.getPassword())
        .build();
    try {
      conn.makeBucket(
        MakeBucketArgs.builder()
          .bucket(bucket)
          .build());
    } catch (Exception e) {
    }
  }

  @Test
  void testGetAttachment() {
    byte[] blob = { 1, 2, 3, 4 };
    AttachmentPort repo = new AttachmentRepository(conn, bucket);
    try {
      var name = UUID.randomUUID().toString();
      conn.putObject(
          PutObjectArgs.builder()
              .bucket(bucket)
              .object(name)
              .stream(new ByteArrayInputStream(blob), -1, Integer.MAX_VALUE)
              .build());
      Assertions.assertTrue(Arrays.equals(blob, repo.get(name)));
    } catch (Exception e) {
    }
  }

  @Test
  void testInsertAttachment() {
    byte[] blob = { 1, 2, 3, 4 };
    AttachmentPort repo = new AttachmentRepository(conn, bucket);
    String name = repo.insert(blob, "application/octet-stream", Long.valueOf(4));
    byte[] data = null;
    try {
      data = conn.getObject(
        GetObjectArgs.builder()
            .bucket(bucket)
            .object(name)
            .build()).readAllBytes();
    } catch (Exception e) {
    }
    Assertions.assertTrue(Arrays.equals(blob, data));
  }

  @Test
  void testDeleteAttachment() {
    byte[] blob = { 1, 2, 3, 4 };
    AttachmentPort repo = new AttachmentRepository(conn, bucket);
    try {
      var name = UUID.randomUUID().toString();
      conn.putObject(
          PutObjectArgs.builder()
              .bucket(bucket)
              .object(name)
              .stream(new ByteArrayInputStream(blob), -1, Integer.MAX_VALUE)
              .build());
      repo.delete(name);
      Assertions.assertEquals(null, repo.get(name));
    } catch (Exception e) {
    }
  }
}
