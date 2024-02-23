package it.unipd.overture.adapters.out;

import it.unipd.overture.ports.out.Email;

public class EmailImpl implements EmailPort {
  String getEmail(String id) {
    return "";
  }

  String insertEmail(String email) {
    return "";
  }

  void deleteEmail(String id) {
    return ;
  }

  /*
  public void reset(LinkedList<String[]> accounts, String domain) {
    try {
      r.dbDrop(db).run(conn);
    } catch (ReqlRuntimeError e) {
    }

    r.dbCreate(db).run(conn);

    r.tableCreate("account").run(conn);
    r.table("account").indexCreate("address").run(conn);
    for (var acc : accounts) {
      r.table("account").insert(
        r.hashMap("address", acc[0]+"@"+domain)
          .with("name", acc[0])
          .with("password", acc[1])
          .with("state", "0")
      ).run(conn);
    }

    r.tableCreate("email").run(conn);
    r.tableCreate("mailbox").run(conn);
    r.tableCreate("attachment").run(conn);
    r.tableCreate("update").run(conn);
  }

  public String getAccountName(String id) {
    return r.table("account").get(id).run(conn, Properties.class).single().getProperty("name");
  }

  public String getAccountAddress(String id) {
    return r.table("account").get(id).run(conn, Properties.class).single().getProperty("address");
  }

  public String getAccountPassword(String id) {
    var t = r.table("account").get(id).run(conn).first();
    return gson.fromJson(gson.toJson(t), Properties.class).getProperty("password");
  }

  public String getAccountId(String address) {
    var t = r.table("account").getAll(address).optArg("index", "address").run(conn).first();
    return gson.fromJson(gson.toJson(t), Properties.class).getProperty("id");
  }

  public String getAccountState(String accountid) {
    Properties t = r.table("account").get(accountid).run(conn, Properties.class).single();
    return t.getProperty("state");
  }

  public void incrementAccountState(String accountid) {
    r.table("account").get(accountid).update(
        a -> r.hashMap("state", a.g("state").coerceTo("number").add(1))
    ).run(conn);
  }

  public byte[] getAttachment(String id) {
    // Object cursor = r.table("attachment").get(id).pluck("content").toJson().run(conn, OptArgs.of("binary_format", "raw")).single();
    var cursor = r.table("attachment").get(id).pluck("content").toJson().run(conn);
    var blob = gson.fromJson(cursor.first().toString(), JsonObject.class)
      .getAsJsonObject("content")
      .get("data")
      .getAsString();
      return Base64.getDecoder().decode(blob.getBytes());
  }

  public String getAccountEmails(String address) {
    var t = r.table("email").filter(row -> row.g("from").contains(email -> email.g("email").eq(address))
      .or(row.g("to").contains(email -> email.g("email").eq(address)))).run(conn);
    return gson.toJson(t);
  }

  public String insertAttachment(byte[] content) {
    var key = r.table("attachment").insert(
      r.hashMap("content", content)
    ).toJson().run(conn);
    return gson.fromJson(key.first().toString(), JsonObject.class).get("generated_keys").getAsString();
  }

  public List<String> getTable(String table) {
    Result<Map<String, Object>> cursor = r.table(table).run(conn, stringObjectMap);
    List<String> res = new LinkedList<>();
    for (var el : cursor) {
      res.add(gson.toJson(el));
    }
    cursor.close();
    return res;
  }

  public String insertInTable(String table, String content) {
    Map<String, Object> res = r.table(table).insert(r.json(content)).run(conn, stringObjectMap).single();
    var keys = ((List<?>) res.get("generated_keys"));
    if (keys != null) {
      return keys.get(0).toString();
    }
    return null;
  }

  public String replaceInTable(String table, String id, String content) {
    Map<String, Object> res = r.table(table).get(id).replace(r.json(content)).run(conn, stringObjectMap).single();
    var keys = ((List<?>) res.get("generated_keys"));
    if (keys != null) {
      return keys.get(0).toString();
    }
    return null;
  }
  */
}
