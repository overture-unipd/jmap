package it.unipd.overture;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import rs.ltt.jmap.common.entity.AbstractIdentifiableEntity;
import rs.ltt.jmap.mock.server.Changes;

public class UpdateDeserializer implements JsonDeserializer<Update> {
  @Override
  public Update deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    JsonObject updateObject = json.getAsJsonObject();

    // Get changes map
    Map<Class<? extends AbstractIdentifiableEntity>, Changes> changesMap = new HashMap<>();
    JsonObject changesJson = updateObject.getAsJsonObject("changes");
    if (changesJson != null) {
      for (Map.Entry<String, JsonElement> entry : changesJson.entrySet()) {
        try {
          Class<?> entityClass = Class.forName(entry.getKey());
          Changes changes = context.deserialize(entry.getValue(), Changes.class);
          changesMap.put((Class<? extends AbstractIdentifiableEntity>) entityClass, changes);
        } catch (ClassNotFoundException e) {
          throw new JsonParseException("Invalid entity class name: " + entry.getKey(), e);
        }
      }
    }

    // Get newVersion
    String newVersion = updateObject.get("newVersion").getAsString();

    return new Update(changesMap, newVersion);
  }
}
