package org.atlas.domain.payment.repository;

import org.atlas.domain.payment.entity.WebhookEventEntity;

public interface WebhookEventRepository {

  void save(WebhookEventEntity webhookEventEntity);
}
