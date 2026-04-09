package org.atlas.services.catalog.infrastructure.persistence.jpa.entity.chatbot;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.persistence.jpa.entity.JpaBaseEntity;
import org.atlas.services.catalog.domain.entity.chatbot.MessageType;
import org.atlas.services.catalog.domain.entity.chatbot.SenderType;

@Entity
@Table(name = "chat_message")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class JpaChatMessageEntity extends JpaBaseEntity {

  @Id
  @Column(name = "id")
  @EqualsAndHashCode.Include
  private String id;

  @Column(name = "conversation_id")
  private String conversationId;

  @Column(name = "message_type")
  @Enumerated(EnumType.STRING)
  private MessageType messageType;

  @Column(name = "sender_type")
  @Enumerated(EnumType.STRING)
  private SenderType senderType;

  @Column(name = "user_id")
  private String userId;

  @Column(name = "text")
  private String text;
}
