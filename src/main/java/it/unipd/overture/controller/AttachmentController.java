package it.unipd.overture.controller;

import com.google.gson.Gson;
import com.google.inject.Inject;

import it.unipd.overture.port.in.DownloadPort;
import it.unipd.overture.port.in.UploadPort;
import it.unipd.overture.service.AttachmentLogic;

public class AttachmentController implements UploadPort, DownloadPort {
  private Gson gson;
  private AttachmentLogic attachmentLogic;

  @Inject
  AttachmentController(Gson gson, AttachmentLogic attachmentLogic) {
    this.gson = gson;
    this.attachmentLogic = attachmentLogic;
  }

  @Override
  public byte[] pull(String id) {
    if (id == null || id == "") {
      return null;
    }
    return attachmentLogic.download(id);
  }

  @Override
  public String push(byte[] data, String contentType, Long size) {
    if (contentType == null || contentType == "" || size == null || size == 0) {
      return null;
    }
    var upload = attachmentLogic.upload(data, contentType, size);
    System.out.println(upload);
    return gson.toJson(upload);
  }
}
