# graphql-springboot2

[WHAT IS GRAPHQL?](https://graphql-kr.github.io/)

## 특징이 뭔데?

일단 다른 언어쪽은 잘 모르겠지만 자바 진영의 Springframework 또는 Spring Boot를 기준으로 이야기 해 볼까 한다.

예를 들면 우리는 저 위에 프레임워크를 사용했을 때 일반적으로 다음과 같이 프로그래밍을 하게 된다.

```
@Entity
public class Foo {

	private String foo;
	
	private String bar;
	
	getter/setter do Something...

}

```

이렇게 모델 또는 엔티티를 정의할 것이다.

```
@RestController
@RequestMapping("")
public class ApiController {

	// do something DI

	@GetMapping("/get")
	public Foo find() {
		// do Something
		return Foo;
	}
	
	@PostMapping("/post")
	public Foo create() {
		// do Something
		return Foo;		
	}

	@PutMapping("/put")
	public Foo update(String foo) {
		// do Something
		return Foo;	
	}

	@DeleteMapping("/delete")
	public Foo delete() {
		return Foo;
	}

}

```

너무나 익숙한 코드이다. 

필요한 기능의 엔드포인트에 따라  GET, POST, PUT, DELETE (그 이후 애매한 부분을 해결하기 위한 PATCH, OPTION, PURGE등등등 늘어났지만) 그에 따라 위에 코드처럼 정의해서 쓰게 된다.

만일 자잘한 요청 API가 필요하다면 그에 따라 늘어나게 된다.

이 방식으로 오랜 기간 개발을 해 온 입장에서 딱히 불편하다는 것을 느끼진 못했다.

왜냐하면 습관처럼 몸에 베면서 익숙해졌기 때문이 아닌가 싶은데 GraphQL은 좀 독특한 방식으로 작동한다.

일단 위에 어떤 요청을 하게 되면 우리는 내가 원하든 원치 않든 모델, 엔티티에 정의된 모든 필드를 담은 정보를 보게 된다.

예를 들면 


```
info: {
	id: "1",
	title: "Something",
	info1: "info1",
	info2: "info2",
	info3: "info3",
	info4: "",
	info5: "",
	info6: "info6",
	info7: "Nothing",
	info8: "OK",
	info9: "Yeah~",
	info10: null,
	info11: null,
	info12: null,
	info13: null,
	info13: null
	.
	.
	.
	blah blah...
}

```

이런 생각을 해볼 수 있지 않을까?

나는 저 위에 정보들중에 id, title, info1, info3의 정보를 알고 싶은데??

## 누군가가 저렇게 만들어 놓지 않았을까?

facebook이 저렇게 만들어 놨다.

그게 GraphQL이다.

아래 괜찮은 글이 하나 있어서 소개해 본다.

[GraphQL과 RESTful API](https://www.holaxprogramming.com/2018/01/20/graphql-vs-restful-api/)

## Next Episode

Spring boot와 관련 정보를 찾다 보니 3가지 방식으로 구현할 수 있다.

기존의 방식과 controller없이 Resolver를 이용한 방식 (그래서 약간 이질감이 있다) 그리고 어노테이션을 이용한 방식이다.

각기 장점이 있어 보이는데 그 중에 첫 번째 브랜치인 like-controller는 기존의 방식을 유지하는 방식으로 작성한 예제이다.

최종 목표는 저 3가지 방식 구현과 현재 Query, Mutation을 적용한 예제만 있지만 차후 Subscription 적용 및 WebFlux를 활용하는 것이다.

그외 enum같은 것도 제공하는데 이것저것 다 적용해서 최종 완성하는게 목표이다. 


## complete Branch

[like-controller complete](https://github.com/basquiat78/graphql-springboot2/tree/like-controller)
    
[use-resolver complete](https://github.com/basquiat78/graphql-springboot2/tree/use-resolver)

[use-resolver-other complete](https://github.com/basquiat78/graphql-springboot2/tree/use-resolver-other)

[using-annotation](https://github.com/basquiat78/graphql-springboot2/tree/using-annotation)

[graphql-subscription Subscription 구현]
(https://github.com/basquiat78/graphql-springboot2/tree/graphql-subscription)

# At A Glance  

간만에 graphQL을 할 일이 있어서 작년에 작성했던 이 글을 보니 오류가 상당하다.

일단 Entity는 롬복을 썼을 때 하지 말아야 하는 어노테이션을 죄다 붙여놨다. ~~이러지 말자~~

또한 Resolver를 활용하게 될 경우에는 JPA에서 추구하는 객체 지향적인 설계보다 오히려 DB관점에서의 코드가 있다.

예를 들면 

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

뭔가 느낌이 오지 않나? Album 객체를 보게 되면  musicianId를 필드로 갖게 된다.

DB의 ERD를 보고 이것을 사용하게 되면 이렇게 DB 관점에서 엔티티를 설계하게 되는 것이다.

그렇다면 의문점이 든다. Resolver를 써야 하는 것인가 말아야 하는 것인가?

나의 개인적인 입장이라면 기왕 OOP세상과 ORM에서 허우적되겠다고 맘먹은 이상 이 방법보다는 JPA의 객체지향적인 설계를 통해서 작성하게 될거 같다.
