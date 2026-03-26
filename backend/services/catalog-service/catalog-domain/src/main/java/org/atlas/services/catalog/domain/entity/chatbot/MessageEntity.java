package org.atlas.services.catalog.domain.entity.chatbot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.entity.DomainEntity;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class MessageEntity extends DomainEntity {

  @EqualsAndHashCode.Include
  private String id;

  private String conversationId;

  private MessageType messageType;

  private SenderType senderType;

  private String userId;

  private String text;
}
