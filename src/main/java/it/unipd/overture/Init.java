package it.unipd.overture;

import com.google.inject.Guice;
import com.google.inject.Injector;

import it.unipd.overture.adapter.in.Spark;

public class Init {
  // guice init
  public static void main(String[] args) throws Exception {
    new RethinkDBConnection(
      System.getenv("DB_HOST"),
      Integer.parseInt(System.getenv("DB_PORT")),
      System.getenv("DB_DB")
    );

    new MinioConnection(
      System.getenv("MINIO_HOST"),
      Integer.parseInt(System.getenv("MINIO_PORT")),
      System.getenv("MINIO_BUCKET"),
      System.getenv("MINIO_ACCESS"),
      System.getenv("MINIO_SECRET")
    );

		// Injector injector = Guice.createInjector(new AppInjector());		
		
		// MyApplication app = injector.getInstance(MyApplication.class);
		
		// app.sendMessage("Hi Pankaj", "pankaj@abc.com");

    Spark spark = null;//  = new Spark();
    spark.start();
  }
}

/*
    Injector injector = Guice.createInjector(
        // new DatabaseModule(),
        // new WebserverModule(),
    );

    Service databaseConnectionPool = injector.getInstance(
        Key.get(Service.class, DatabaseService.class));
    databaseConnectionPool.start();
    addShutdownHook(databaseConnectionPool);

    Service webserver = injector.getInstance(
        Key.get(Service.class, WebserverService.class));
    webserver.start();
    addShutdownHook(webserver);
  }
}

// https://github.com/google/guice/wiki/Bindings
// https://github.com/google/guice/wiki/AvoidConditionalLogicInModules
// https://github.com/google/guice/wiki/Injections
// https://github.com/google/guice/wiki/BuiltInBindings
// https://github.com/google/guice/wiki/CyclicDependencies
// https://github.com/google/guice/wiki/KeepConstructorsHidden
// https://github.com/google/guice/wiki/Avoid-Injecting-Closable-Resources
// https://github.com/google/guice/wiki/PreferAtProvides
// https://github.com/google/guice/wiki/AvoidCallingProvideMethodsAndInjectConstructors
// https://github.com/google/guice/wiki/DontReuseAnnotations
// https://github.com/google/guice/wiki/OrganizeModulesByFeature
// https://github.com/google/guice/wiki/DocumentPublicBindings
// https://github.com/google/guice/wiki/FrequentlyAskedQuestions
// https://github.com/google/guice/wiki/ExternalDocumentation
// https://github.com/google/guice/wiki/AppsThatUseGuice

public interface Service {
  void start() throws Exception;

  void stop();
}

*/
