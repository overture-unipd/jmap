package it.unipd.overture;

import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import rs.ltt.jmap.common.entity.AbstractIdentifiableEntity;
import rs.ltt.jmap.mock.server.Changes;

public class UpdateSerializer implements JsonSerializer<Update> {
  @Override
  public JsonElement serialize(Update update, Type typeOfT, JsonSerializationContext context) {
    JsonObject updateObject = new JsonObject();

    // Serialize changes
    JsonObject changesObject = new JsonObject();
    for (Map.Entry<Class<? extends AbstractIdentifiableEntity>, Changes> entry : update.getChanges().entrySet()) {
      changesObject.add(entry.getKey().getName(), context.serialize(entry.getValue()));
    }
    updateObject.add("changes", changesObject);

    // Serialize newVersion
    updateObject.addProperty("newVersion", update.getNewVersion());

    return updateObject;
  }
}
