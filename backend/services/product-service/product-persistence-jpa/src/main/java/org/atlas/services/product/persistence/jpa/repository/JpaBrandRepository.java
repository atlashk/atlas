package org.atlas.services.product.persistence.jpa.repository;

import org.atlas.libs.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.services.product.persistence.jpa.entity.JpaBrand;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaBrandRepository extends JpaBaseRepository<JpaBrand, Integer> {

}
