package it.unipd.overture.adapters.out;

import java.io.ByteArrayInputStream;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import it.unipd.overture.adapters.out.MinioConnection.BucketName;
import it.unipd.overture.ports.out.AttachmentPort;
import java.util.UUID;

public class AttachmentImpl implements AttachmentPort {
  private MinioClient conn;
  private BucketName buck;

  AttachmentImpl(MinioClient conn, BucketName buck) {
    this.conn = conn;
    this.buck = buck;
  }

  @Override
  public byte[] getAttachment(String id) {
    try {
      return conn.getObject(
        GetObjectArgs.builder()
          .bucket(buck.getName())
          .object(id)
          .build()).readAllBytes();
    } catch (Exception e) {
    }
    return null;
  }

  @Override
  public boolean deleteAttachment(String id) {
    try {
      conn.removeObject(
        RemoveObjectArgs.builder()
          .bucket(buck.getName())
          .object(id)
          .build());
      return true;
    } catch (Exception e) {
    }
    return false;
  }

  @Override
  public String insertAttachment(byte[] data) {
    try {
      var name = UUID.randomUUID().toString();
      conn.putObject(
        PutObjectArgs.builder()
          .bucket(buck.getName())
          .object(name)
          .stream(new ByteArrayInputStream(data), 0, -1)
          .build());
        return name;
    } catch (Exception e) {
    }
    return null;
  }
}
