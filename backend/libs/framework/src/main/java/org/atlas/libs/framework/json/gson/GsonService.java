package org.atlas.libs.framework.json.gson;

import com.google.common.reflect.TypeToken;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.constant.CommonConstant;
import org.atlas.libs.framework.json.JsonService;
import org.atlas.libs.framework.util.StringUtil;

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
  public Object toObject(String json) {
    return JsonParser.parseString(json);
  }

  @Override
  public <T> T toObject(String json, Class<T> objectClass) {
    return gson.fromJson(json, objectClass);
  }

  @Override
  public <T> List<T> toList(String json, Class<T> type) {
    Type listType = new TypeToken<ArrayList<T>>() {
    }.getType();
    return gson.fromJson(json, listType);
  }

  @Override
  public Map<String, Object> toMap(String json) {
    Type mapType = new TypeToken<Map<String, Object>>() {
    }.getType();
    return gson.fromJson(json, mapType);
  }

  @Override
  public String getAsString(String json, String key) {
    JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
    boolean hasKey = jsonObject.has(key);
    if (hasKey) {
      var jsonElement = jsonObject.get(key);
      // If the element is a primitive (string, number, boolean), use getAsString()
      if (jsonElement.isJsonPrimitive()) {
        return jsonElement.getAsString();
      }
      // If the element is an object or array, return the JSON string representation
      else if (jsonElement.isJsonObject() || jsonElement.isJsonArray()) {
        return jsonElement.toString();
      }
      // For null values
      else if (jsonElement.isJsonNull()) {
        return StringUtil.EMPTY;
      }
      // Fallback
      else {
        return jsonElement.getAsString();
      }
    } else {
      log.warn("Key '{}' not found in the JSON", key);
      return StringUtil.EMPTY;
    }
  }

  @Override
  public Integer getAsInt(String json, String key) {
    String plainStr = getAsString(json, key);
    if (StringUtil.isBlank(plainStr)) {
      return null;
    }
    return Integer.parseInt(plainStr);
  }

  @Override
  public String compact(String json) {
    try {
      // Parse the JSON string to validate it and then write it back compactly
      JsonElement jsonElement = JsonParser.parseString(json);
      return gson.toJson(jsonElement);
    } catch (JsonSyntaxException e) {
      log.error("Failed to compact JSON", e);
      return json;
    }
  }

  @Override
  public String toJson(Object source) {
    return gson.toJson(source);
  }
}
