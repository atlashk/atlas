package org.atlas.framework.objectmapper;

import org.atlas.framework.objectmapper.modelmapper.ModelMapperService;

/**
 * Implement Singleton pattern with Bill Pugh solution
 */
public class ObjectMapperUtil {

  private static class ServiceHolder {

    private static final ObjectMapperService INSTANCE = new ModelMapperService();
  }

  public static ObjectMapperService getInstance() {
    return ServiceHolder.INSTANCE;
  }
}
