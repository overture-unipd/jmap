package it.unipd.overture.service;

import com.google.inject.Inject;

import it.unipd.overture.port.out.AttachmentPort;
import rs.ltt.jmap.common.entity.Upload;

public class AttachmentLogic {
  private AttachmentPort attachmentPort;

  @Inject
  AttachmentLogic(AttachmentPort attachmentPort) {
    this.attachmentPort = attachmentPort;
  }
  
  public Upload upload(byte[] data, String contentType, Long size) {
    final String blobid = attachmentPort.insert(data, contentType, size);
    final Upload upload =
      Upload.builder()
        .accountId("")
        .blobId(blobid)
        .type(contentType)
        .size(size)
        .build();
    return upload;
  }

  public byte[] download(String id) {
    return attachmentPort.get(id);
  }
}
