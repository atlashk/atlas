package org.atlas.services.catalog.infrastructure.persistence.jpa.adapter.chatbot;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.libs.persistence.jpa.paging.JpaPagingUtil;
import org.atlas.services.catalog.domain.entity.chatbot.ChatConversation;
import org.atlas.services.catalog.infrastructure.persistence.jpa.entity.chatbot.JpaChatConversationEntity;
import org.atlas.services.catalog.infrastructure.persistence.jpa.mapper.chatbot.JpaChatConversationMapper;
import org.atlas.services.catalog.infrastructure.persistence.jpa.repository.chatbot.JpaChatConversationRepository;
import org.atlas.services.catalog.port.out.repository.chatbot.ConversationRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaChatConversationRepositoryAdapter implements ConversationRepository {

  private final JpaChatConversationRepository jpaChatConversationRepository;

  @Override
  public List<ChatConversation> findByUserId(String userId, PagingRequest pagingRequest) {
    Pageable pageable = JpaPagingUtil.convert(pagingRequest);
    List<JpaChatConversationEntity> jpaConversations =
        jpaChatConversationRepository.findByUserId(userId, pageable);
    return MapperUtil.mapList(jpaConversations,
        JpaChatConversationMapper.INSTANCE::toConversation);
  }

  @Override
  public void insert(ChatConversation conversation) {
    JpaChatConversationEntity jpaConversation =
        JpaChatConversationMapper.INSTANCE.toJpaConversation(conversation);
    jpaChatConversationRepository.insert(jpaConversation);
  }

  @Override
  public void delete(String conversationId) {
    jpaChatConversationRepository.deleteById(conversationId);
  }
}
