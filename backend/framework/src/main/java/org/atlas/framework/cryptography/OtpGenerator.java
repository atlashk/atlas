package org.atlas.framework.cryptography;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.experimental.UtilityClass;
import org.apache.commons.codec.binary.Hex;

@UtilityClass
public class OtpGenerator {

  /**
   * Generates an HMAC-based One-Time Password (HOTP) as per RFC 4226.
   *
   * @param secret   The shared secret key in hexadecimal format.
   *                 Example: "12345678901234567890" (hex) or decode from base32 "GEZDGNBVGY3TQOI=".
   * @param counter  The counter value used to generate the OTP.
   * @param digits   The number of digits in the generated OTP (typically 6 or 8).
   * @return         A string representation of the HOTP with the specified number of digits.
   * @throws Exception If the HMAC-SHA1 algorithm is not supported or the secret is invalid.
   */
  public static String generateHOTP(String secret, long counter, int digits) throws Exception {
    // Decode the hexadecimal secret into a byte array for HMAC computation
    byte[] key = Hex.decodeHex(secret.toCharArray());

    // Convert the counter to an 8-byte array in big-endian format
    byte[] counterBytes = new byte[8];
    for (int i = 7; i >= 0; i--) {
      counterBytes[i] = (byte) (counter & 0xff);
      counter >>= 8;
    }

    // Initialize the HMAC-SHA1 algorithm with the secret key
    Mac mac = Mac.getInstance("HmacSHA1");
    mac.init(new SecretKeySpec(key, "HmacSHA1"));

    // Compute the HMAC-SHA1 hash of the counter bytes
    byte[] hash = mac.doFinal(counterBytes);

    // Perform dynamic truncation as per RFC 4226 to extract a 4-byte value
    int offset = hash[hash.length - 1] & 0xf;
    int binary = ((hash[offset] & 0x7f) << 24) |
        ((hash[offset + 1] & 0xff) << 16) |
        ((hash[offset + 2] & 0xff) << 8) |
        (hash[offset + 3] & 0xff);

    // Calculate the OTP by taking the modulo with 10^digits to ensure the desired length
    int otp = binary % (int) Math.pow(10, digits);

    // Format the OTP as a zero-padded string with the specified number of digits
    return String.format("%0" + digits + "d", otp);
  }

  /**
   * Generates a Time-based One-Time Password (TOTP) as per RFC 6238.
   *
   * @param secret          The shared secret key in hexadecimal format.
   *                        Example: "12345678901234567890" (hex) or decode from base32 "GEZDGNBVGY3TQOI=".
   * @param timeStepSeconds The time step interval in seconds (typically 30).
   * @param digits          The number of digits in the generated OTP (typically 6 or 8).
   * @return                A string representation of the TOTP with the specified number of digits.
   * @throws Exception      If the HMAC-SHA1 algorithm is not supported or the secret is invalid.
   */
  public static String generateTOTP(String secret, long timeStepSeconds, int digits)
      throws Exception {
    // Get the current time in seconds since the Unix epoch
    long currentTime = System.currentTimeMillis() / 1000;

    // Calculate the counter by dividing the current time by the time step
    long counter = currentTime / timeStepSeconds;

    // Generate the TOTP by reusing the HOTP algorithm with the time-based counter
    return generateHOTP(secret, counter, digits);
  }
}
