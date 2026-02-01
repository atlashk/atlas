package org.atlas.libs.framework.json;

import java.util.List;
import java.util.Map;

public interface JsonService {

  Object toObject(String json);

  <T> T toObject(String json, Class<T> type);

  <T> List<T> toList(String json, Class<T> type);

  Map<String, Object> toMap(String json);

  String getAsString(String json, String key);

  Integer getAsInt(String json, String key);

  String compact(String json);

  String toJson(Object source);
}
