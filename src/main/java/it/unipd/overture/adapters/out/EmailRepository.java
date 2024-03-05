package it.unipd.overture.adapters.out;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;
import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;
import com.rethinkdb.utils.Types;
import it.unipd.overture.ports.out.EmailPort;
import java.util.List;
import java.util.Map;

public class EmailRepository implements EmailPort {
  private final RethinkDB r = RethinkDB.r;
  private final TypeReference<Map<String, Object>> stringObjectMap = Types.mapOf(String.class, Object.class);
  private Connection conn;

  @Inject
  EmailRepository(Connection conn) {
    this.conn = conn;
  }

  @Override
  public String get(String id) {
    return r.table("email").get(id).toJson().run(conn).single().toString();
  }

  @Override
  public String insert(String email) {
    Map<String, Object> res = r.table("email").insert(r.json(email)).run(conn, stringObjectMap).single();
    return ((List<?>) res.get("generated_keys")).get(0).toString();
  }

  @Override
  public void delete(String id) {
    r.table("email").get(id).delete().run(conn);
  }
}
