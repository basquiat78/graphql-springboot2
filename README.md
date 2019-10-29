## Field Resolver

이것은 받은 피드백을 중심으로 작성한 브랜치이다.

사실 이 방식으로 하는게 맞는건지 의문이 들지만 어찌되었든 작동은 한다.

다만 JPA를 사용한다면 JPA가 제공하는 것들을 최대한 적용하고 발생하는 문제들, 예를 들면 1:N 양방향 매핑시 발생하는 N+1같은 문제에 대해서는 해결해 나가는 것이 아무래도 개발자의 태도가 아닌가 싶은데 일단 그런 것은 뒤로 넘겨보자.

일단 스키마는 각 도메인과의 연관 관계만 설정하는 선에서 끝낸다. 기존과 크게 다르지 않다.

```
schema {
	query: Query
	mutation: Mutation
}

type Musician {
	id: ID
	name: String
	age: Int @default(value: 0)
	genre: String
	albums: [Album] @relation(name: "Albums")
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

type Album {
	id: ID
	title: String
	releasedYear: String
	musician: Musician @relation(name: "Musician")
}

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

스키마의 변경은 아무래도 앨범쪽에 변경이 생겼다.

그럼 변경된 엔티티도 확인해 보자.

Musician.java

```
package io.basquiat.music.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
	
	/** 뮤지션 이름 */
	private String name;
	
	/** 뮤지션 나이 */
	private int age;
	
	/** 뮤지션의 주요 음악 장르 */
	private String genre;

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

	/** 해당 앨범의 뮤지션 아이 */
	@Column(name = "musician_id")
	private long musicianId;
	
}

```

두 도메인은 어떤 연관관계도 갖고 있지 않다.

다만 Album쪽은 뮤지션의 아이디를 관리하게만 변경되었다.

또한 변경된 엔티티로 인해서 AlbumMutationResolver.java에도 create시 살짝 변경이 되었다.

## Definition Field Resolver

그러면 새로 추가된 Field Resolver를 확인해 보자.

MusicianFieldResolver.java

```
package io.basquiat.music.resolver.musician;

import java.util.List;

import org.springframework.stereotype.Component;

import com.coxautodev.graphql.tools.GraphQLResolver;

import io.basquiat.music.models.Album;
import io.basquiat.music.models.Musician;
import io.basquiat.music.repo.AlbumRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * Album Field Resolver
 * 
 * created by basquiat
 *
 */
@Component
@Slf4j
public class MusicianFieldResolver implements GraphQLResolver<Musician> {

	private final AlbumRepository albumRepository;

	/**
	 * constructor
	 * @param albumRepository
	 */
	public MusicianFieldResolver(AlbumRepository albumRepository) {
		this.albumRepository = albumRepository;
	}
	
	/**
	 * get albums by musician id
	 * @param musician
	 * @return List<Album>
	 */
	public List<Album> getAlbums(Musician musician) {
		log.info("msucian id ---> " + musician.getId());
		return albumRepository.findByMusicianId(musician.getId());
	}
	
}

```

AlbumFieldResolver.java

```
package io.basquiat.music.resolver.album;

import org.springframework.stereotype.Component;

import com.coxautodev.graphql.tools.GraphQLResolver;

import io.basquiat.music.models.Album;
import io.basquiat.music.models.Musician;
import io.basquiat.music.repo.MusicianRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * Field Resolver
 * 
 * created by basquiat
 *
 */
@Component
@Slf4j
public class AlbumFieldResolver implements GraphQLResolver<Album> {

	private final MusicianRepository musicianRepository;

	/**
	 * constructor
	 * @param musicianRepository
	 */
	public AlbumFieldResolver(MusicianRepository musicianRepository) {
		this.musicianRepository = musicianRepository;
	}
	
	/**
	 * 
	 * get musician by musician id
	 * 
	 * @param album
	 * @return Musician
	 */
	public Musician getMusician(Album album) {
		log.info("musician id ---> " + album.getMusicianId());
		return musicianRepository.findById(album.getMusicianId()).orElseGet(Musician::new);
	}
	
}

