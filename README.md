## Use Resolver

이번 브랜치는 컨트롤러가 아닌 리졸버를 활용한 방법이다.

컨트롤러가 없고 Query, Mutation에 맞는 리졸버를 구현함으로써 그것이 대신 컨트롤러 역할을 하게 된다.

## 구조
io.basquiat    
 	ㄴ music    
 		ㄴ models    
 		ㄴ repo    
 		ㄴ resolver
 		    ㄴalbum
 		    ㄴmusician
resources    
	ㄴ graphql    
		- album.graphqls    
		- musician.graphqls    

## application.yml Configuration

컨트롤러가 없기 때문에 엔드포인트가 무엇이냐는 의문이 생길텐데 기본적으로 다음과 같다.

<app url>/graphql

하지만 application.yml에 다음과 같이 엔드포인트를 지정할 수 있다.

```
#spring  setup and common configuration
spring:
  profiles:
    active: local

---
spring:
  profiles: local
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate.ddl-auto: update
    show-sql: true
  h2:
    console:
      path: /h2
      enabled: true
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_ON_EXIT=FALSE
    username: sa
    password:
    driver-class-name: org.h2.Driver

# graphql config
# default endpoint 'graphql'
# if you change endpoint, graphql.servlet.mapping -> definition your endpoint
graphql:
  servlet:
       mapping: /music
       enabled: true
       corsEnabled: true

```

raphql.servlet.mapping에 원하는 엔드포인트를 지정하면 된다. 

설정을 하지 않으면 기본값은 graphql
 		
## implement Resolver


QueryResolver.java

```
package io.basquiat.music.resolver;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;

import io.basquiat.music.models.Album;
import io.basquiat.music.models.Musician;
import io.basquiat.music.repo.AlbumRepository;
import io.basquiat.music.repo.MusicianRepository;

/**
 * 
 * created by basquiat
 * 
 * Root Query Resolver
 * 
 * 필드명을 작성하 방식이 baeldung.com에 명시되어 있다.
 * 
 * 만일 스키마에 musicians, musician이라면 리턴되는 타입에 따라 3가지 방식을 적용할 수 있다.
 * 
 * 1. musicians
 * 2. isMusicians 만일 boolean을 리턴한다면
 * 3. getMusicians
 * 
 * 여기서는 스키마에 정의된 필드 명으로 작성한다. 
 * 
 * @see https://www.baeldung.com/spring-graphql
 * 
 */
@Component
public class QueryResolver implements GraphQLQueryResolver {

	@Autowired
	private MusicianRepository musicianRepository;

	@Autowired
	private AlbumRepository albumRepository;
	
	/**
	 * get musician by id
	 * 
	 * @param id
	 * @return Musician
	 */
	public Musician musician(long id) {
		return musicianRepository.findById(id).orElseGet(Musician::new);
	}
	
	/**
	 * 
	 * get musician list
	 * 
	 * @return List<Musician>
	 */
	public List<Musician> musicians() {
		return musicianRepository.findAll();
	}
	
	/**
	 * get album by id
	 * 
	 * @param id
	 * @return Album
	 */
	public Album album(long id) {
		return albumRepository.findById(id).orElseGet(Album::new);
	}
	
	/**
	 * 
	 * get album list
	 * 
	 * @return List<Album>
	 */
	public List<Album> albums() {
		return albumRepository.findAll();
	}
	
}

```
 
query에 대한 부분은 GraphQLQueryResolve을 구현하면 된다.

mutation역시 비슷하리라 예상이 가능할 것이다.

MutationResolver.java

