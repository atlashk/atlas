package org.atlas.edge.auth.springsecurityjwt.core;

import java.util.Collection;
import java.util.Collections;
import lombok.Getter;
import lombok.Setter;
import org.atlas.domain.auth.entity.User;
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

  public UserDetailsImpl(User user) {
    this.userId = user.getUserId();
    this.username = user.getUsername();
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
