package org.atlas.framework.util;

import java.math.BigDecimal;
import java.util.Currency;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CurrencyUtil {

  // Method to adjust the amount for a given currency
  public static long getAmountInSmallestUnit(BigDecimal amount, String currencyCode) {
    Currency currency = Currency.getInstance(currencyCode);
    int decimalPlaces = currency.getDefaultFractionDigits();

    // If the currency uses 2 decimal places, multiply by 100
    if (decimalPlaces == 2) {
      return amount.multiply(BigDecimal.valueOf(100)).longValue();
    }
    // If the currency uses 0 decimal places, no multiplication needed
    else if (decimalPlaces == 0) {
      return amount.longValue();
    }

    throw new IllegalArgumentException("Unsupported currency decimal format: " + currencyCode);
  }

  // Method to convert amount back to the currency's decimal representation
  public static BigDecimal getAmountFromSmallestUnit(long smallestUnitAmount, String currencyCode) {
    Currency currency = Currency.getInstance(currencyCode);
    int decimalPlaces = currency.getDefaultFractionDigits();

    // If the currency uses 2 decimal places, divide by 100
    if (decimalPlaces == 2) {
      return BigDecimal.valueOf(smallestUnitAmount).divide(BigDecimal.valueOf(100));
    }
    // If the currency uses 0 decimal places, return as is
    else if (decimalPlaces == 0) {
      return BigDecimal.valueOf(smallestUnitAmount);
    }

    throw new IllegalArgumentException("Unsupported currency decimal format: " + currencyCode);
  }
}
