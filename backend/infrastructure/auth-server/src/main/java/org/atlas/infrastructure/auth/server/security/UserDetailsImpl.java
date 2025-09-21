package org.atlas.infrastructure.auth.server.security;

import java.util.Collection;
import java.util.Collections;
import lombok.Getter;
import lombok.Setter;
import org.atlas.domain.user.entity.UserEntity;
import org.atlas.domain.user.shared.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Setter
public class UserDetailsImpl implements UserDetails {

  private Integer userId;
  private String username;
  private String password;
  private Collection<? extends GrantedAuthority> authorities;

  public UserDetailsImpl(UserEntity userEntity) {
    this.userId = userEntity.getId();
    this.username = userEntity.getUsername();
    this.password = userEntity.getPassword();
    this.authorities = Collections.singletonList(
        new SimpleGrantedAuthority(userEntity.getRole().name()));
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
    return username;
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

  public Role getRole() {
    return authorities.stream()
        .map(authority -> Role.valueOf(authority.getAuthority()))
        .findFirst()
        .orElse(Role.USER);
  }
}
