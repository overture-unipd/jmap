package it.unipd.overture.port.in;

public interface AuthenticationPort {
  boolean authenticate(String auth);
}
