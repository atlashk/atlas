package org.atlas.framework.util;

import java.io.Serializable;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.SerializationUtils;

@UtilityClass
public class ObjectUtil {

  /**
   * Creates a deep copy of the provided object using serialization. The method serializes the input
   * object to a byte stream and then deserializes it to create a new instance with no shared
   * references to the original object. All objects in the object graph must implement
   * {@link java.io.Serializable}.
   *
   * @param <T>    the type of the object to clone
   * @param object the object to be deeply cloned, or null
   * @return a deep copy of the input object, or null if the input is null
   */
  public static <T extends Serializable> T deepClone(T object) {
    return SerializationUtils.clone(object);
  }
}
