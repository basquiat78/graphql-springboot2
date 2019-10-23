## Like Controller

생각외로 이 방식은 좀 약간 지저분하다.

그 이유는 차후 설명하겠지만 이런 방식으로 확장하는 것은 아름답지 못해 보인다.

다만 아주 작은 microservice에는 적합하지 않나 생각을 한다.

## Prerequisites

OS: 			Window10
Java: 		1.8.x
IDE: 		Spring Tool Suite version 3.9.7
Framework: 	Spring Boot 2.1.9.RELEASE
plugin: 		Lombok Plugin
RDBMS: 		In-Memory Database H2

## Definition Your Schema

musician.graphqls

```
schema {
	query: Query
	mutation: Mutation
}

type Musician {
	id: ID
	name: String
	genre: String
	albums: [Album]
}

type Query {
	musicians: [Musician]
	musician(id: ID): Musician
}

type Mutation {
	createMusician(name: String!, genre: String!): Musician!
	updateMusician(id: ID!, name: String!, genre: String!): Musician!
	deleteMusician(id: ID!): Boolean
}

```

album.graphqls

```
type Album {
	id: ID
	title: String
	releasedYear: String
	musician: Musician
}

extend type Query {
	albums: [Album]
	album(id: ID): Album!
}

extend type Mutation {
	createAlbum(id: ID, title: String!, releasedYear: String!): Album
	updateAlbum(id: ID, title: String!, releasedYear: String!): Album!
	deleteAlbum(id: ID): Boolean
}

```

gRPC와 비슷한듯 아닌듯 한 일종의 유즈케이스 명세서를 작성한다. schema라고 하는데 일단 위에 musician.graphqls를 확인해 보자.

스키마라는 것을 정의하고 있는데 보면 query/mutation으로 정의를 하고 있다.

Query는 말 그대로 일종의 어떤 정보를 원하거나 하는 질의를 의미하고 Mutation은 어떤 정보에 대한 액션을 취한다고 보면 된다.

사실 이게 맞는 건지 모르겠지만 공식 사이트에서 보여준 예제를 보면 대충 저 의미가 맞는것 같다.

맨 처음 type으로 Musician을 정의하고 있다. 아마도 익숙하지 않을까?

이 정의는 뮤지션이며 뮤지션이 갖일 수 있는 정보를 표현하고 있다. 뭐 수많은 정보를 담을 수 있겠지만 가장 기본적으로 뮤지션 명과 해당 뮤지션의 음악 장르, 그리고 해당 뮤지션이 발표한 앨범의 정보를 담는 일종의 객체로 보면 된다.

그리고 밑에 Query를 정의하고 있다. 위에 언급했던 말을 상기한다면 여기에는 당연히 뮤지션 정보를 가져오겠다는 의미를 담고 있다.

예를 들면  

```
type Query {
	musicians: [Musician]
	musician(id: ID): Musician
}
```

위에서 내가 서버로 musicians라는 질의를 하면 뮤지션들의 리스트를 보여준다는 일종의 서비스 명세서를 표현하고 있으며 뮤지션 정보의 아이디를 서버에 넘겨주면 해당 뮤지션의 정보를 보여준다는 의미를 담고 있다.

mutation관련 부분은 따로 설명하지 않겠다. 비슷한 의미니깐...

album.graphqls 파일 정보를 보면 특이한 부분을 볼 수 있는데 extend라는 예약어를 볼 수 있다.

이것은 하나의 파일에 모든 것을 정의하기 보다는 어떤 특정 리소스를 중심으로 정의하고 자 할때 최초 작성한 부분 즉 musician.graphqls에 이미 Query, Mutation을 정의했기 때문에 확장하겠다는 의미로 볼 수 있다.

스키마는 유연하게 확장할 수 있는 장점이 있다.

### ORM

Musician.java

```
package io.basquiat.music.models;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * 
 * Musician Entity
 * 
 * created by basquiat
 *
 */
@Builder
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "musician")
public class Musician {

	/** 뮤지션 유니크 아이디 */
	@Id
	@GeneratedValue(strategy= GenerationType.AUTO)
	private long id;
	
	/** 뮤지션 이 */
	private String name;
	
	/** 뮤지션의 주요 음악 장르 */
	private String genre;
	
	@OneToMany(cascade=CascadeType.ALL, mappedBy="musician")
	private List<Album> albums;

}

```

Album.java

```
package io.basquiat.music.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * 
 * Album Entity
 * 
 * created by basquiat
 *
 */
@Builder
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "album")
public class Album {

	/** 앨범 아이디 */
	@Id
	@GeneratedValue(strategy= GenerationType.AUTO)
	private long id;
	
	/** 음반 명 */
	private String title;

	/** 릴리즈된 년도 */
	@Column(name = "released_year")
	private String releasedYear;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "musician_id", nullable = false, updatable = false)
	private Musician musician;
	
}


```

