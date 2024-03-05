package it.unipd.overture.business;

import com.google.gson.Gson;
import com.google.inject.Inject;
import it.unipd.overture.ports.out.AttachmentPort;
import rs.ltt.jmap.common.entity.Upload;

public class AttachmentLogic {
  Gson gson;
  AttachmentPort attachmentPort;

  @Inject
  AttachmentLogic(Gson gson, AttachmentPort attachmentPort) {
    this.gson = gson;
    this.attachmentPort = attachmentPort;
  }
  
  String upload(byte[] data) {
    var blobid = attachmentPort.insert(data);
    final Upload upload =
      Upload.builder()
        .blobId(blobid)
        .build();
      return gson.toJson(upload);
  }

  byte[] download(String id) {
    return attachmentPort.get(id);
  }
}
