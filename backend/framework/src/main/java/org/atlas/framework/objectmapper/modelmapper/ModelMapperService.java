package org.atlas.framework.objectmapper.modelmapper;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.atlas.framework.objectmapper.ObjectMapperService;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

public class ModelMapperService implements ObjectMapperService {

  private final ModelMapper MAPPER;

  public ModelMapperService() {
    MAPPER = new ModelMapper();
    MAPPER.getConfiguration()
        .setMatchingStrategy(MatchingStrategies.STRICT);
  }

  /**
   * Maps a single object to an object of the specified destination type.
   */
  @Override
  public <D> D map(Object source, Class<D> destinationType) {
    if (source == null) {
      return null;
    }
    return MAPPER.map(source, destinationType);
  }

  /**
   * Maps a list of objects to a list of objects of the specified destination type.
   */
  @Override
  public <D> List<D> mapList(List<?> source, Class<D> destinationType) {
    if (source == null || source.isEmpty()) {
      return List.of();
    }
    return source.stream()
        .filter(Objects::nonNull)
        .map(it -> MAPPER.map(it, destinationType))
        .collect(Collectors.toList());
  }

  /**
   * Suppose you have a source object and a destination object, and you want to map properties from
   * the source to the destination. By default, ModelMapper will attempt to map all properties, even
   * those that are null in the source object. This configuration setting ensures that only non-null
   * properties from the source object are mapped to the destination object, preventing null values
   * from overwriting existing values in the destination object.
   */
  @Override
  public void merge(Object source, Object destination) {
    if (source == null || destination == null) {
      throw new IllegalArgumentException("Source and Destination objects cannot be null");
    }
    MAPPER.getConfiguration()
        .setPropertyCondition(context -> context.getSource() != null);
    MAPPER.map(source, destination);
  }
}
