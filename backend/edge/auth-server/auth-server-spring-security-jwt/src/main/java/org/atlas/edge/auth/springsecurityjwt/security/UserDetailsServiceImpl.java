package org.atlas.edge.auth.springsecurityjwt.security;

import lombok.RequiredArgsConstructor;
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
  public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
    return userRepository.findByIdentifier(identifier)
        .map(UserDetailsImpl::new)
        .orElseThrow(() -> new UsernameNotFoundException(
            String.format("User with identifier %s not found", identifier)));
  }
}
