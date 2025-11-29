package org.atlas.edge.auth.springsecurityjwt.core;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.auth.entity.User;
import org.atlas.domain.auth.repository.UserRepository;
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
    Optional<User> userOpt = userRepository.findByUsername(username);
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
