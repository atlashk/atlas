package org.atlas.product.persistence.jpa.repository;

import org.atlas.common.infrastructure.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.product.persistence.jpa.entity.JpaBrand;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaBrandRepository extends JpaBaseRepository<JpaBrand, Integer> {

}
