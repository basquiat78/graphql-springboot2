## Using Annotation

이번 브랜치는 annotation을 활용한 브랜치이다.


기존의 방식은 graphqls라는 파일을 만들어서 스키마를 만들었었다.

하지만 이 방식은 모델 또는 엔티티에 특정 어노테이션을 설정해서 객체 자체가 스키마가 되는 방식이다.

내부적인 로직은 살펴보지 않았지만 아마도 클래식 명이 타입 명으로 그리고 어노테이션이 붙은 필드에 대해서 필드를 매핑하는 형식이 아닌가 싶다.

이 방식이 좋은 이유는 기존에 짜여진 스프링 부트의 코드를 고스란히 사용할 수 있다는 것이다.

다음과 같이 pom.xml에 다음과 같이 세팅만 하면 된다.

```
	<dependency>
		<groupId>io.leangen.graphql</groupId>
		<artifactId>graphql-spqr-spring-boot-starter</artifactId>
		<version>0.0.4</version>
	</dependency>

```

다만 위에 버전을 보면 알겠지만 0.0.4가 최신버전 (2019년 10월 31일 기준) 인데 버전만 봐도 마이너 버전이며 해당 깃헙에서도 ALPHA버전임을 상기시키고 있다.

한마디로 아직은 완성된 것은 아니라는 말이니 그것을 감안해서 사용하라는 의미이다. 

사실 공식 사이트는 com.graphql 깃헙에도 자체 라이브러리를 통해서 어노테이션을 이용한 방식이 이미 존재하고 baeldung사이트에서도 그 예제가 나와있다.

내부 분석은 일단 난중에 하고 어떻게 설정하고 작동하는지 확인해 보자.


## Model 또는 Entity설정

일단 classPath의 resource폴더의 graphql폴더는 밖으로 뺴놓았다.

어노테이션으로 하는 만큼 그래도 기본적인 스키마를 작성하고 그것을 토대로 설정하는게 맞는거 같아서 지우지 않았으니 참조하시면 된다.


Musician.java

```
package io.basquiat.music.models;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import io.leangen.graphql.annotations.GraphQLQuery;
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
	@GraphQLQuery(name = "id")
	private long id;
	
	/** 뮤지션 이름 */
	@GraphQLQuery(name = "name")
	private String name;
	
	/** 뮤지션 나이 */
	@GraphQLQuery(name = "age")
	private int age;
	
	/** 뮤지션의 주요 음악 장르 */
	@GraphQLQuery(name = "genre")
	private String genre;

	/** 뮤지션의 앨범 리스트 */
	@OneToMany(fetch = FetchType.LAZY, cascade=CascadeType.ALL, mappedBy="musician")
	@GraphQLQuery(name = "albums")
	private List<Album> albums;
	
}


```

Album.java

```
package io.basquiat.music.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import io.leangen.graphql.annotations.GraphQLQuery;
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
	@GraphQLQuery(name = "id")
	private long id;
	
	/** 음반 명 */
	@GraphQLQuery(name = "title")
	private String title;

	/** 릴리즈된 년도 */
	@Column(name = "released_year")
	@GraphQLQuery(name = "releasedYear")
	private String releasedYear;

	/** 해당 앨범의 뮤지션 정보 */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "musician_id", nullable = false, updatable = false)
	@GraphQLQuery(name = "musician")
	private Musician musician;
	
}


```

기존의 코드와 좀 달라진 점은 @GraphQLQuery이 붙어 있다는 것이다.

이런 식으로 코드를 읽으면 된다.

'Musician.java에 어노테이션들이 붙었는데 type은 Musician이고 해당 타입이 갖는 필드는 id, name, age, genre, albums를 갖는 코드이다.' 

결국 

```
type Musician {
	id: ID
	name: String
	age: Int @default(value: 0)
	genre: String
	albums: [Album] @relation(name: "Albums")
}

```

