package it.unipd.overture.ports.in;

public interface AuthenticationPort {
  boolean authenticate(String auth);
}