```

다른 건 따로 바꾼 것은 없다.

GraphQLResolver의 역활은 서로 다른 도메인을 필드로 사용하는 경우 해당 필드에 대한 해석기로 보면 된다.

예를 들면 Album입장에서 뮤지션의 정보를 요청받을 경우 스키마의 정의된 필드에 대한 해석을 GraphQLResolver가 처리하고 매핑시켜주는 역할을 한다고 보면 맞을 거 같은데 그럼 이제 실제로 그렇게 작동하는지 한번 확인해 볼까 한다.


## 진짜 그렇게 작동하니??


일단 데이터를 처음 생성하는 단계는 건너 뛰겠다.

자 우선 다음과 같이 호출을 해보자.

```
query{
    musicians {
        name
        genre
         
  
    }
}

```

![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/use-resolver-other/capture/capture1.png)

일단 원하는 대로 정보가 나왔다.

그럼 실제로 찍힌 로그도 살펴보자.

```
Hibernate: select musician0_.id as id1_1_, musician0_.age as age2_1_, musician0_.genre as genre3_1_, musician0_.name as name4_1_ from musician musician0_

```
![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/use-resolver-other/capture/capture2.png)

당연한 결과일 것이다.

그럼 이제는 앨범 정보까지 한번 가져와 보자

```
query{
    musicians {
        name
        genre
        albums{
        	title
        	releasedYear
        }
  
    }
}

```

![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/use-resolver-other/capture/capture3.png)

이제는 놀랍지 않다. 너무 당연하니깐.

그러면 실제 로그는??

```
Hibernate: select musician0_.id as id1_1_, musician0_.age as age2_1_, musician0_.genre as genre3_1_, musician0_.name as name4_1_ from musician musician0_
--- 이전 테스트 로그 ---

--- 현재 로그 ---
Hibernate: select musician0_.id as id1_1_, musician0_.age as age2_1_, musician0_.genre as genre3_1_, musician0_.name as name4_1_ from musician musician0_

--- 실제로 해당 MusicianFieldResolver를 탔는지 확인하기 위해 쩍은 로그 ---
2019-10-29 13:08:13 - msucian id ---> 1
Hibernate: select album0_.id as id1_0_, album0_.musician_id as musician2_0_, album0_.released_year as released3_0_, album0_.title as title4_0_ from album album0_ where album0_.musician_id=?

```

![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/use-resolver-other/capture/capture4.png)

마치 JPA의 일대다 양방향 매핑을 한거와 비슷한 결과를 가져온다.

그럼 당연히 앨범도 그런지 확인해 보자.
콘솔 로그를 일단 지우고...

```
query {
    albums {
        title
        releasedYear
         
    }
}
```

![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/use-resolver-other/capture/capture5.png)

역시 예상대로 나온다.

```
Hibernate: select album0_.id as id1_0_, album0_.musician_id as musician2_0_, album0_.released_year as released3_0_, album0_.title as title4_0_ from album album0_


```

![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/use-resolver-other/capture/capture6.png)

이제는 뮤지션의 정보를 불러와서 한번 보자.


```
query {
    albums {
        title
        releasedYear
        musician {
            name
            age
            genre
        }
    }
}

```

![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/use-resolver-other/capture/capture7.png)

그럼 로그도 역시 한번 살펴봐야지.

```
Hibernate: select album0_.id as id1_0_, album0_.musician_id as musician2_0_, album0_.released_year as released3_0_, album0_.title as title4_0_ from album album0_





Hibernate: select album0_.id as id1_0_, album0_.musician_id as musician2_0_, album0_.released_year as released3_0_, album0_.title as title4_0_ from album album0_
2019-10-29 13:19:05 - musician id ---> 1
Hibernate: select musician0_.id as id1_1_0_, musician0_.age as age2_1_0_, musician0_.genre as genre3_1_0_, musician0_.name as name4_1_0_ from musician musician0_ where musician0_.id=?

```

![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/use-resolver-other/capture/capture8.png)
