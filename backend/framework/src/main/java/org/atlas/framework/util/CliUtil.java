package org.atlas.framework.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class CliUtil {

  public static void invoke(String cmd) {
    ProcessBuilder processBuilder = new ProcessBuilder();

    // Check the operating system and set the command accordingly
    boolean isWindows = System.getProperty("os.name")
        .toLowerCase().startsWith("windows");
    if (isWindows) {
      processBuilder.command("cmd.exe", "/c", cmd);
    } else {
      processBuilder.command("sh", "-c", cmd);
    }

    try {
      Process process = processBuilder.start();

      // Read the output of the command
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      List<String> outputLines = new ArrayList<>();
      String outputLine;
      while ((outputLine = reader.readLine()) != null) {
        outputLines.add(outputLine);
      }

      // Wait for the command to finish
      int exitCode = process.waitFor();

      // Read the error stream of command
      BufferedReader errorReader = new BufferedReader(
          new InputStreamReader(process.getErrorStream()));
      List<String> errorLines = new ArrayList<>();
      String errorLine;
      while ((errorLine = errorReader.readLine()) != null) {
        errorLines.add(errorLine);
      }

      String logStr = String.format("Command %s exited with code %d", cmd, exitCode);
      if (!outputLines.isEmpty()) {
        logStr += System.lineSeparator() + String.join(System.lineSeparator(), outputLines);
      }
      if (!errorLines.isEmpty()) {
        logStr += System.lineSeparator() + String.join(System.lineSeparator(), errorLines);
      }
      log.info(logStr);
    } catch (IOException e) {
      log.error("Failed to run command {}", cmd, e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
