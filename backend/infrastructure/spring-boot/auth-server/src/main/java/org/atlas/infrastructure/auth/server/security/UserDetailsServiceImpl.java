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
  public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
    Optional<UserEntity> userEntityOpt = userRepository.findByUsername(identifier);
    if (userEntityOpt.isEmpty()) {
      userEntityOpt = userRepository.findByEmail(identifier);
      if (userEntityOpt.isEmpty()) {
        userEntityOpt = userRepository.findByPhoneNumber(identifier);
        if (userEntityOpt.isEmpty()) {
          throw new UsernameNotFoundException(identifier);
        }
      }
    }
    return userEntityOpt.map(UserDetailsImpl::new).get();
  }
}
