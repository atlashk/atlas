package org.atlas.framework.json.gson;

import com.google.common.reflect.TypeToken;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.constant.CommonConstant;
import org.atlas.framework.json.JsonService;
import org.atlas.framework.util.StringUtil;

@Slf4j
public class GsonService implements JsonService {

  private static final Gson gson;

  static {
    GsonBuilder gsonBuilder = new GsonBuilder();
    gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
    // By default, Gson excludes fields with null values during serialization,
    // and ignores unknown properties
    gsonBuilder.setDateFormat(CommonConstant.DATE_TIME_FORMAT);
    gson = gsonBuilder.create();
  }

  @Override
  public Object toObject(String source) {
    return JsonParser.parseString(source);
  }

  @Override
  public <T> T toObject(String source, Class<T> objectClass) {
    return gson.fromJson(source, objectClass);
  }

  @Override
  public <T> T toObject(LinkedHashMap<?, ?> source, Class<T> type) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> List<T> toList(String source, Class<T> type) {
    Type listType = new TypeToken<ArrayList<T>>() {
    }.getType();
    return gson.fromJson(source, listType);
  }

  @Override
  public Map<String, Object> toMap(String source) {
    Type mapType = new TypeToken<Map<String, Object>>() {
    }.getType();
    return gson.fromJson(source, mapType);
  }

  @Override
  public String toJson(Object source) {
    return gson.toJson(source);
  }

  @Override
  public String getAsString(String source, String key) {
    JsonObject jsonObject = JsonParser.parseString(source).getAsJsonObject();
    boolean hasKey = jsonObject.has(key);
    if (hasKey) {
      return jsonObject.get(key).getAsString();
    } else {
      log.warn("Key '{}' not found in the JSON", key);
      return StringUtil.EMPTY;
    }
  }
}
