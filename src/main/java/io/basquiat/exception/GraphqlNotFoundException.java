package io.basquiat.exception;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.language.SourceLocation;

/**
 * 
 * created by basquiat
 * 
 * album id가 없을 때 메세지를 명확하게 정의해서 던진다.
 * 
 */
public class GraphqlNotFoundException extends RuntimeException implements GraphQLError {

	private static final long serialVersionUID = -6856095627314499827L;

	private Map<String, Object> extensions = new HashMap<>();

    public GraphqlNotFoundException(String message, long id) {
        super(message);
        extensions.put("not found Anything by id, maybe id doesn't exit", id);
    }

    @Override
    public List<SourceLocation> getLocations() {
        return null;
    }

    @Override
    public Map<String, Object> getExtensions() {
    	return extensions;
    }

    @Override
    public ErrorType getErrorType() {
        return ErrorType.DataFetchingException;
    }

}
