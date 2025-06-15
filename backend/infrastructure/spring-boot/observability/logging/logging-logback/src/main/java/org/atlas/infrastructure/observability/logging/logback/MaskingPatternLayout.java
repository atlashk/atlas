package org.atlas.infrastructure.observability.logging.logback;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import java.util.regex.Pattern;

public class MaskingPatternLayout extends PatternLayout {

    private static final Pattern SENSITIVE_PATTERN = Pattern.compile(
        "(\"?(password|ssn|cardNumber)\"?\\s*[:=]\\s*\")([^\"\\s]+)(\")", Pattern.CASE_INSENSITIVE);

    @Override
    public String doLayout(ILoggingEvent event) {
        String message = super.doLayout(event);
        return SENSITIVE_PATTERN.matcher(message).replaceAll("$1******$4");
    }
}
