package it.unipd.overture.adapter.out;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;
import com.rethinkdb.net.Result;

import it.unipd.overture.port.out.IdentityPort;
import rs.ltt.jmap.common.entity.Identity;

public class IdentityRepository implements IdentityPort {
  private final RethinkDB r = RethinkDB.r;
  private Connection conn;
  private Gson gson;

  @Inject
  IdentityRepository(Connection conn, Gson gson) {
    this.conn = conn;
    this.gson = gson;
  }

  @Override
  public Identity[] getOf(String accountid) {
    Result<Map> res = r.table("identity")
                    .getAll(accountid)
                    .optArg("index", "account")
                    .run(conn, Map.class);
    List<Identity> list = new ArrayList<>();
    res.forEach(doc -> list.add(gson.fromJson(gson.toJson(doc), Identity.class)));
    Identity[] ids = list.toArray(Identity[]::new);
    return ids;
  }
}
