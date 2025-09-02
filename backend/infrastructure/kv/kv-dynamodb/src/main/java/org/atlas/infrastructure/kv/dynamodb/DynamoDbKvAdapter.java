package org.atlas.infrastructure.kv.dynamodb;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.json.JsonUtil;
import org.atlas.framework.kv.KvPort;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
@Slf4j
public class DynamoDbKvAdapter implements KvPort {

  private final DynamoDbClient dynamoDbClient;

  @Override
  public void put(String storeName, String key, Object value) {
    try {
      String tableName = storeName;
      Map<String, AttributeValue> item = createItem(key, value, null);

      PutItemRequest request = PutItemRequest.builder()
          .tableName(tableName)
          .item(item)
          .build();

      dynamoDbClient.putItem(request);
      log.debug("Successfully put item with key: {} in table: {}", key, tableName);
    } catch (Exception e) {
      throw new RuntimeException("Failed to put item with key: " + key, e);
    }
  }

  @Override
  public void put(String storeName, String key, Object value, Duration expiration) {
    try {
      String tableName = storeName;
      long ttl = Instant.now().plus(expiration).getEpochSecond();
      Map<String, AttributeValue> item = createItem(key, value, ttl);

      PutItemRequest request = PutItemRequest.builder()
          .tableName(tableName)
          .item(item)
          .build();

      dynamoDbClient.putItem(request);
      log.debug("Successfully put item with key: {} and TTL: {} in table: {}", key, ttl, tableName);
    } catch (Exception e) {
      throw new RuntimeException("Failed to put item with key: " + key + " and expiration: " + expiration,
          e);
    }
  }

  @Override
  public boolean putIfAbsent(String storeName, String key, Object value) {
    try {
      String tableName = storeName;
      Map<String, AttributeValue> item = createItem(key, value, null);

      PutItemRequest request = PutItemRequest.builder()
          .tableName(tableName)
          .item(item)
          .conditionExpression("attribute_not_exists(#k)")
          .expressionAttributeNames(Map.of("#k", "key"))
          .build();

      dynamoDbClient.putItem(request);
      log.debug("Successfully put item if absent with key: {} in table: {}", key, tableName);
      return true;
    } catch (ConditionalCheckFailedException e) {
      log.debug("Item already exists with key: {} in table: {}", key, storeName);
      return false;
    } catch (Exception e) {
      throw new RuntimeException("Failed to put item if absent with key: " + key, e);
    }
  }

  @Override
  public boolean putIfAbsent(String storeName, String key, Object value, Duration expiration) {
    try {
      String tableName = storeName;
      long ttl = Instant.now().plus(expiration).getEpochSecond();
      Map<String, AttributeValue> item = createItem(key, value, ttl);

      PutItemRequest request = PutItemRequest.builder()
          .tableName(tableName)
          .item(item)
          .conditionExpression("attribute_not_exists(#k)")
          .expressionAttributeNames(Map.of("#k", "key"))
          .build();

      dynamoDbClient.putItem(request);
      log.debug("Successfully put item if absent with key: {} and TTL: {} in table: {}", key, ttl,
          tableName);
      return true;
    } catch (ConditionalCheckFailedException e) {
      log.debug("Item already exists with key: {} in table: {}", key, storeName);
      return false;
    } catch (Exception e) {
      throw new RuntimeException(
          "Failed to put item if absent with key: " + key + " and expiration: " + expiration, e);
    }
  }

  @Override
  public Optional<Object> get(String storeName, String key) {
    try {
      String tableName = storeName;
      GetItemRequest request = GetItemRequest.builder()
          .tableName(tableName)
          .key(Map.of("key", AttributeValue.builder().s(key).build()))
          .build();

      GetItemResponse response = dynamoDbClient.getItem(request);

      if (!response.hasItem() || response.item().isEmpty()) {
        return Optional.empty();
      }

      Map<String, AttributeValue> item = response.item();

      // Check TTL if present
      if (item.containsKey("ttl")) {
        long ttl = Long.parseLong(item.get("ttl").n());
        if (Instant.now().getEpochSecond() > ttl) {
          // Item has expired, delete it and return empty
          delete(storeName, key);
          return Optional.empty();
        }
      }

      String valueJson = item.get("value").s();
      Object value = JsonUtil.getInstance().toObject(valueJson);

      log.debug("Successfully retrieved item with key: {} from table: {}", key, tableName);
      return Optional.of(value);
    } catch (Exception e) {
      throw new RuntimeException(
          "Failed to get item with key: " + key + " from table: " + storeName, e);
    }
  }

  @Override
  public boolean exists(String storeName, String key) {
    try {
      String tableName = storeName;
      GetItemRequest request = GetItemRequest.builder()
          .tableName(tableName)
          .key(Map.of("key", AttributeValue.builder().s(key).build()))
          .projectionExpression("#k")
          .expressionAttributeNames(Map.of("#k", "key"))
          .build();

      GetItemResponse response = dynamoDbClient.getItem(request);

      if (!response.hasItem() || response.item().isEmpty()) {
        return false;
      }

      Map<String, AttributeValue> item = response.item();

      // Check TTL if present
      if (item.containsKey("ttl")) {
        long ttl = Long.parseLong(item.get("ttl").n());
        if (Instant.now().getEpochSecond() > ttl) {
          // Item has expired, delete it
          delete(storeName, key);
          return false;
        }
      }

      log.debug("Item exists with key: {} in table: {}", key, tableName);
      return true;
    } catch (Exception e) {
      throw new RuntimeException(
          "Failed to check existence of item with key: " + key + " in table: " + storeName, e);
    }
  }

  @Override
  public boolean delete(String storeName, String key) {
    try {
      String tableName = storeName;
      DeleteItemRequest request = DeleteItemRequest.builder()
          .tableName(tableName)
          .key(Map.of("key", AttributeValue.builder().s(key).build()))
          .build();

      dynamoDbClient.deleteItem(request);
      log.debug("Successfully deleted item with key: {} from table: {}", key, tableName);
      return true;
    } catch (ResourceNotFoundException e) {
      log.debug("Item not found for deletion with key: {} in table: {}", key, storeName);
      return false;
    } catch (Exception e) {
      throw new RuntimeException(
          "Failed to delete item with key: " + key + " from table: " + storeName, e);
    }
  }

  private Map<String, AttributeValue> createItem(String key, Object value, Long ttl) {
    Map<String, AttributeValue> item = new HashMap<>();
    item.put("key", AttributeValue.builder().s(key).build());

    String valueJson = JsonUtil.getInstance().toJson(value);
    item.put("value", AttributeValue.builder().s(valueJson).build());

    if (ttl != null) {
      item.put("ttl", AttributeValue.builder().n(String.valueOf(ttl)).build());
    }

    return item;
  }
}
