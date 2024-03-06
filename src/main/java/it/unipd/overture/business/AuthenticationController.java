package it.unipd.overture.business;

import java.util.Base64;

import com.google.inject.Inject;

import it.unipd.overture.ports.in.AuthenticationPort;

public class AuthenticationController implements AuthenticationPort {
  AuthenticationLogic authenticationLogic;

  @Inject
  AuthenticationController(AuthenticationLogic authenticationLogic) {
    this.authenticationLogic = authenticationLogic;
  }

  @Override
  public boolean authenticate(String auth) {
    var token = auth.split(" ");
    if (token.length != 2) {
      return false;
    }
    var decoded = new String(Base64.getDecoder().decode(token[1]));
    String[] fields = decoded.split(":");
    if (fields.length != 2) {
      return false;
    }
    return authenticationLogic.authenticate(fields[0], fields[1]);
  }
}
