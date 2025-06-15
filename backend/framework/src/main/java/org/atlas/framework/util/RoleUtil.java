package org.atlas.framework.util;

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.atlas.domain.user.shared.enums.Role;

@UtilityClass
public class RoleUtil {

  public static String toRolesString(Collection<Role> roles) {
    return roles.stream()
        .map(Enum::name)
        .collect(Collectors.joining(","));
  }

  public static Set<Role> toRolesSet(String rolesString) {
    if (StringUtils.isBlank(rolesString)) {
      return Sets.newHashSet();
    }
    return Arrays.stream(rolesString.split(","))
        .map(String::trim)
        .map(Role::valueOf)
        .collect(Collectors.toSet());
  }
}
