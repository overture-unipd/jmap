package it.unipd.overture.port.in;

public interface AuthenticationPort {
  Boolean authenticate(String auth);
}
