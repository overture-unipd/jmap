package it.unipd.overture.business;

public class Converter {
  private Gson gson;

  Converter() {
    var gsonBuilder = new GsonBuilder();
    JmapAdapters.register(gsonBuilder);
    gson = gsonBuilder.create();
  }

  String serialize(Object obj) {
  }

  String deserialize(Object obj) {
  }
}
