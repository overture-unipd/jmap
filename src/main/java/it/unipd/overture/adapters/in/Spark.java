package it.unipd.overture.adapters.in;

import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.port;
import static spark.Spark.post;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import it.unipd.overture.ports.in.MethodPort;
import spark.Request;
import spark.Response;
import spark.utils.IOUtils;

public class Spark {
  private final MethodPort request;

  Spark(MethodPort request) {
    this.request = request;
  }

  private String authenticate(Request q, Response a) {
    if (! request.authenticate(q.headers("Authorization"))) {
      halt(401, "Wrong credentials.");
    }
    return null;
  }

  private String up(Request q, Response a) {
    return "Spark server is up";
  }

  private String wellKnown(Request q, Response a) {
    a.redirect("/api/jmap");// , Redirect.Status.MOVED_PERMANENTLY);
    return null;
  }

  private String getJmap(Request q, Response a) {
    a.type("application/json");
    return request.session(q.headers("Authorization"));
  }

  private String postJmap(Request q, Response a) {
    a.type("application/json");
    return request.jmap(q.body());
  }

  private String download(Request q, Response a) {
    a.type("application/octet-stream");
    var blobid = q.queryParams("blobid");
    var blob = request.download(blobid);
    if (blob == null) {
      halt(500, "Something went wrong with the download.");
    }
    try {
      var is = new ByteArrayInputStream(blob);
      var os = a.raw().getOutputStream();
      IOUtils.copy(is, os);
      is.close();
      os.close();
    } catch (IOException e) {
      a.body("Error");
    }
    return null;
  }

  private String upload(Request q, Response a) {
    a.type("application/json");
    var blob = q.bodyAsBytes();
    return request.upload(blob);
  }

  void start() {
    port(8000);

    before("/api/*", this::authenticate);
    get("/", this::up);
    get("/.well-known/jmap", this::wellKnown);
    get("/api/jmap", this::getJmap);
    post("/api/jmap", this::postJmap);
    get("/api/upload", this::upload);
    get("/api/download", this::download);
  }
}
