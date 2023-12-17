package it.unipd.overture.jmap;

import spark.Redirect;
import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.before;
import static spark.Spark.redirect;

public class Ignite {
  public static void main(String[] args) {
    var dispatcher = new Dispatcher();

    port(8000);

    get("/", (q, a) -> "Hi");

    redirect.get("/.well-known/jmap", "/api/jmap", Redirect.Status.MOVED_PERMANENTLY);
    before("/api/*", dispatcher::authenticate);
    get("/api/jmap", dispatcher::session);
    post("/api/jmap", dispatcher::jmap);
    post("/api/download", (q, a) -> "TODO");
    post("/api/upload", (q, a) -> "TODO");
    post("/api/upload", dispatcher::upload);
    get("/api/download", dispatcher::download);

    post("/mta", (q, a) -> "TODO");

    get("/reset", dispatcher::reset);
  }
}

