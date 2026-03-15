package org.atlas.platform.authorization.spring.application.core;

import lombok.RequiredArgsConstructor;
import org.atlas.platform.authorization.port.out.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    return userRepository.findByEmail(email)
        .map(UserDetailsImpl::new)
        .orElseThrow(() -> new UsernameNotFoundException(String.format("User %s not found", email)));
  }
}
