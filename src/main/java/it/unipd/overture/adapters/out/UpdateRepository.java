package it.unipd.overture.adapters.out;

import com.google.inject.Inject;
import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;

import it.unipd.overture.ports.out.UpdatePort;

public class UpdateRepository implements UpdatePort {
  private Connection conn;
  private final RethinkDB r = RethinkDB.r;

  @Inject
  UpdateRepository(Connection conn) {
    this.conn = conn;
  }

  @Override
  public String insert(String update) {
    return r.table("update").insert(update).run(conn).toString();
  }

  @Override
  public String get(String id) {
    return r.table("update").get(id).toJson().run(conn).single().toString();
  }

  @Override
  public void delete(String id) {
    r.table("update").get(id).delete().run(conn);
  }
}
