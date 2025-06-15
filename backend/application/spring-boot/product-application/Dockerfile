# Use multi-stage build for optimization
FROM eclipse-temurin:17-jre-alpine AS builder
WORKDIR /workspace
COPY ./build/libs/*.jar app.jar
# Use the new Spring Boot 3.2+ layertools syntax
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher

# Final runtime image
FROM eclipse-temurin:17-jre-alpine

# Create non-root user for security
RUN addgroup -g 1001 -S spring && \
    adduser -u 1001 -S spring -G spring

# Install dumb-init for proper signal handling
RUN apk add --no-cache dumb-init

# Create log directory with proper permissions
RUN mkdir -p /var/log/product-service && \
    chown -R spring:spring /var/log/product-service

# Set working directory
WORKDIR /app

# Copy layers in order of least to most frequently changing
# This optimizes Docker layer caching
COPY --from=builder --chown=spring:spring /workspace/*/dependencies/ ./
COPY --from=builder --chown=spring:spring /workspace/*/spring-boot-loader/ ./
COPY --from=builder --chown=spring:spring /workspace/*/snapshot-dependencies/ ./
COPY --from=builder --chown=spring:spring /workspace/*/application/ ./

# Switch to non-root user
USER spring:spring

# Expose application port
EXPOSE 8082

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
  # Use the Spring Boot launcher for layered structure
  "org.springframework.boot.loader.launch.JarLauncher"]
