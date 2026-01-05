package org.atlas.common.framework.saga.core.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.atlas.common.framework.json.JsonUtil;
import org.atlas.common.framework.saga.core.exception.SagaExecutionException;

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

  public <T> T get(String key, Class<T> clazz) {
    Object value = data.get(key);
    if (value == null) {
      return null;
    }

    if (clazz.isInstance(value)) {
      return clazz.cast(value);
    }

    throw new SagaExecutionException(
        String.format("Value for key '%s' is of type %s, but expected type %s",
            key, value.getClass().getSimpleName(), clazz.getSimpleName()));
  }

  public void remove(String key) {
    data.remove(key);
  }

  public void clear() {
    data.clear();
  }

  public String serialize() {
    return JsonUtil.getInstance().toJson(data);
  }

  public static SagaContext deserialize(String serializedContext) {
    try {
      Map<String, Object> data = JsonUtil.getInstance().toMap(serializedContext);
      SagaContext sagaContext = new SagaContext();
      data.forEach(sagaContext::put);
      return sagaContext;
    } catch (Exception e) {
      throw new SagaExecutionException("Failed to deserialize saga context", e);
    }
  }
}
