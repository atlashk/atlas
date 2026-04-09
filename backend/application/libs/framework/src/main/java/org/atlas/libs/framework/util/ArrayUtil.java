package org.atlas.libs.framework.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ArrayUtil {

  public static <T> boolean isEmpty(T[] arr) {
    return arr == null || arr.length == 0;
  }

  public static <T> boolean isNotEmpty(T[] arr) {
    return !isEmpty(arr);
  }

  public static boolean isEmpty(byte[] arr) {
    return arr == null || arr.length == 0;
  }

  public static boolean isNotEmpty(byte[] arr) {
    return !isEmpty(arr);
  }

  public static boolean isEmpty(int[] arr) {
    return arr == null || arr.length == 0;
  }

  public static boolean isNotEmpty(int[] arr) {
    return !isEmpty(arr);
  }

  public static boolean isEmpty(long[] arr) {
    return arr == null || arr.length == 0;
  }

  public static boolean isNotEmpty(long[] arr) {
    return !isEmpty(arr);
  }

  public static boolean isEmpty(double[] arr) {
    return arr == null || arr.length == 0;
  }

  public static boolean isNotEmpty(double[] arr) {
    return !isEmpty(arr);
  }

  public static boolean isEmpty(float[] arr) {
    return arr == null || arr.length == 0;
  }

  public static boolean isNotEmpty(float[] arr) {
    return !isEmpty(arr);
  }

  public static boolean isEmpty(char[] arr) {
    return arr == null || arr.length == 0;
  }

  public static boolean isNotEmpty(char[] arr) {
    return !isEmpty(arr);
  }

  public static boolean isEmpty(short[] arr) {
    return arr == null || arr.length == 0;
  }

  public static boolean isNotEmpty(short[] arr) {
    return !isEmpty(arr);
  }

  public static boolean isEmpty(boolean[] arr) {
    return arr == null || arr.length == 0;
  }

  public static boolean isNotEmpty(boolean[] arr) {
    return !isEmpty(arr);
  }

  public static int size(Object[] arr) {
    return arr == null ? 0 : arr.length;
  }

  public static int size(int[] arr) {
    return arr == null ? 0 : arr.length;
  }
}