```
package io.basquiat.music.resolver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;

import io.basquiat.music.models.Album;
import io.basquiat.music.models.Musician;
import io.basquiat.music.repo.AlbumRepository;
import io.basquiat.music.repo.MusicianRepository;

/**
 * 
 * created by basquiat
 * 
 * Mutation Resolver
 * 
 * 필드명을 작성하 방식이 baeldung.com에 명시되어 있다.
 * 
 * 만일 스키마에 musicians, musician이라면 리턴되는 타입에 따라 3가지 방식을 적용할 수 있다.
 * 
 * 1. musicians
 * 2. isMusicians 만일 boolean을 리턴한다면
 * 3. getMusicians
 * 
 * 여기서는 스키마에 정의된 필드 명으로 작성한다. 
 * 
 * @see https://www.baeldung.com/spring-graphql
 * 
 */
@Component
public class MutationResolver implements GraphQLMutationResolver {

	@Autowired
	private MusicianRepository musicianRepository;

	@Autowired
	private AlbumRepository albumRepository;
	
	/**
	 * create musician
	 * 
	 * @param name
	 * @param genre
	 * @return Musician
	 */
	public Musician createMusician(String name, String genre) {
		Musician musician = Musician.builder()
									.name(name)
									.genre(genre)
									.build();
		return musicianRepository.save(musician);
	}
	
	/**
	 * 
	 * update musician
	 * 
	 * @param id
	 * @param name
	 * @param genre
	 * @return Musician
	 */
	@Transactional
	public Musician updateMusician(long id, String name, String genre) {
		// id로 뮤지션을 찾아온다.
		Musician musician = musicianRepository.findById(id).orElseGet(Musician::new);
		// dirty checking
		if(!StringUtils.isEmpty(name)) {
			musician.setName(name);
		}

		if(!StringUtils.isEmpty(genre)) {
			musician.setGenre(genre);
		}
		return musician;
	}

	/**
	 * 
	 * delete musician
	 * 
	 * @param id
	 * @return boolean
	 */
	public boolean deleteMusician(long id) {
		musicianRepository.deleteById(id);
		return musicianRepository.existsById(id);
	}
	
	/**
	 * 
	 * create album
	 * 
	 * @param id
	 * @param title
	 * @param releasedYear
	 * @return Album
	 */
	public Album createAlbum(long id, String title, String releasedYear) {
		Musician musician = musicianRepository.findById(id).orElseGet(Musician::new);
		Album album = Album.builder()
						   .musician(musician)
						   .title(title)
						   .releasedYear(releasedYear)
						   .build();
		return albumRepository.save(album);
	}

	/**
	 * update album
	 * 
	 * @param id
	 * @param title
	 * @param releasedYear
	 * @return Album
	 */
	@Transactional
	public Album updateAlbum(long id, String title, String releasedYear) {
		Album album = albumRepository.findById(id).orElseGet(Album::new);
		// dirty checking
		if(!StringUtils.isEmpty(title)) {
			album.setTitle(title);
		}
		
		if(!StringUtils.isEmpty(releasedYear)) {
			album.setReleasedYear(releasedYear);
		}
		return album;
	}
	
	/**
	 * delete album
	 * 
	 * @param id
	 * @return boolean
	 */
	public boolean deleteAlbum(long id) {
		albumRepository.deleteById(id);
		return albumRepository.existsById(id);
	}
	
}


```

GraphQLMutationResolver을 구현하면 된다.

다만 메소드 이름을 정의할 때는 규칙이 있다.

위에 코드 내의 설명을 참조하면 될듯 싶다.


## 번외

Field Resolvers for Complex Values

예를 들면 

```
type Album {
	id: ID
	title: String
	releasedYear: String
	musician: Musician
}
```
위와 같은 경우 musician 필드는 db를 기준으로 album테이블이 아닌 musician테이블에서 정보를 가져오게 되어 있다.

현재 이 토이 프로젝트는 JPA의 @OneToMany, @ManyToOne 을 통해 Musician과 Album과의 연관관계를 가지고 있지만 그렇지 않을 경우 해당 필드에 대한 정보를 Resolver를 통해서 명시적으로 표현하는 방법이 있다.

```
public class AlbomResolver implements GraphQLResolver<Album> {
    
    @Autowired
    private MusicianRepository musicianRepository;
 
 	// 메소드 명칭을 정의 하는 방식에 의해
 	// getMusician이라고 정의해도 무방하다.
    public Musician musician(Album album) {
        return musicianRepository.getOne(album.getMusician().getId());
    }
}

```
처럼 표현할 수 있다.

만일 그냥 예를 들어서 설명하자면

