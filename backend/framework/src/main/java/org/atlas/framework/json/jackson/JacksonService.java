package org.atlas.framework.json.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.LinkedHashMap;
import java.util.List;
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
  public String toJson(Object source) {
    try {
      return OBJECT_MAPPER.writer().writeValueAsString(source);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String getNodeValue(String json, String key) {
    try {
      JsonNode tree = OBJECT_MAPPER.readTree(json);
      JsonNode valueNode = tree.get(key);
      if (valueNode != null) {
        return valueNode.asText();
      } else {
        log.warn("Key '{}' not found in the JSON", key);
        return StringUtil.EMPTY;
      }
    } catch (JsonProcessingException e) {
      log.error("Failed to parse JSON", e);
      return StringUtil.EMPTY;
    }
  }
}