이것을 저 위에 스키마처럼 내부적으로 생성할 것이라는 것을 짐작할 수 있게 된다.

 
## How to create Query, Mutation ? 

만일 기존의 스프링 부트의 소스가 있다면 다음과 같이 수정할 수 있게 된다.

그 중에 AlbumService.java 하나만 확인해 보자.


```
package io.basquiat.music.service.album;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import io.basquiat.exception.GraphqlNotFoundException;
import io.basquiat.music.models.Album;
import io.basquiat.music.models.Musician;
import io.basquiat.music.repo.AlbumRepository;
import io.basquiat.music.repo.MusicianRepository;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi;

@Service("albumService")
@GraphQLApi
@Transactional
public class AlbumService {

	private final MusicianRepository musicianRepository;

	private final AlbumRepository albumRepository;

	public AlbumService(MusicianRepository musicianRepository, AlbumRepository albumRepository) {
		this.musicianRepository = musicianRepository;
		this.albumRepository = albumRepository;
	}
	
	/** Query Type */
	
	
	/**
	 * get album by id
	 * 
	 * @param id
	 * @return Album
	 */
	@GraphQLQuery(name = "album")
	public Album getAlbumById(long id) {
		return albumRepository.findById(id).orElseGet(Album::new);
	}
	
	/**
	 * get album list
	 * 
	 * @return List<Album>
	 */
	@GraphQLQuery(name = "albums")
	public List<Album> getAlbumList() {
		return albumRepository.findAll();
	}
	
	
	/** Mutation Type */
	
	/**
	 * 
	 * save album info
	 * 
	 * @param title
	 * @param releasedYear
	 * @param musicianId
	 * @return Album
	 */
	@GraphQLMutation(name = "createAlbum")
	public Album createAlbum(String title, String releasedYear, long musicianId) {
		
		Musician musician = musicianRepository.findById(musicianId).orElseGet(Musician::new);
		
		if(musician.getName() == null) {
			throw new GraphqlNotFoundException("not found musician by id, it doesn't create album", musicianId);
		}
		
		Album album = Album.builder()
						   .musician(musician)
						   .title(title)
						   .releasedYear(releasedYear)
						   .build();
		
		return albumRepository.save(album);
	}
	
	/**
	 * 
	 * update album
	 * 
	 * @param id
	 * @param title
	 * @param releasedYear
	 * @return Album
	 */
	@GraphQLMutation(name = "updateAlbum")
	public Album updateAlbum(long id, String title, String releasedYear) {
		Album album = albumRepository.findById(id).orElseGet(Album::new);
		
		if(album.getTitle() == null) {
			throw new GraphqlNotFoundException("not found album by id, it doesn't update", id);
		}
		
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
	@GraphQLMutation(name = "deleteAlbum")
	public boolean deleteAlbum(long id) {
		albumRepository.deleteById(id);
		return albumRepository.existsById(id);
	}
	
}


```

코드를 보면 기존에 로직을 건드리지 않고 어노테이션만으로 설정하게 되어 있다.


해당 클래스에는 @GraphQLApi를 통해서 이 서비스가 GraphQL의 api를 담당하고 있고 마치 스프링의 @GetMapping, @PutMapping처럼 각 메소드에 그에 해당하는 어노테이션을 붙인다.

잠시 스키마를 보자면

```
extend type Query {
	albums: [Album]
	album(id: ID): Album!
}

extend type Mutation {
	createAlbum(title: String!, releasedYear: String!, musicianId: Int!): Album
	updateAlbum(id: ID, title: String!, releasedYear: String!): Album!
	deleteAlbum(id: ID): Boolean
}

```

query에 해당하는 부분은 @GraphQLQuery, mutation에 해당한다면 @GraphQLMutation를 붙이고 그에 맞는 이름을 생성하는 것만으로 설정을 끝내고 있다.

이게 전부이다.

그러면 엔드포인트를 어떻게 설정할 것이냐?

