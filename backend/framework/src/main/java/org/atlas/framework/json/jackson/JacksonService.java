package org.atlas.framework.json.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.json.JsonService;
import org.atlas.framework.util.StringUtil;

@Slf4j
public class JacksonService implements JsonService {

  public static final ObjectMapper objectMapper;

  static {
    objectMapper = new ObjectMapper();

    // Basics
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    // Date-time
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Use ISO-8601 format
  }

  @Override
  public <T> T toObject(String source, Class<T> type) {
    try {
      return objectMapper.readValue(source, type);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public <T> T toObject(LinkedHashMap<?, ?> source, Class<T> type) {
    return objectMapper.convertValue(source, type);
  }

  @Override
  public <T> List<T> toList(String source, Class<T> type) {
    try {
      return objectMapper.readValue(source,
          objectMapper.getTypeFactory().constructCollectionType(List.class, type));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String toJson(Object source) {
    try {
      return objectMapper.writer().writeValueAsString(source);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String getNodeValue(String json, String key) {
    try {
      JsonNode tree = objectMapper.readTree(json);
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
