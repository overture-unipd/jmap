package it.unipd.overture.adapters.out;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;
import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;
import com.rethinkdb.utils.Types;

import it.unipd.overture.ports.out.EmailSubmissionPort;

public class EmailSubmissionRepository implements EmailSubmissionPort {
  private final RethinkDB r = RethinkDB.r;
  private final TypeReference<Map<String, Object>> stringObjectMap = Types.mapOf(String.class, Object.class);
  private Connection conn;

  @Inject
  EmailSubmissionRepository(Connection conn) {
    this.conn = conn;
  }

  @Override
  public String get(String id) {
    return r.table("submission").getAll(id).optArg("index", "threadId").coerceTo("array").toJson().run(conn).single().toString();
  }

  @Override
  public String insert(String submission) {
    Map<String, Object> res = r.table("submission").insert(r.json(submission)).run(conn, stringObjectMap).single();
    return ((List<?>) res.get("generated_keys")).get(0).toString();
  }
}
