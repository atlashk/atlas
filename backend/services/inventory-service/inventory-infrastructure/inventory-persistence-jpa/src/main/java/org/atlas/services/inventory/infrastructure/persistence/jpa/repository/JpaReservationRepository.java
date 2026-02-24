package org.atlas.services.inventory.infrastructure.persistence.jpa.repository;

import java.util.Optional;
import org.atlas.libs.persistence.jpa.repository.JpaBaseRepository;
import org.atlas.services.inventory.domain.entity.ReservationStatus;
import org.atlas.services.inventory.infrastructure.persistence.jpa.entity.JpaReservationEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaReservationRepository extends JpaBaseRepository<JpaReservationEntity, Integer> {

  Optional<JpaReservationEntity> findByOrderIdAndProductId(String orderId, String productId);
  
  @Modifying
  @Query("""
        UPDATE JpaReservationEntity r
           SET r.status = :status
         WHERE r.orderId = :orderId
      """)
  int updateStatusByOrderId(@Param("orderId") String orderId, 
                            @Param("status") ReservationStatus status);
}
