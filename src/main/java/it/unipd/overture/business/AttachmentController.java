package it.unipd.overture.business;

import com.google.inject.Inject;

import it.unipd.overture.ports.in.DownloadPort;
import it.unipd.overture.ports.in.UploadPort;

public class AttachmentController implements UploadPort, DownloadPort {
  AttachmentLogic attachmentLogic;

  @Inject
  AttachmentController(AttachmentLogic attachmentLogic) {
    this.attachmentLogic = attachmentLogic;
  }

  @Override
  public byte[] pull(String id) {
    return attachmentLogic.download(id);
  }

  @Override
  public String push(byte[] data) {
    return attachmentLogic.upload(data);
  }
}
