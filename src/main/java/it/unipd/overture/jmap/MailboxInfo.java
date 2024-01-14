package it.unipd.overture.jmap;

import rs.ltt.jmap.common.entity.IdentifiableMailboxWithRole;
import rs.ltt.jmap.common.entity.Role;
import lombok.Getter;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Getter
public class MailboxInfo implements IdentifiableMailboxWithRole {
  private final String id;
  private final String name;
  private final Role role;
}
