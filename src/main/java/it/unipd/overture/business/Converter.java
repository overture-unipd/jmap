package it.unipd.overture.business;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Provides;

import rs.ltt.jmap.gson.JmapAdapters;

public class Converter {
  private Gson gson;

  Converter() {
    var gsonBuilder = new GsonBuilder();
    JmapAdapters.register(gsonBuilder);
    this.gson = gsonBuilder.create();
  }

  @Provides
  public Gson provideGson() {
    return gson;
  }
}
