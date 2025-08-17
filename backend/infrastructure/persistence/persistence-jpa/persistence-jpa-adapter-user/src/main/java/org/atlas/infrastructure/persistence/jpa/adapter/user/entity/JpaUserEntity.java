package org.atlas.infrastructure.persistence.jpa.adapter.user.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.atlas.domain.user.shared.enums.Role;
import org.atlas.infrastructure.persistence.jpa.core.converter.StringCryptoConverter;
import org.atlas.infrastructure.persistence.jpa.core.entity.JpaBaseEntity;

@Entity
@Table(name = "users")
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class JpaUserEntity extends JpaBaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private Integer id;

  private String username;

  private String password;

  @Column(name = "first_name")
  private String firstName;

  @Column(name = "last_name")
  private String lastName;

  @Convert(converter = StringCryptoConverter.class)
  private String email;

  @Column(name = "phone_number")
  @Convert(converter = StringCryptoConverter.class)
  private String phoneNumber;

  @Enumerated(EnumType.STRING)
  private Role role;
}
