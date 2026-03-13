package org.atlas.libs.framework.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.type.MapType;

@UtilityClass
@Slf4j
public class JsonUtil {

  public static final JsonMapper JSON_MAPPER;

  static {
    // Dates and times are serialized as ISO‑8601 strings by default, so you don’t need to turn off WRITE_DATES_AS_TIMESTAMPS.
    JSON_MAPPER = JsonMapper.builder()
        .changeDefaultPropertyInclusion(
            include -> include.withValueInclusion(JsonInclude.Include.NON_NULL))
        .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
        .disable(
            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
            DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES
        )
        .findAndAddModules()
        .build();
  }

  public static Object toObject(String json) {
    return JSON_MAPPER.readValue(json, Object.class);
  }

  public static <T> T toObject(String json, Class<T> type) {
    return JSON_MAPPER.readValue(json, type);
  }

  public static <T> List<T> toList(String json, Class<T> type) {
    return JSON_MAPPER.readValue(json,
        JSON_MAPPER.getTypeFactory().constructCollectionType(List.class, type));
  }

  public static Map<String, Object> toMap(String json) {
    MapType mapType = JSON_MAPPER.getTypeFactory()
        .constructMapType(Map.class, String.class, Object.class);
    return JSON_MAPPER.readValue(json, mapType);
  }

  public static String getAsString(String json, String key) {
    JsonNode tree = JSON_MAPPER.readTree(json);
    JsonNode valueNode = tree.get(key);
    if (valueNode != null) {
      // If the node is a text node, use asText()
      if (valueNode.isString()) {
        return valueNode.asString();
      }
      // If the node is an object or array, return the JSON string representation
      else if (valueNode.isObject() || valueNode.isArray()) {
        return valueNode.toString();
      }
      // For other types (numbers, booleans, null), use asText()
      else {
        return valueNode.asString();
      }
    } else {
      log.warn("Key '{}' not found in the JSON", key);
      return StringUtil.EMPTY;
    }
  }

  public static Integer getAsInt(String json, String key) {
    String plainStr = getAsString(json, key);
    if (StringUtil.isBlank(plainStr)) {
      return null;
    }
    return Integer.parseInt(plainStr);
  }

  public static String compact(String json) {
    // Parse the JSON string to validate it and then write it back compactly
    JsonNode jsonNode = JSON_MAPPER.readTree(json);
    return JSON_MAPPER.writeValueAsString(jsonNode);
  }

  public static String toJson(Object source) {
    return JSON_MAPPER.writer().writeValueAsString(source);
  }
}