application.yml에 할 수 있다.


```
graphql:
  spqr:
    http:
      enabled: true
      endpoint: /music
    gui:
      enabled: true
```

graphql.sqpr.http.endpoint에 원하는 path를 설정하면 된다. 설정하지 않으면 기본 /graphql이다.

밑에 gui는 부분이 있는데 이부분은 난중에 테스트시 설명을 하겠다.


## API Call

use-resolver 브랜치에서 했던 방식으로 Postman에서 테스트를 하면 된다.

하지만 위에 graphql.sqpr.gui설정을 통해서 웹 ide로 테스트를 해볼까 한다.

내부적으로 제공하는 이 ide는 기본 접속 url이 존재한다.

[http://localhost:8080/gui](http://localhost:8080/gui)

하지만 저 뒤의 /gui 패스 역시 설정을 할 수 있다.

이 프로젝트에서는 기본 설정을 하지 않았지만 바꾸고 싶다면 graphql.sqpr.gui.endpoint=/blah 처럼 설정하면 된다.

다만 이것을 통해서 테스트시에는 콘솔 로그를 확인하기가 너무 어렵다. 

주기적으로 통신을 하면서 로그를 남기고 있어서인데 이 로그를 없앴 수 있는 설정이 있는지 찾아 보고 있는중...

## Schema 확인하기

자 그럼 위 gui주소로 들어가면 이미지처럼 우측 중간에 2개의 탭을 볼 수 있다. 그 중에 Schema를 클릭하면 설정한 스키마 정보를 보여준다.


![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/useing-annotation/capture/capture1.png)


물론 DOCS로 보는 것이 편할 수도 있다.


![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/useing-annotation/capture/capture2.png)


그럼 실제로 이전처럼 했을떄와 같이 작동하는지 확인해보자.

기존과 같이 작동하기때문에 다른건 다 생략하고 이미지로 대체를 할까 한다.

테스트 순서는 다음과 같다.

1. 뮤지션 정보 생성
2. 뮤지션의 앨범 생성
3. 뮤지션 정보만 가져오기
4. 뮤지션의 정보와 앨범 정보까지 가져오기
5. 앨범 정보도 뮤지션과 마찬가지


![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/useing-annotation/capture/capture3.png)
1. 뮤지션 정보 생성


![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/useing-annotation/capture/capture4.png)
2. 뮤지션의 앨범 생성


![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/useing-annotation/capture/capture5.png)
3. 뮤지션 정보만 가져오기


![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/useing-annotation/capture/capture6.png)
4. 뮤지션의 정보와 앨범 정보까지 가져오기


![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/useing-annotation/capture/capture7.png)
5. 앨범 정보만 가져오기


![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/useing-annotation/capture/capture8.png)
6. 앨범 정보와 뮤지션 정보까지 가져오기


## At A Glance

[graphql-spqr-spring-boot-starter](https://github.com/leangen/graphql-spqr-spring-boot-starter)

해당 깃헙에는 커스텀 관련 정보도 있고 많은 내용들이 있는데 우선 기본적인 예제에만 초점을 맞추고 진행했다.

커스텀 부분은 천천히 진행해 보고자 한다.

이 방법은 어노테이션을 이용한 방식이기때문에 작성하기가 쉽고, 스키마 작성에 대한 고민을 할 필요가 없다.

또한 기존의 스프링 부트로 작성된 로직을 거의 손을 대지 않고 변환할 수 있다는 장점도 크게 작용한다. 

다만 아직은 완전한 버전이 아닌 알파 버전으로 변경시 생길 수 있는 사이드 이펙트에 대해서는 감안하고 사용해야한다는 점이 살짝 불안하다.

웹용 브라우저 ide를 통해서 스키마를 확인할 수 있지만 적어도 모델/엔티티 디자인 타임에서는 최소한 스키마에 대한 이해가 좀 필요한 부분도 있다.

