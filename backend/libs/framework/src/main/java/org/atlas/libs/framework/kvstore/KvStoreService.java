package org.atlas.libs.framework.kvstore;

import java.time.Duration;
import java.util.Optional;

/**
 * Port interface for key-value store operations. Provides basic CRUD operations and additional
 * utility methods for key-value storage.
 */
public interface KvStoreService {

  /**
   * Stores a key-value pair.
   *
   * @param storeName the store name to specify which store to use
   * @param key       the key to store
   * @param value     the value to associate with the key
   */
  void put(String storeName, String key, Object value);

  /**
   * Stores a key-value pair with expiration time.
   *
   * @param storeName  the store name to specify which store to use
   * @param key        the key to store
   * @param value      the value to associate with the key
   * @param expiration the duration after which the key-value pair expires
   */
  void put(String storeName, String key, Object value, Duration expiration);

  /**
   * Stores a key-value pair only if the key does not already exist.
   *
   * @param storeName the store name to specify which store to use
   * @param key       the key to store
   * @param value     the value to associate with the key
   * @return true if the key-value pair was stored, false if the key already existed
   */
  boolean putIfAbsent(String storeName, String key, Object value);

  /**
   * Stores a key-value pair with expiration time only if the key does not already exist.
   *
   * @param storeName  the store name to specify which store to use
   * @param key        the key to store
   * @param value      the value to associate with the key
   * @param expiration the duration after which the key-value pair expires
   * @return true if the key-value pair was stored, false if the key already existed
   */
  boolean putIfAbsent(String storeName, String key, Object value, Duration expiration);

  /**
   * Retrieves the value associated with the given key.
   *
   * @param storeName the store name to specify which store to use
   * @param key       the key to look up
   * @return an Optional containing the value if found, empty otherwise
   */
  Optional<Object> get(String storeName, String key);

  /**
   * Checks if a key exists in the store.
   *
   * @param storeName the store name to specify which store to use
   * @param key       the key to check
   * @return true if the key exists, false otherwise
   */
  boolean exists(String storeName, String key);

  /**
   * Removes a key-value pair from the store.
   *
   * @param storeName the store name to specify which store to use
   * @param key       the key to remove
   * @return true if the key was removed, false if it didn't exist
   */
  boolean delete(String storeName, String key);
}
