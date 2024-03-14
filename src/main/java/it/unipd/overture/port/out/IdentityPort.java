package it.unipd.overture.port.out;

import rs.ltt.jmap.common.entity.Identity;

public interface IdentityPort {
  Identity[] getOf(String accountid);
}
