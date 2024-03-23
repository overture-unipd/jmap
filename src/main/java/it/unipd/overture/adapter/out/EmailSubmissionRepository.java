package it.unipd.overture.adapter.out;

import java.util.Map;

import com.google.gson.Gson;
import com.google.inject.Inject;

import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;
import com.rethinkdb.net.Result;

import it.unipd.overture.port.out.EmailSubmissionPort;
import rs.ltt.jmap.common.entity.EmailSubmission;

public class EmailSubmissionRepository implements EmailSubmissionPort {
  private final RethinkDB r = RethinkDB.r;
  private Connection conn;
  private Gson gson;

  @Inject
  EmailSubmissionRepository(Connection conn, Gson gson) {
    this.conn = conn;
    this.gson = gson;
  }

  @Override
  public EmailSubmission get(String id) {
    Result<Map> res = r.table("emailsubmission")
                   .get(id)
                   .run(conn, Map.class);
    if (! res.hasNext()) return null;
    Map t = res.first();
    return gson.fromJson(gson.toJson(t), EmailSubmission.class);
  }

  @Override
  public void insert(String accountid, String id, EmailSubmission emailSubmission) {
    r.table("emailsubmission")
      .insert(r.json("{\"account\":\""+accountid+"\",\"id\":\""+id+"\","+gson.toJson(emailSubmission).substring(1)))
      .optArg("conflict", "replace")
      .run(conn);
  }
}
