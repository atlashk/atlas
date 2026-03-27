package org.atlas.services.catalog.infrastructure.persistence.jpa.adapter.chatbot;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.libs.persistence.jpa.paging.JpaPagingUtil;
import org.atlas.services.catalog.domain.entity.chatbot.ChatMessageEntity;
import org.atlas.services.catalog.infrastructure.persistence.jpa.entity.chatbot.JpaChatMessageEntity;
import org.atlas.services.catalog.infrastructure.persistence.jpa.mapper.chatbot.JpaChatMessageMapper;
import org.atlas.services.catalog.infrastructure.persistence.jpa.repository.chatbot.JpaChatMessageRepository;
import org.atlas.services.catalog.port.out.repository.chatbot.MessageRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaChatMessageRepositoryAdapter implements MessageRepository {

  private final JpaChatMessageRepository jpaMessageRepository;

  @Override
  public List<ChatMessageEntity> findByConversationId(String conversationId,
      PagingRequest pagingRequest) {
    Pageable pageable = JpaPagingUtil.convert(pagingRequest);
    List<JpaChatMessageEntity> jpaMessages =
        jpaMessageRepository.findByConversationId(conversationId, pageable);
    return MapperUtil.mapList(jpaMessages, JpaChatMessageMapper.INSTANCE::toMessage);
  }

  @Override
  public void insert(ChatMessageEntity chatMessage) {
    JpaChatMessageEntity jpaMessage = JpaChatMessageMapper.INSTANCE.toJpaMessage(chatMessage);
    jpaMessageRepository.insert(jpaMessage);
  }

  @Override
  public void deleteByConversationId(String conversationId) {
    jpaMessageRepository.deleteByConversationId(conversationId);
  }
}
