package org.atlas.infrastructure.persistence.jpa.impl.product.repository;

import org.atlas.infrastructure.persistence.jpa.core.repository.JpaBaseRepository;
import org.atlas.infrastructure.persistence.jpa.impl.product.entity.JpaBrand;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaBrandRepository extends JpaBaseRepository<JpaBrand, Integer> {

}
