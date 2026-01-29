package org.atlas.platform.auth.common.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.user.Role;
import org.atlas.libs.persistence.jpa.converter.StringCryptoConverter;
import org.atlas.libs.persistence.jpa.entity.JpaBaseEntity;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class JpaUser extends JpaBaseEntity {

  @Id
  @Column(name = "user_id")
  private Integer userId;

  @Column(name = "username")
  private String username;

  @Column(name = "password")
  private String password;

  @Column(name = "email")
  @Convert(converter = StringCryptoConverter.class)
  private String email;

  @Column(name = "phone_number")
  @Convert(converter = StringCryptoConverter.class)
  private String phoneNumber;

  @Column(name = "role")
  @Enumerated(EnumType.STRING)
  private Role role;
}
