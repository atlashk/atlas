package org.atlas.services.identity.application.jwt.core;

import java.util.Collection;
import java.util.Collections;
import lombok.Getter;
import lombok.Setter;
import org.atlas.libs.framework.domain.shared.identity.UserRole;
import org.atlas.services.identity.domain.entity.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Setter
public class UserDetailsImpl implements UserDetails {

  private String id;
  private String email;
  private String password;
  private Collection<? extends GrantedAuthority> authorities;

  public UserDetailsImpl(UserEntity user) {
    this.id = user.getId();
    this.email = user.getEmail();
    this.password = user.getPassword();
    this.authorities = Collections.singletonList(
        new SimpleGrantedAuthority(user.getRole().name()));
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  public UserRole getRole() {
    return authorities.stream()
        .map(authority -> UserRole.valueOf(authority.getAuthority()))
        .findFirst()
        .orElse(UserRole.USER);
  }
}