뮤지션과 앨범에 대해 다음과 같이 정의를 한다. 일단 1:N 양방향 매핑을 하고 있으며 뮤지션의 경우 영속성 전이를 ALL로 해두었기 때문에 뮤지션을 삭제할 경우 그 뮤지션의 음반도 전부 삭제하게 만들어 두었다.

실제로는 이것은 정책에 따라 달라질 수 있을 것이다. 뮤지션은 지워져도 해당 뮤지션의 음반은 히스토리로 남긴다던지...

JPA는 일단 뒤로 두고 이제부터 실제 구현이 어떻게 되어 있는지 확인해 보자.

## Controller

MusicianController.java

```
package io.basquiat.music.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import graphql.ExecutionResult;
import io.basquiat.music.service.MusicianService;

/**
 * 
 * created by basquiat
 *
 * 일반적인 API와는 하나의 엔드포인트만 존재한다.
 * 따라서 스프링 컨트롤러는 PostMapping의 하나의 엔드포인트만 존재한다.
 *
 */
@RestController
@RequestMapping("/musicians")
public class MusicianController {

	@Autowired
	private MusicianService musicianService;
	
	@PostMapping
 	public ExecutionResult getCoffeeByQuery(@RequestBody String query) {
        return musicianService.execute(query);
    }
	
}
```

정말 심플하다. 엔드포인트는 딱 하나! POST /musicians 이게 전부이며 모든 것은 커맨드 패턴으로 구성된 execute메소드가 들어오는 요청 정보를 해석하고 그에 따른 로직을 실행하게 될것이다.


```
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

```

이게 전부다.

그렇다면 핵심적인 부분을 한번 살펴보자.

```
package io.basquiat.music.service.fetcher;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.basquiat.music.service.fetcher.album.AlbumCreateMutation;
import io.basquiat.music.service.fetcher.album.AlbumDataFetcher;
import io.basquiat.music.service.fetcher.album.AlbumDeleteMutation;
import io.basquiat.music.service.fetcher.album.AlbumListDataFetcher;
import io.basquiat.music.service.fetcher.album.AlbumUpdateMutation;
import io.basquiat.music.service.fetcher.music.MusicianCreateMutation;
import io.basquiat.music.service.fetcher.music.MusicianDataFetcher;
import io.basquiat.music.service.fetcher.music.MusicianDeleteMutation;
import io.basquiat.music.service.fetcher.music.MusicianListDataFetcher;
import io.basquiat.music.service.fetcher.music.MusicianUpdateMutation;

/**
 * 
 * created by basquiat
 * 
 * graphQL provider
 * classPath의 정의된 스키마를 읽어와 해당 타입 (Query, Mutation)에 정의된 로직을 매핑하는 클래스
 * 
 * @see classPath: musician.graphqls
 * @see classPath: album.graphqls
 *
 */
@Component
public class GraphQLProvider {

	@Value("classpath:musician.graphqls")
	Resource musician;
	
	@Value("classpath:album.graphqls")
	Resource album;
	
	@Autowired
	private MusicianDataFetcher musicianDataFetcher;
	
	@Autowired
	private MusicianListDataFetcher musicianListDataFetcher;
	
	@Autowired
	private MusicianCreateMutation musicianCreateMutation;
	
	@Autowired
	private MusicianUpdateMutation musicianUpdateMutation;
	
	@Autowired
	private MusicianDeleteMutation musicianDeleteMutation;
	
	@Autowired
	private AlbumDataFetcher albumDataFetcher;
	
	@Autowired
	private AlbumListDataFetcher albumListDataFetcher;
	
	@Autowired
	private AlbumCreateMutation albumCreateMutation;
	
	@Autowired
	private AlbumUpdateMutation albumUpdateMutation;
	
	@Autowired
	private AlbumDeleteMutation albumDeleteMutation;
	
	private GraphQL graphQL;
	
	@PostConstruct
	public void setupSchema() throws IOException {
		
		File musicianSchema = musician.getFile();
		File albumSchema = album.getFile();
		
        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(musicianSchema);
        typeRegistry.merge(new SchemaParser().parse(albumSchema));
        
        RuntimeWiring wiring = RuntimeWiring.newRuntimeWiring()
                							.type(newTypeWiring("Query").dataFetcher("musician", musicianDataFetcher)
									                        		  .dataFetcher("musicians", musicianListDataFetcher)
									                        		  .dataFetcher("album", albumDataFetcher)
									                        		  .dataFetcher("albums", albumListDataFetcher)
									                        		    
        									)
                							.type(newTypeWiring("Mutation").dataFetcher("createMusician", musicianCreateMutation)
			                										  	.dataFetcher("updateMusician", musicianUpdateMutation)
			                											.dataFetcher("deleteMusician", musicianDeleteMutation)
			                											.dataFetcher("createAlbum", albumCreateMutation)
			                											.dataFetcher("updateAlbum", albumUpdateMutation)
			                											.dataFetcher("deleteAlbum", albumDeleteMutation)
                									)
            								.build();
        GraphQLSchema graphQLSchema = new SchemaGenerator().makeExecutableSchema(typeRegistry, wiring);
        graphQL = GraphQL.newGraphQL(graphQLSchema).build();
	
	}

	@Bean
	public GraphQL graphQL() {
		return this.graphQL;
	}
	
}


```

