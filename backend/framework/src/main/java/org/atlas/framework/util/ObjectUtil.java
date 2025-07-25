package org.atlas.framework.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ObjectUtil {

  /**
   * Creates a deep copy of the provided object using serialization.
   * The method serializes the input object to a byte stream and then deserializes
   * it to create a new instance with no shared references to the original object.
   * All objects in the object graph must implement {@link java.io.Serializable}.
   *
   * @param <T>    the type of the object to clone
   * @param object the object to be deeply cloned, or null
   * @return a deep copy of the input object, or null if the input is null
   * @throws java.io.IOException    if an I/O error occurs during serialization or deserialization
   * @throws ClassNotFoundException if a class in the object graph cannot be found during deserialization
   */
  @SuppressWarnings("unchecked")
  public static <T> T deepClone(T object) throws IOException, ClassNotFoundException {
    // Handle null input
    if (object == null) {
      return null;
    }

    // Serialize the object to a byte array
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try (ObjectOutputStream out = new ObjectOutputStream(bos)) {
      out.writeObject(object);
    }

    // Deserialize the byte array to create a new object
    ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
    try (ObjectInputStream in = new ObjectInputStream(bis)) {
      return (T) in.readObject();
    }
  }
}
