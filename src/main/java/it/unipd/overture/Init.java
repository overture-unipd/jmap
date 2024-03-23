package it.unipd.overture;

import com.google.inject.Guice;
import com.google.inject.Injector;

import it.unipd.overture.adapter.in.Spark;
public class Init {
  public static void main(String[] args) throws Exception {
    Injector injector = Guice.createInjector(new BindModule());

    Spark spark = injector.getInstance(Spark.class);
    spark.start();
  }
}
