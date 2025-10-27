package org.atlas.framework.json.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.json.JsonService;
import org.atlas.framework.util.StringUtil;

@Slf4j
public class JacksonService implements JsonService {

  public static final ObjectMapper OBJECT_MAPPER;

  static {
    OBJECT_MAPPER = new ObjectMapper();

    // Basics
    OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    OBJECT_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    // Date-time
    OBJECT_MAPPER.registerModule(new JavaTimeModule());
    OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Use ISO-8601 format
  }

  @Override
  public Object toObject(String source) {
    try {
      return OBJECT_MAPPER.readValue(source, Object.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public <T> T toObject(String source, Class<T> type) {
    try {
      return OBJECT_MAPPER.readValue(source, type);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public <T> T toObject(LinkedHashMap<?, ?> source, Class<T> type) {
    return OBJECT_MAPPER.convertValue(source, type);
  }

  @Override
  public <T> List<T> toList(String source, Class<T> type) {
    try {
      return OBJECT_MAPPER.readValue(source,
          OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, type));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Map<String, Object> toMap(String source) {
    try {
      MapType mapType = OBJECT_MAPPER.getTypeFactory()
          .constructMapType(Map.class, String.class, Object.class);
      return OBJECT_MAPPER.readValue(source, mapType);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String toJson(Object source) {
    try {
      return OBJECT_MAPPER.writer().writeValueAsString(source);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String getAsString(String source, String key) {
    try {
      JsonNode tree = OBJECT_MAPPER.readTree(source);
      JsonNode valueNode = tree.get(key);
      if (valueNode != null) {
        // If the node is a text node, use asText()
        if (valueNode.isTextual()) {
          return valueNode.asText();
        }
        // If the node is an object or array, return the JSON string representation
        else if (valueNode.isObject() || valueNode.isArray()) {
          return valueNode.toString();
        }
        // For other types (numbers, booleans, null), use asText()
        else {
          return valueNode.asText();
        }
      } else {
        log.warn("Key '{}' not found in the JSON", key);
        return StringUtil.EMPTY;
      }
    } catch (JsonProcessingException e) {
      log.error("Failed to parse JSON", e);
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
      JsonNode jsonNode = OBJECT_MAPPER.readTree(source);
      return OBJECT_MAPPER.writeValueAsString(jsonNode);
    } catch (JsonProcessingException e) {
      log.error("Failed to compact JSON", e);
      return source;
    }
  }
}
