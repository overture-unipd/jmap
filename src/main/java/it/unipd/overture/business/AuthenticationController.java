package it.unipd.overture.business;

import java.util.Base64;
import com.google.inject.Inject;
import it.unipd.overture.ports.out.AccountPort;

public class AuthenticationController {
  AccountPort account;

  @Inject
  AuthenticationController(AccountPort account) {
    this.account = account;
  }

  boolean authenticate(String auth) {
    var encoded = auth.split(" ")[1];
    var decoded = new String(Base64.getDecoder().decode(encoded));
    String[] fields = decoded.split(":");
    var address = fields[0];
    var password = fields[1];
    return account.getPassword(account.getId(address)).equals(password);
  }
}
