package org.atlas.libs.framework.saga.core.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.atlas.libs.framework.util.JsonUtil;
import org.atlas.libs.framework.saga.core.exception.SagaExecutionException;

public class SagaContext {

  private final Map<String, Object> data = new ConcurrentHashMap<>();

  public static SagaContext of(String key, Object value) {
    SagaContext context = new SagaContext();
    context.put(key, value);
    return context;
  }

  public void put(String key, Object value) {
    data.put(key, value);
  }

  public Object get(String key) {
    return data.get(key);
  }

  public void remove(String key) {
    data.remove(key);
  }

  public void clear() {
    data.clear();
  }

  public String serialize() {
    return JsonUtil.toJson(data);
  }

  public static SagaContext deserialize(String serializedContext) {
    try {
      Map<String, Object> data = JsonUtil.toMap(serializedContext);
      SagaContext sagaContext = new SagaContext();
      data.forEach(sagaContext::put);
      return sagaContext;
    } catch (Exception e) {
      throw new SagaExecutionException("Failed to deserialize saga context", e);
    }
  }
}
