package org.atlas.services.iam.application.jwt.core;

import lombok.RequiredArgsConstructor;
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
    return userRepository.findByUsername(username)
        .map(UserDetailsImpl::new)
        .orElseThrow(() -> new UsernameNotFoundException(String.format("User %s not found", username)));
  }
}