```
type Album {
	id: ID
	title: String
	releasedYear: String
	musician: Musician
	reference: Reference
}
```

처럼 Album에 대한 타입을 정의했다면 Reference부분에 대한 Field Resolvers를 작성할 수 있다.

## API Call

우선 like-controller에서 처럼 호출하면 에러가 난다.

하지만 포스트맨의 베타로 지원하는 GraphQL를 활용하면 적용이 된다.

일단 기존 방식으로 호출하는 방식을 먼저 설명해 보고자 한다.

하지만 일단 이 방식은 좀 짜증이 난다. 


like-controller에서 했던  방식은 Post body text로 선택해서

```
mutation {
    createMusician(name:"Charlie Parker", genre:"jazz") {
        name
        genre
    }
}

```

이런 방식으로 날렸다.

하지만 resolver을 이용해 구현한 경우에는 json형식이어야 한다.


```
{ "query": "mutation { createMusician(name:\"John Coltrane\", genre:\"jazz\") { name genre } }" }

```


![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/use-resolver/capture/capture1.png)

그러면 query도??

```
{"query":"{musicians {name genre albums{ title releasedYear } } }"}

```

![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/use-resolver/capture/capture2.png)

날리는 방식 자체가 약간 지저분하다는 느낌이 든다.


어라? 그러면 like-controller처럼 못하는건가?

아니다.

포스트맨의 GraphQL를 이용하면 된다.

기존처럼

```
mutation {
    createMusician(name:"Charlie Parker", genre:"jazz") {
        name
        genre
        albums {
            title
        }
    }
}

```

![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/use-resolver/capture/capture3.png)

물론 뮤지션 정보를 가져오는 방식도 기존처럼

```
query {
    musicians {
        name
        genre
        albums {
        	title
        	releasedYear
        }
    }
}

```

![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/use-resolver/capture/capture4.png)

cURL로 하는 방식 역시 마찬가지다.

```
curl  -X POST  -H "Content-Type: application/json; charset=utf-8"  -d '{ "query": "{ musician(id:1) { name genre albums { title releasedYear  } } }" }'  http://localhost:8080/music

```

일단 테스트 하는 방식중 GraphiQL를 이용하는 방식이 있다. 다운로드용도 존재하고 standalone 형식으로 pom.xml에

```
<dependency>
    <groupId>com.graphql-java</groupId>
    <artifactId>graphiql-spring-boot-starter</artifactId>
    <version>5.0.2</version>
</dependency>

```

을 통해 접근하는 방식도 있다.

일종의 GraphQL IDE라고 하는데 아직 확인해 보진 않았다.

하지만 여기서는 일단 난중에 해볼 생각이다.     

완료가 되면 그때 시도를....


## 에러 ???

이런 걸 생각해보자.

앨범이나 뮤지션 정보를 업데이트할때 또는 앨범의 경우에는 정보 생성시 뮤지션의 아이디를 함께 넘기는 유효하지 않거나 존재하지 않는 id를 넘겼다면 어떻게 응답을 할까??

![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/use-resolver/capture/capture5.png)

이미지처

```
{
    "data": {
        "createAlbum": null
    },
    "errors": [
        {
            "message": "Internal Server Error(s) while executing query",
            "path": null,
            "extensions": null
        }
    ]
}

```

Internal Server Error(s) while executing query <--를 넘길 것이다.

하지만 클라이언트 입장에서는 에러가 왜 났는지에 대한 이유를 알 수가 없다.

NPE에 대한 에러 처리를 할 필요가 있기 때문에 에러를 해결해야 한다.

## 고전적인 방식의 에러 처리

다음 클래스를 생성할 것이다.

GraphqlNotFoundException.java

```

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

```

간단하게 메세지와 id를 받아서 에러 메세지를 위임할 것이다.



수정한 MusicianMutationResolver.java

