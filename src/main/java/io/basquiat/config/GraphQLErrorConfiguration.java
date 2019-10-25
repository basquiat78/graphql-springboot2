package io.basquiat.config;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import graphql.ExceptionWhileDataFetching;
import graphql.GraphQLError;
import graphql.servlet.GraphQLErrorHandler;
import io.basquiat.exception.adapter.GraphQLErrorAdapter;

/**
 * 
 * created by basquiat
 * 
 * GraphQLErrorHandler를 재정의 하는 설정 클래
 *
 */
@Configuration
public class GraphQLErrorConfiguration {

	@Bean
	public GraphQLErrorHandler errorHandler() {
		return new GraphQLErrorHandler() {
			@Override
			public List<GraphQLError> processErrors(List<GraphQLError> errors) {
				List<GraphQLError> clientErrors = errors.stream()
														.filter(this::isClientError)
														.collect(Collectors.toList());

				List<GraphQLError> serverErrors = errors.stream()
														.filter(e -> !isClientError(e))
														.map(GraphQLErrorAdapter::new)
														.collect(Collectors.toList());

				List<GraphQLError> e = new ArrayList<>();
				e.addAll(clientErrors);
				e.addAll(serverErrors);
				return e;
			}

			protected boolean isClientError(GraphQLError error) {
				return !(error instanceof ExceptionWhileDataFetching || error instanceof Throwable);
			}
		};
	}
	
}
