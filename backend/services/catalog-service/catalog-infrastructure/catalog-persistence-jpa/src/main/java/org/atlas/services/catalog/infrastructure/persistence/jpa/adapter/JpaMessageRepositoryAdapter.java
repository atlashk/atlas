package org.atlas.services.catalog.infrastructure.persistence.jpa.adapter;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.services.catalog.domain.entity.chatbot.MessageEntity;
import org.atlas.services.catalog.infrastructure.persistence.jpa.entity.JpaMessageEntity;
import org.atlas.services.catalog.infrastructure.persistence.jpa.mapper.JpaMessageMapper;
import org.atlas.services.catalog.infrastructure.persistence.jpa.repository.JpaMessageRepository;
import org.atlas.services.catalog.port.out.repository.chatbot.MessageRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaMessageRepositoryAdapter implements MessageRepository {

  private final JpaMessageRepository jpaMessageRepository;

  @Override
  public List<MessageEntity> findByConversationId(String conversationId, PagingRequest pagingRequest) {
    Sort sort = buildSort(pagingRequest);
    List<JpaMessageEntity> jpaMessages;
    if (pagingRequest.hasPaging()) {
      PageRequest pageable = PageRequest.of(pagingRequest.getPage(), pagingRequest.getSize(), sort);
      jpaMessages = jpaMessageRepository.findByConversationId(conversationId, pageable);
    } else if (pagingRequest.hasSort()) {
      jpaMessages = jpaMessageRepository.findByConversationId(conversationId, sort);
    } else {
      jpaMessages = jpaMessageRepository.findByConversationId(conversationId);
    }
    return MapperUtil.mapList(jpaMessages, JpaMessageMapper.INSTANCE::toMessage);
  }

  @Override
  public void insert(MessageEntity messageEntity) {
    JpaMessageEntity jpaMessage = JpaMessageMapper.INSTANCE.toJpaMessage(messageEntity);
    jpaMessageRepository.insert(jpaMessage);
  }

  @Override
  public void deleteByConversationId(String conversationId) {
    jpaMessageRepository.deleteByConversationId(conversationId);
  }

  private Sort buildSort(PagingRequest pagingRequest) {
    if (pagingRequest.hasSort()) {
      Sort.Direction direction =
          pagingRequest.isSortAscending() ? Sort.Direction.ASC : Sort.Direction.DESC;
      return Sort.by(direction, pagingRequest.getSortBy());
    }
    return Sort.by(Sort.Direction.ASC, "createdAt");
  }
}
