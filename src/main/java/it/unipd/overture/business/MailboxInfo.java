package it.unipd.overture.business;

import rs.ltt.jmap.common.entity.IdentifiableMailboxWithRole;
import rs.ltt.jmap.common.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MailboxInfo implements IdentifiableMailboxWithRole {
  private final String id;
  private final String name;
  private final Role role;
  private final Boolean isSubscribed;
}
