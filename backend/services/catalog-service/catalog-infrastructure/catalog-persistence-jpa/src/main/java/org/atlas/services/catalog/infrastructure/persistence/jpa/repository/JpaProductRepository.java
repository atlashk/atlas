package org.atlas.services.catalog.infrastructure.persistence.jpa.repository;

import java.util.List;
import java.util.Optional;
import org.atlas.libs.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.services.catalog.infrastructure.persistence.jpa.entity.JpaProductEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaProductRepository extends JpaBaseRepository<JpaProductEntity, String> {

  @Query("""
        select p
        from JpaProductEntity p
        left join fetch p.details
        left join fetch p.attributes
        left join fetch p.brand
        left join fetch p.categories
        where p.id in (:ids)
      """)
  List<JpaProductEntity> findAllByIdInWithAssociations(@Param("ids") List<String> ids);

  @Query("""
        select p
        from JpaProductEntity p
        left join fetch p.details
        left join fetch p.attributes
        left join fetch p.brand
        left join fetch p.categories
        where p.id = :id
      """)
  Optional<JpaProductEntity> findByIdWithAssociations(@Param("id") String id);
}
