package org.atlas.infrastructure.notification.websocket.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.config.ApplicationConfigPort;
import org.atlas.framework.constant.Application;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for real-time notifications. Enables STOMP messaging over WebSocket with
 * SockJS fallback support.
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Slf4j
public class WebSocketServerConfig implements WebSocketMessageBrokerConfigurer {

  public static final String DESTINATION_PREFIX = "/topic";
  private static final String STOMP_ENDPOINT = "/notification/ws";

  private final ApplicationConfigPort applicationConfigPort;

  /**
   * Configure the message broker for handling STOMP messages. Sets up heartbeat mechanism with a
   * task scheduler.
   */
  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    config.enableSimpleBroker(DESTINATION_PREFIX)
        .setHeartbeatValue(new long[]{10000, 10000}) // 10 second heartbeat interval
        .setTaskScheduler(heartBeatTaskScheduler()); // Provide task scheduler for heartbeat
  }

  /**
   * Register STOMP endpoints for WebSocket connections. Configures CORS and enables SockJS
   * fallback.
   */
  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    // Get allowed origins from configuration
    String[] allowedOrigins = applicationConfigPort.getConfigAsList(Application.SYSTEM,
            "cors.allowed-origins")
        .toArray(new String[0]);

    registry.addEndpoint(STOMP_ENDPOINT)
        .setAllowedOrigins(allowedOrigins)
        .withSockJS();

    log.info("WebSocket server configuration completed successfully with allowed origins: {}",
        String.join(", ", allowedOrigins));
  }

  /**
   * Configure client inbound channel to add server header to CONNECT frames.
   */
  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(new ChannelInterceptor() {
      @Override
      public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
          // Add server identifier to CONNECT response
          accessor.addNativeHeader("server", applicationConfigPort.getApplicationName());
        }
        return message;
      }
    });
  }

  /**
   * Task scheduler bean for handling WebSocket heartbeat mechanism. Required when heartbeat values
   * are configured.
   */
  @Bean
  public TaskScheduler heartBeatTaskScheduler() {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setPoolSize(10); // Number of threads for scheduling tasks
    scheduler.setThreadNamePrefix("websocket-heartbeat-");
    scheduler.setWaitForTasksToCompleteOnShutdown(true);
    scheduler.setAwaitTerminationSeconds(60);
    return scheduler;
  }
}