서버가 뜰 때 각 스키마를 읽어와서 TypeRegistry 객체로 파싱하게 된다. 현재는 두 개의 스키마로 나눴기 때문에 merge를 통해서 2개의 스키마를 통합할 수 있다.

그리고 각 Query, Mutation에 따른 로직을 정의한다.

그 중에 하나만 살펴보자.


```
package io.basquiat.music.service.fetcher.music;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.basquiat.music.models.Musician;
import io.basquiat.music.repo.MusicianRepository;

/**
 * 
 * created by basquiat
 *
 * musician select query class
 *
 */
@Component
public class MusicianDataFetcher implements DataFetcher<Musician> {

	@Autowired
	private MusicianRepository musicianRepository;
	
	@Override
	public Musician get(DataFetchingEnvironment environment) {
		long id = Long.parseLong(environment.getArgument("id"));
		return musicianRepository.findById(id).orElse(null);
	}

}


```
다시 한번 뮤지션에 대한 스키마를 확인해 보자

```
musician(id: ID): Musician

```

id를 던지면 해당 id에 해당하는 뮤지션을 가져오는 구조이다.

이렇게 일일이 하나의 서비스 명세서에 대한 클래스를 작성하고 GraphQLProvider에 정의를 한다.

나머지는 반복적인 작업이다.

그러면 이제 실제로 어떻게 하는지 POSTMAN을 통해서 알아보자.


## Request

현재 postman이 베타로 graphql를 지원하지만 이상하게 에러가 발생한다.

그래서 Body -> raw 를 선택해서 날려보고자 한다.


일단 뮤지션 정보를 넣어보자.

```
mutation {
    createMusician(name:"Charlie Parker", genre:"jazz") {
        name
        genre
    }
}

```

위에는 이렇게 해석하면 쉬울 것 같다.

'나는 뮤지션을 만들건데 이름은 Charlie Parker고 음악 장르는 jazz야. 그리고 나서 인서트 이후 보여줄 정보를 이름과 장르만 보여줘.'

또는 

'보여줄 정보에는 이름만 있으면 되'

라고도 할 수 있다.

간단하다.


```
mutation {
    createMusician(name:"Charlie Parker", genre:"jazz") {
        name
    }
}

```
자 저렇게만 하면 된다.

![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/like-controller/capture/capture1.png)
    
        
    
    
이제 뮤지션의 정보를 한번 가져와 보자

```
{
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

일단 앨범 정보를 생성하지 않았기 때문에 빈 배열로 나온다.

![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/like-controller/capture/capture2.png)


어 나는 그냥 이름만 알고 싶어라면 

```
{
    musicians {
        name
    }
}


```

![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/like-controller/capture/capture3.png)


내가 질의할 때 보고자 하는 정보를 정의하면 그 정보만 보여주게 되어 있다.

일단 뭔가가 좀 부족해 보이니 앨범 정보를 한번 넣어보자.

3. 앨범 정보를 생성해 보자.

```
{
    musicians {
        id
        name
        
    }
}

```

![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/like-controller/capture/capture4.png)

정보를 보니 Charlie Parker의 아이디는 1이다.

그럼 다음과 같이 앨범을 한번 넣어보자.


```
mutation {
    createAlbum(id:1, title:"With String", releasedYear:"1950") {
        title
        releasedYear
        musician {
        	name
        	genre
        }
    }
}

```

이젠 익숙해 질 듯 한데 저 정보만을 보면 어떤 값이 리턴되어 돌아올지 알 수 있다.

아래 그림처럼

![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/like-controller/capture/capture5.png)


자 이제 처음으로 돌아가서

뮤지션의 정보를 가져와 보자

```
{
    musician(id:1) {
        name
        genre
        albums{
        	title
        }
    }
}

```

![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/like-controller/capture/capture6.png)


```
{
    musician(id:1) {
        name
        genre
        albums{
        	title
        	releasedYear
        }
    }
}

```

정보를 가져오는데 앨범의 릴리즈 년도까지 보고 싶다면

![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/like-controller/capture/capture7.png)


업데이트도 해봐야지?

```
mutation {
    updateMusician(id: 2, name:"", genre:"jazzzzzzz") {
        name
        genre
    }
}

```

![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/like-controller/capture/capture8.png)

그리고 실제로 제대로 업뎃이 됬는지 확인해 보자.

```
{
    musician(id:2) {
        name
        genre
        albums{
        	title
        	releasedYear
        }
    }
}

```

![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/like-controller/capture/capture9.png)