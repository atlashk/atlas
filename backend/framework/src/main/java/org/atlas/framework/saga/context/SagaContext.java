package org.atlas.framework.saga.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.Setter;
import org.atlas.framework.json.JsonUtil;

public class SagaContext {

  @Getter
  @Setter
  private Long sagaId;

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

  public void clear() {
    data.clear();
  }

  public String serialize() {
    return JsonUtil.getInstance().toJson(data);
  }
}
