package it.unipd.overture.adapter.out;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.inject.Inject;

import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;
import com.rethinkdb.net.Result;

import it.unipd.overture.port.out.EmailPort;
import rs.ltt.jmap.common.entity.Email;

public class EmailRepository implements EmailPort {
  private final RethinkDB r = RethinkDB.r;
  private Connection conn;
  private Gson gson;

  @Inject
  EmailRepository(Connection conn, Gson gson) {
    this.conn = conn;
    this.gson = gson;
  }

  @Override
  public Email get(String id) {
    Result<Map> res = r.table("email")
                   .get(id)
                   .run(conn, Map.class);
    if (!res.hasNext()) return null;
    return gson.fromJson(gson.toJson(res.first()), Email.class);
  }

  @Override
  public Map<String, Email> getOf(String accountid) {
    Map<String, Email> map = new LinkedHashMap<>();
    var res = r.table("email")
              .getAll(accountid)
              .optArg("index", "account")
              .without("account")
              .run(conn);
    res.forEach(doc -> {
      var email = gson.fromJson(gson.toJson(doc, Map.class), Email.class);
      var id = email.getId();
      map.put(id, email);
    });
    return map;
  }

  @Override
  public void insert(String accountid, Email email) {
    r.table("email")
      .insert(r.json("{\"account\":\""+accountid+"\","+gson.toJson(email).substring(1)))
      .optArg("conflict", "replace")
      .run(conn);
  }

  @Override
  public void delete(String id) {
    r.table("email").get(id).delete().run(conn);
  }
}
