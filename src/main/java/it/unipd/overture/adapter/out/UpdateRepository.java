package it.unipd.overture.adapter.out;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.inject.Inject;

import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;
import com.rethinkdb.net.Result;

import it.unipd.overture.Update;
import it.unipd.overture.port.out.UpdatePort;

public class UpdateRepository implements UpdatePort {
  private final RethinkDB r = RethinkDB.r;
  private Connection conn;
  private Gson gson;

  @Inject
  UpdateRepository(Connection conn, Gson gson) {
    this.conn = conn;
    this.gson = gson;
  }

  @Override
  public Update get(String accountid, String state) {
    Result<Map> res = r.table("update")
                            .getAll(accountid)
                            .optArg("index","account")
                            .without("account")
                            .filter(row -> row.g("state").eq(state))
                            .run(conn, Map.class);
    if (! res.hasNext()) return null;
    Map t = res.first();
    t.replace("id", t.get("state"));
    return gson.fromJson(gson.toJson(t), Update.class);
  }

  @Override
  public Map<String, Update> getOf(String accountid) {
    Map<String, Update> map = new LinkedHashMap<>();
    Map[] res = r.table("update")
                  .getAll(accountid)
                  .optArg("index", "account")
                  .orderBy("state")
                  .run(conn, Map[].class)
                  .single();
    for (var i : res) {
      i.replace("id", i.get("state"));
      map.put(i.get("state").toString(), gson.fromJson(gson.toJson(i), Update.class));
    }
    return map;
  }

  @Override
  public void insert(String accountid, String oldstate, Update update) {
    r.table("update")
      .insert(r.json("{\"state\":\""+oldstate+"\",\"account\":\""+accountid+"\","+gson.toJson(update).substring(1)))
      .optArg("conflict", "replace")
      .run(conn);
  }
}
