## Use Resolver

이번 브랜치는 컨트롤러가 아닌 리졸버를 활용한 방법이다.

컨트롤러가 없고 Query, Mutation에 맞는 리졸버를 구현함으로써 그것이 대신 컨트롤러 역할을 하게 된다.

## 구조
io.basquiat
 	ㄴ music
 		ㄴ models
 		ㄴ repo
 		ㄴ resolver
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

현재 이 토이 프로젝트는 JPA의 @ManyToOne 을 통해 연관관계를 가지고 있지만 그렇지 않을 경우 해당 필드에 대한 정보를 Resolver를 통해서 명시적으로 표현하는 방법이 있다.

```
public class AlbomResolver implements GraphQLResolver<Albom> {
    
    @Autowired
    private MusicianRepository musicianRepository;
 
 	// 메소드 명칭을 정의 하는 방식에 의해
 	// getMusician이라고 정의해도 무방하다.
    public Musician musician(Albom albom) {
        return musicianRepository.getOne(albom.getMusician().getId());
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

하지만 여기서는 일단 난중에 해볼 생각이다.

## Next 

컨트롤러가 없기 때문에 이 방식의 경우에는 Error Handler에 대한 부분이 필요하다.

이것은 현재 진행중....
