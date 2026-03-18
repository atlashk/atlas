# Use multi-stage build for optimization
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /extracted

COPY ./build/libs/*.jar app.jar

RUN java -Djarmode=layertools -jar app.jar extract

# Final runtime image
FROM eclipse-temurin:17-jre

# Create non-root user for security
RUN groupadd -g 1001 spring && \
    useradd -u 1001 -g spring -s /bin/bash -m spring

# Install dumb-init for proper signal handling
RUN apt-get update && apt-get install -y --no-install-recommends dumb-init && \
    rm -rf /var/lib/apt/lists/*

# Create log directory with proper permissions
RUN mkdir -p /var/log/authorization-server && \
    chown -R spring:spring /var/log/authorization-server

# Set working directory
WORKDIR /app

# Copy layers in order of least to most frequently changing
# This optimizes Docker layer caching
COPY --from=builder /extracted/dependencies/ ./
COPY --from=builder /extracted/spring-boot-loader/ ./
COPY --from=builder /extracted/snapshot-dependencies/ ./
COPY --from=builder /extracted/application/ ./

# Switch to non-root user
USER spring:spring

# Expose application port
EXPOSE 8901

# Use dumb-init for proper signal handling and optimized JVM settings
ENTRYPOINT ["dumb-init", "--", "java", \
  # Memory settings
  "-Xms512m", \
  "-Xmx1g", \
  # GC settings optimized for containers
  "-XX:+UseG1GC", \
  "-XX:MaxGCPauseMillis=200", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  # JIT compiler optimizations
  "-XX:+AlwaysPreTouch", \
  "-XX:+UseStringDeduplication", \
  # Debugging and monitoring
  "-XX:+HeapDumpOnOutOfMemoryError", \
  "-XX:HeapDumpPath=/tmp/heapdump.hprof", \
  "-XX:+ExitOnOutOfMemoryError", \
  # Security and entropy
  "-Djava.security.egd=file:/dev/urandom", \
  # Spring Boot 3.x optimizations
  "-Dspring.jmx.enabled=false", \
  # Since Spring Boot 3.2.0, a new org.springframework.boot.loader.launch.JarLauncher has been introduced
  # instead of org.springframework.boot.loader.JarLauncher
  "org.springframework.boot.loader.launch.JarLauncher"]
