package org.atlas.services.catalog.infrastructure.persistence.jpa.entity.chatbot;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.persistence.jpa.entity.JpaBaseEntity;

@Entity
@Table(name = "chat_conversation")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class JpaChatConversationEntity extends JpaBaseEntity {

  @Id
  @Column(name = "id")
  @EqualsAndHashCode.Include
  private String id;

  @Column(name = "user_id")
  private String userId;

  @Column(name = "title")
  private String title;
}
