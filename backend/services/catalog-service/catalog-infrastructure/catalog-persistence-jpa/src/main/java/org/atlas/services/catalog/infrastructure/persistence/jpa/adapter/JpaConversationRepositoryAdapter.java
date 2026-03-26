package org.atlas.services.catalog.infrastructure.persistence.jpa.adapter;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.services.catalog.domain.entity.chatbot.ConversationEntity;
import org.atlas.services.catalog.infrastructure.persistence.jpa.entity.JpaConversationEntity;
import org.atlas.services.catalog.infrastructure.persistence.jpa.mapper.JpaConversationMapper;
import org.atlas.services.catalog.infrastructure.persistence.jpa.repository.JpaConversationRepository;
import org.atlas.services.catalog.port.out.repository.chatbot.ConversationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaConversationRepositoryAdapter implements ConversationRepository {

  private final JpaConversationRepository jpaConversationRepository;

  @Override
  public List<ConversationEntity> findByUserId(String userId, PagingRequest pagingRequest) {
    Sort sort = buildSort(pagingRequest);
    List<JpaConversationEntity> jpaConversations;
    if (pagingRequest.hasPaging()) {
      PageRequest pageable = PageRequest.of(pagingRequest.getPage(), pagingRequest.getSize(), sort);
      jpaConversations = jpaConversationRepository.findByUserId(userId, pageable);
    } else if (pagingRequest.hasSort()) {
      jpaConversations = jpaConversationRepository.findByUserId(userId, sort);
    } else {
      jpaConversations = jpaConversationRepository.findByUserId(userId);
    }
    return MapperUtil.mapList(jpaConversations, JpaConversationMapper.INSTANCE::toConversation);
  }

  @Override
  public void insert(ConversationEntity conversation) {
    JpaConversationEntity jpaConversation = JpaConversationMapper.INSTANCE.toJpaConversation(conversation);
    jpaConversationRepository.insert(jpaConversation);
  }

  @Override
  public void delete(String conversationId) {
    jpaConversationRepository.deleteById(conversationId);
  }

  private Sort buildSort(PagingRequest pagingRequest) {
    if (pagingRequest.hasSort()) {
      Sort.Direction direction =
          pagingRequest.isSortAscending() ? Sort.Direction.ASC : Sort.Direction.DESC;
      return Sort.by(direction, pagingRequest.getSortBy());
    }
    return Sort.by(Sort.Direction.DESC, "createdAt");
  }
}