```
package io.basquiat.music.resolver.musician;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;

import io.basquiat.exception.GraphqlNotFoundException;
import io.basquiat.music.models.Musician;
import io.basquiat.music.repo.MusicianRepository;

/**
 * 
 * created by basquiat
 * 
 * Mutation Resolver
 * 
 * 필드명을 작성하 방식이 baeldung.com에 명시되어 있다.
 * 
 * 만일 스키마에 musicians, musician이라면 리턴되는 타입에 따라 3가지 방식을 적용할 수 있다.
 * 
 * 1. musicians
 * 2. isMusicians 만일 boolean을 리턴한다면
 * 3. getMusicians
 * 
 * 여기서는 스키마에 정의된 필드 명으로 작성한다. 
 * 
 * @see https://www.baeldung.com/spring-graphql
 * 
 */
@Component
public class MusicianMutationResolver implements GraphQLMutationResolver {

	@Autowired
	private MusicianRepository musicianRepository;
	
	/**
	 * create musician
	 * 
	 * @param name
	 * @param genre
	 * @return Musician
	 */
	public Musician createMusician(String name, String genre) {
		Musician musician = Musician.builder()
									.name(name)
									.genre(genre)
									.build();
		return musicianRepository.save(musician);
	}
	
	/**
	 * 
	 * update musician
	 * 
	 * @param id
	 * @param name
	 * @param genre
	 * @return Musician
	 */
	@Transactional
	public Musician updateMusician(long id, String name, String genre) {
		// id로 뮤지션을 찾아온다.
		Musician musician = musicianRepository.findById(id).orElseGet(Musician::new);
		if(musician.getName() == null) {
			throw new GraphqlNotFoundException("not found musician by id, it doesn't update musician", id);
		}
		// dirty checking
		if(!StringUtils.isEmpty(name)) {
			musician.setName(name);
		}

		if(!StringUtils.isEmpty(genre)) {
			musician.setGenre(genre);
		}
		return musician;
	}

	/**
	 * 
	 * delete musician
	 * 
	 * @param id
	 * @return boolean
	 */
	public boolean deleteMusician(long id) {
		musicianRepository.deleteById(id);
		return musicianRepository.existsById(id);
	}
	
}


```



근데 문제는 다음과 같은 응답을 받는다.


![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/use-resolver/capture/capture6.png)

```
메세지 하단부의 정보

"locations": [
                {
                    "line": 2,
                    "column": 5,
                    "sourceName": null
                }
            ],
            "extensions": {
                "not found musician by id, maybe id doesn't exit": 1
            },
            "errorType": "DataFetchingException"

```

message에서는 그 이유가 잘 설명이 나와서 좋긴 한데 중간에 어마무시한 라인의 exception stackTrace정보도 함께 나온다.

테스트 기준으로 무려 570에 해당하는 정보가 전부 나온다.


## 그럼 어떻게??

찾은 정보는 GraphQLError를 구현한 GraphQLErrorAdapter를 만들고 그것을 통해서 기본적인 GraphQLErrorHandler를 재정의해서 사용한다는 것을 찾았다.


GraphQLErrorAdapter.java

```
package io.basquiat.exception.adapter;

import java.util.List;
import java.util.Map;

import graphql.ErrorType;
import graphql.ExceptionWhileDataFetching;
import graphql.GraphQLError;
import graphql.language.SourceLocation;

/**
 * 
 * created by basquiat
 * 
 * GraphQLError를 구현한 어댑터를 생성한다. 
 * 
 *
 */
public class GraphQLErrorAdapter implements GraphQLError {

	private static final long serialVersionUID = 6820355151702777990L;

	private GraphQLError error;

    public GraphQLErrorAdapter(GraphQLError error) {
        this.error = error;
    }

    @Override
    public Map<String, Object> getExtensions() {
        return error.getExtensions();
    }

    @Override
    public List<SourceLocation> getLocations() {
        return error.getLocations();
    }

    @Override
    public ErrorType getErrorType() {
        return error.getErrorType();
    }

    @Override
    public List<Object> getPath() {
        return error.getPath();
    }

    @Override
    public Map<String, Object> toSpecification() {
        return error.toSpecification();
    }

    @Override
    public String getMessage() {
        return (error instanceof ExceptionWhileDataFetching) ? ((ExceptionWhileDataFetching) error).getException().getMessage() : error.getMessage();
    }

}


```


