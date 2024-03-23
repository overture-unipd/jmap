package it.unipd.overture.adapter.out;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.inject.Inject;

import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;
import com.rethinkdb.net.Result;

import it.unipd.overture.port.out.MailboxPort;
import it.unipd.overture.service.MailboxInfo;

public class MailboxRepository implements MailboxPort {
  private final RethinkDB r = RethinkDB.r;
  private Connection conn;
  private Gson gson;

  @Inject
  MailboxRepository(Connection conn, Gson gson) {
    this.conn = conn;
    this.gson = gson;
  }

  @Override
  public MailboxInfo get(String id) {
    Result<Map> res = r.table("mailbox")
                   .get(id)
                   .without("account")
                   .run(conn, Map.class);
    Map t = res.next();
    if (t == null) return null;
    return gson.fromJson(gson.toJson(t), MailboxInfo.class);
  }

  @Override
  public Map<String, MailboxInfo> getOf(String accountid) {
    Map<String, MailboxInfo> map = new LinkedHashMap<>();
    var res = r.table("mailbox")
              .getAll(accountid)
              .optArg("index", "account")
              .without("account")
              .run(conn);
    res.forEach(doc -> {
      var mailbox = gson.fromJson(gson.toJson(doc, Map.class), MailboxInfo.class);
      var id = mailbox.getId();
      map.put(id, mailbox);
    });
    return map;
  }

  @Override
  public void insert(String accountid, MailboxInfo mailbox) {
    r.table("mailbox")
      .insert(r.json("{\"account\":\""+accountid+"\","+gson.toJson(mailbox).substring(1)))
      .optArg("conflict", "replace")
      .run(conn);
  }

  @Override
  public void delete(String id) {
    r.table("mailbox").get(id).delete().run(conn);
  }
}
