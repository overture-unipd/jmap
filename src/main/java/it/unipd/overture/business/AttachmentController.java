package it.unipd.overture.business;

import com.google.gson.Gson;
import com.google.inject.Inject;
import it.unipd.overture.ports.out.AttachmentPort;
import rs.ltt.jmap.common.entity.Upload;

public class AttachmentController {
  Gson gson;
  AttachmentPort attachmentPort;

  @Inject
  AttachmentController(Gson gson, AttachmentPort attachmentPort) {
    this.gson = gson;
    this.attachmentPort = attachmentPort;
  }
  
  String upload(byte[] data) {
    var blobid = attachmentPort.insertAttachment(data);
    final Upload upload =
      Upload.builder()
        .blobId(blobid)
        .build();
      return gson.toJson(upload);
  }

  byte[] download(String id) {
    return attachmentPort.getAttachment(id);
  }
}
