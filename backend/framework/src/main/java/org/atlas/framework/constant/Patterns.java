package org.atlas.framework.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Patterns {

  public static final String EMAIL = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
  public static final String PASSWORD = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{8,}$";
}
