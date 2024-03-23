import com.fasterxml.jackson.core.type.TypeReference;
import com.rethinkdb.RethinkDB;
import com.rethinkdb.gen.exc.ReqlError;
import com.rethinkdb.gen.exc.ReqlQueryLogicError;
import com.rethinkdb.gen.exc.ReqlRuntimeError;
import com.rethinkdb.model.MapObject;
import com.rethinkdb.model.OptArgs;
import com.rethinkdb.net.Connection;
import com.rethinkdb.net.Result;
import com.rethinkdb.utils.Types;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import rs.ltt.jmap.gson.JmapAdapters;
import it.unipd.overture.Update;
import it.unipd.overture.UpdateSerializer;
import it.unipd.overture.UpdateDeserializer;
import rs.ltt.jmap.common.entity.Email;

/set feedback verbose

RethinkDB r = RethinkDB.r;
Connection conn = r.connection().hostname("localhost").port(28015).connect().use("jmap");
var gsonBuilder = new GsonBuilder();
JmapAdapters.register(gsonBuilder);
gsonBuilder.registerTypeAdapter(Update.class, new UpdateSerializer());
gsonBuilder.registerTypeAdapter(Update.class, new UpdateDeserializer());
var gson = gsonBuilder.create();

final TypeReference<List<String>> stringList = Types.listOf(String.class);
final TypeReference<Map<String, Object>> stringObjectMap = Types.mapOf(String.class, Object.class);
