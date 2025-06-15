package org.atlas.framework.json;

import java.util.LinkedHashMap;
import java.util.List;

public interface JsonService {

  <T> T toObject(String source, Class<T> type);

  <T> T toObject(LinkedHashMap<?, ?> source, Class<T> type);

  <T> List<T> toList(String source, Class<T> type);

  String toJson(Object source);

  Object getNodeValue(String json, String key);
}
