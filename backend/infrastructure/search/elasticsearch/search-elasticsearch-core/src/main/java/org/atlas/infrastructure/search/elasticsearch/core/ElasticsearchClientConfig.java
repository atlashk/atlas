package org.atlas.infrastructure.search.elasticsearch.core;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;

@Configuration
@RequiredArgsConstructor
public class ElasticsearchClientConfig extends ElasticsearchConfiguration {

  private final ElasticsearchProps props;

	@Override
	public ClientConfiguration clientConfiguration() {
		return ClientConfiguration.builder()
			.connectedTo(String.format("%s:%s", props.getHost(), props.getPort()))
			.build();
	}
}
