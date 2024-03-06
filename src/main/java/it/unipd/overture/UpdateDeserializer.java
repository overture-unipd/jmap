package it.unipd.overture;

import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import rs.ltt.jmap.common.entity.AbstractIdentifiableEntity;
import rs.ltt.jmap.mock.server.Update;

import rs.ltt.jmap.mock.server.Changes;

public class UpdateDeserializer implements JsonDeserializer<Update> {
  @Override
  public Update deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'deserialize'");
  }
}

  /*
  Gson gson;

  UpdateDeserializer(Gson gson) {
    this.gson = gson;
  }

  @Override
  public Update deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    Type changes_type = new TypeToken<Map<Class<? extends AbstractIdentifiableEntity>, Changes>>(){}.getType();
    JsonObject jobject = json.getAsJsonObject();
    return new Update(
      gson.fromJson(gson.toJson(jobject.get("changes")), changes_type),
      jobject.get("newVersion").getAsString()
    );
  }
  */
