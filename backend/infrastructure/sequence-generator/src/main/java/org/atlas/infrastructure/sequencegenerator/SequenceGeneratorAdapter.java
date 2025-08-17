package org.atlas.infrastructure.sequencegenerator;

import lombok.RequiredArgsConstructor;
import org.atlas.framework.sequencegenerator.SequenceGenerator;
import org.atlas.framework.sequencegenerator.enums.SequenceType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SequenceGeneratorAdapter implements SequenceGenerator {

  private final JdbcTemplate jdbcTemplate;

  @Override
  @Transactional
  public String generate(SequenceType sequenceType) {
    // Step 1: Check if the sequence exists; if not, insert a new one with initial value = 1
    int affectedRows = jdbcTemplate.update(
        """
            INSERT INTO sequence_generator (seq_name, seq_value)
            SELECT ?, 1
            WHERE NOT EXISTS (
              SELECT 1 FROM sequence_generator WHERE seq_name = ?
            )
            """,
        sequenceType.getName(), sequenceType.getName()
    );

    Long sequence;

    // Step 2: Update the sequence if it already exists or was just created, then fetch its new value
    if (affectedRows == 0) {
      // Sequence already exists, increment the sequence and fetch the updated value
      jdbcTemplate.update(
          "UPDATE sequence_generator SET seq_value = seq_value + 1 WHERE seq_name = ?",
          sequenceType.getName()
      );
    }

    // Step 3: Fetch the current sequence value
    sequence = jdbcTemplate.queryForObject(
        "SELECT seq_value FROM sequence_generator WHERE seq_name = ?",
        Long.class,
        sequenceType.getName()
    );

    // Step 4: Format and return the generated code
    return generateFormattedString(sequenceType.getPrefix(), sequence, sequenceType.getPadding());
  }

  private String generateFormattedString(String prefix, Long sequence, int padding) {
    // Create a StringBuilder for efficient string concatenation
    StringBuilder builder = new StringBuilder(prefix);

    // Calculate the number of leading zeros needed
    int leadingZeros = padding - String.valueOf(sequence).length();

    // Append leading zeros if needed
    builder.append("0".repeat(Math.max(0, leadingZeros)));

    // Append the sequence number
    builder.append(sequence);

    return builder.toString();
  }
}
