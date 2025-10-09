package org.atlas.infrastructure.auth.server.security;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.entity.UserEntity;
import org.atlas.domain.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Optional<UserEntity> userOpt = userRepository.findByUsername(username);
    if (userOpt.isEmpty()) {
      userOpt = userRepository.findByEmail(username);
      if (userOpt.isEmpty()) {
        userOpt = userRepository.findByPhoneNumber(username);
        if (userOpt.isEmpty()) {
          throw new UsernameNotFoundException(username);
        }
      }
    }
    return userOpt.map(UserDetailsImpl::new).get();
  }
}
