package it.unipd.overture.adapters.out;

public class Configuration {
    this(
      System.getenv("DB_HOST"),
      Integer.parseInt(System.getenv("DB_PORT")),
      System.getenv("DB_NAME")
    );
}
