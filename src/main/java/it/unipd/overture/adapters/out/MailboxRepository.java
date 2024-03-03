package it.unipd.overture.adapters.out;

import java.util.List;
import java.util.Map;

import com.google.inject.Inject;
import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;

import it.unipd.overture.ports.out.MailboxPort;

import com.fasterxml.jackson.core.type.TypeReference;
import com.rethinkdb.utils.Types;

public class MailboxImpl implements MailboxPort {
  private final RethinkDB r = RethinkDB.r;
  private final TypeReference<Map<String, Object>> stringObjectMap = Types.mapOf(String.class, Object.class);
  private Connection conn;

  @Inject
  MailboxImpl(Connection conn) {
    this.conn = conn;
  }

  @Override
  public String getMailbox(String id) {
    return r.table("mailbox").get(id).toJson().run(conn).single().toString();
  }

  @Override
  public String getAccountMailboxes(String id) {
    return r.table("mailbox").getAll(id).optArg("index", "account").coerceTo("array").toJson().run(conn).single().toString();
  }

  @Override
  public String insertMailbox(String mailbox) {
    Map<String, Object> res = r.table("mailbox").insert(r.json(mailbox)).run(conn, stringObjectMap).single();
    return ((List<?>) res.get("generated_keys")).get(0).toString();
  }

  @Override
  public void deleteMailbox(String id) {
    r.table("mailbox").get(id).delete().run(conn);
  }
}