package io.basquiat.music.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import graphql.ExecutionResult;
import graphql.GraphQL;

/**
 * 
 * created by basquiat
 *
 * graphql은 하나의 엔드포인트만 존재하기 때문에 들어온 리퀘스트 스키마에 의해 액션이 결정된다.
 * 따라서 다음과 같이 스키마를 처리하는 execute 하나의 커맨드만 존재한다.  
 *
 */
@Service("musicianService")
public class MusicianService {

	@Autowired
	private GraphQL graphQL;
	
	public ExecutionResult execute(String query){
        return graphQL.execute(query);
    }
	
}
