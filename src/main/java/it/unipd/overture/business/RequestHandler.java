package it.unipd.overture.business;

import java.util.Base64;

import com.google.gson.Gson;
import com.google.inject.Inject;

import it.unipd.overture.ports.in.RequestPort;
import it.unipd.overture.ports.out.AccountPort;
import it.unipd.overture.ports.out.AttachmentPort;
import rs.ltt.jmap.common.entity.Upload;

public class RequestHandler implements RequestPort {
  AuthenticationHandler authentication;
  SessionHandler session;
  MethodHandler method;
  AttachmentHandler attachment;

  @Inject
  RequestHandler(
      AuthenticationHandler authentication,
      SessionHandler session,
      MethodHandler method,
      AttachmentHandler attachment
  ) {
    this.authentication = authentication;
    this.session = session;
    this.method = method;
    this.attachment = attachment;
  }

  @Override
  public Boolean authenticate(String auth) {
    return authentication.authenticate(auth);
  }

  @Override
  public String session(String token) {
    return session.get(token);
  }

  @Override
  public String jmap(String json) {
    return method.dispatch(json);
  }

  @Override
  public String upload(byte[] data) {
    return attachment.upload(data);
  }

  @Override
  public byte[] download(String id) {
    return attachment.download(id);
  }
}