GraphQLErrorConfiguration.java

```
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


```

![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/use-resolver/capture/capture7.png)


![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/use-resolver/capture/wow.gif)




일단 다음 graphql-java-servlet에 존재하는 

```
package graphql.servlet;

import graphql.ExceptionWhileDataFetching;
import graphql.GraphQLError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Andrew Potter
 */
public class DefaultGraphQLErrorHandler implements GraphQLErrorHandler {

    public static final Logger log = LoggerFactory.getLogger(DefaultGraphQLErrorHandler.class);

    @Override
    public List<GraphQLError> processErrors(List<GraphQLError> errors) {
        final List<GraphQLError> clientErrors = filterGraphQLErrors(errors);
        if (clientErrors.size() < errors.size()) {

            // Some errors were filtered out to hide implementation - put a generic error in place.
            clientErrors.add(new GenericGraphQLError("Internal Server Error(s) while executing query"));

            errors.stream()
                .filter(error -> !isClientError(error))
                .forEach(error -> {
                    if(error instanceof Throwable) {
                        log.error("Error executing query!", (Throwable) error);
                    } else {
                        log.error("Error executing query ({}): {}", error.getClass().getSimpleName(), error.getMessage());
                    }
                });
        }

        return clientErrors;
    }

    protected List<GraphQLError> filterGraphQLErrors(List<GraphQLError> errors) {
        return errors.stream()
            .filter(this::isClientError)
            .collect(Collectors.toList());
    }

    protected boolean isClientError(GraphQLError error) {
        if (error instanceof ExceptionWhileDataFetching) {
            return ((ExceptionWhileDataFetching) error).getException() instanceof GraphQLError;
        }
        return !(error instanceof Throwable);
    }
}

```
위 코드를 보면 발생한 모든 에러의 정보들을 리스트에 담아 내고 있다. 하지만 GraphQLErrorConfiguration.java 코드를 보면 GraphQLErrorAdapter.java에서 override한 녀석들만 리스트에 담게 되어 있다.

## JPA와 GraphQL의 궁합

[Why-Fetch-Lazy-JPA](https://github.com/basquiat78/graphql-springboot2/blob/use-resolver/Why-Fetch-Lazy-JPA.md)


- 어떤 고수분의 피드백

자바 진영이 아닌 다른 분의 피드백 내용중 이런 내용을 받았습니다.

'JPA와의 궁합을 따질 것은 아닙니다. 글에서 설명했던 번외쪽의 Field Resolvers for Complex Values를 통해서 JPA에서 말하는 지연 로딩을 비슷한 방식으 구현이 가능합니다.'

흠...

일대다 양방향/단방향 매핑을 하지 않더라도 Field Resolvers를 구현해서 할 수 있다는 의미로 들린다.

이건 테스트 코드를 짜서 테스트 결과를 올려볼 생각이다.

## At A Glance

작성한 시점에서 query, mutation resolver를 리소스 별로 나눠서 작성하는 것이 더 좋을 것 같아서 현재는 예제 코드와는 달리 분리해서 작성을 완성했다.

테스트시 뭔 짓을 해도 LazyInitializationException때문에 페치 전략을 @OneToMany에서 EAGER로 변경했었다.

하지만 설정을 하나 빠뜨려 지금까지 삽질했다니...

아직까지는 JPA하수인가 보다....

또한 N+1 문제는 어떻게??? 

예제 자체가 작아서 많은 부분을 체크하지 못한것은 실수이긴 하다.

예를 들면 뮤지션과 앨범의 관계를 고려해 볼때 예제 자체가 가지고 있는 경우의 수가 매우 작기 때문이다.

일단 GraphQL에 집중하고 해당 문제는 뒤로 미뤄두자...(안좋은 습관...)

## Next 

현재까지는 에러에 대한 어떤 처리도 하지 않았다.

아마도 에러가 나면 클라이언트에 불필요한 에러 정보가 보내질 것이 뻔한테 이 브랜치에서는 에러 핸들링을 해볼 생각이다. (완료)

다음은 어노테이션으로 스키마, api를 정의하는 방식을 구현할 예정이다.
 