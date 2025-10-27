package org.atlas.framework.json;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface JsonService {

  Object toObject(String source);

  <T> T toObject(String source, Class<T> type);

  <T> T toObject(LinkedHashMap<?, ?> source, Class<T> type);

  <T> List<T> toList(String source, Class<T> type);

  Map<String, Object> toMap(String source);

  String toJson(Object source);

  String getAsString(String source, String key);

  Integer getAsInt(String source, String key);

  String compact(String source);
}
