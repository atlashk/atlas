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
import java.util.LinkedHashMap;
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
  public Integer getAsInt(String source, String key) {
    String plainStr = getAsString(source, key);
    if (StringUtil.isBlank(plainStr)) {
      return null;
    }
    return Integer.parseInt(plainStr);
  }

  @Override
  public String compact(String source) {
    try {
      // Parse the JSON string to validate it and then write it back compactly
      JsonElement jsonElement = JsonParser.parseString(source);
      return gson.toJson(jsonElement);
    } catch (JsonSyntaxException e) {
      log.error("Failed to compact JSON", e);
      return source;
    }
  }
}
