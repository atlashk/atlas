package org.atlas.services.iam.application.jwt.core;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.services.iam.domain.entity.UserEntity;
import org.atlas.services.iam.port.out.repository.UserRepository;
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
