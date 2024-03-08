package it.unipd.overture.controller;

import com.google.inject.Inject;

import it.unipd.overture.port.in.DownloadPort;
import it.unipd.overture.port.in.UploadPort;
import it.unipd.overture.service.AttachmentLogic;

public class AttachmentController implements UploadPort, DownloadPort {
  private AttachmentLogic attachmentLogic;

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
