package org.atlas.services.catalog.port.in.product.service;

public interface ProductInitializerService {

  void initializeImageBucket() throws Exception;

  void initializeSearchData() throws Exception;

  void initializeVectorStore() throws Exception;
}
