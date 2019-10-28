## 페치 전략

[hibernate-lazy-eager-loading](https://www.baeldung.com/hibernate-lazy-eager-loading)    

[즉시로딩과 지연로딩, 컬렉션 래퍼](https://yellowh.tistory.com/126)

JPA에서 @OneToMany, @ManyToOne 관계에서 페치 전략을 Lazy로 하는 이유는 GraphQL의 특징에 기인한다.


fetch = FetchType.EAGER가 디폴트이기 때문에 일반적으로 @OneToMany에서는 LAZY, @ManyToOne에서는 EAGER 페치 전략을 많이 사용한다.


하지만 GraphQL에서는 글로벌 페치 전략을 둘 다 Lazy로 하는 것이 맞다고 개인적으로 생각한다. (그냥 개인적인 생각이다.)

아마도 눈썰미가 있으신 분들은 이미 눈치챘을거 같다.

암튼 이에 대한 JPA이론은 생략하겠지만 왜 그렇게 가야하는지에 대한 것을 실제 로그와 스크린 샷으로 설명해 보고자 한다.


## 그럼 왜 둘 다 LAZY전략으로 가는데??

일반적으로 myBatis같은 경우에 join을 하게 될 경우 하나의 쿼리로 날아가지만 JPA의 경우에는 좀 다른 방식으로 작동한다.

이것은 EAGER냐 LAZY냐에 따라 좀 다르게 작동한다.

기존의 코드에서 Album.java를 살펴보자.

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
	
	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "musician_id", nullable = false, updatable = false)
	private Musician musician;
	
}

```

EAGER로 설정된 경우 실제로 포스트맨에서 어떤 일이 벌어지는 지 확인해 보자.


![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/use-resolver/capture/capture8.png)

위 이미지처럼 날리면 실제 JPA는 어떻게 동작하나 확인해 보자.

![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/use-resolver/capture/capture9.png)

```
Hibernate: select album0_.id as id1_0_, album0_.musician_id as musician4_0_, album0_.released_year as released2_0_, album0_.title as title3_0_ from album album0_
Hibernate: select musician0_.id as id1_1_0_, musician0_.age as age2_1_0_, musician0_.genre as genre3_1_0_, musician0_.name as name4_1_0_ from musician musician0_ where musician0_.id=?

```

쿼리를 두 번 날리는 것을 볼 수 있다.

그럼 다음과 같이 또 날려보자.

![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/use-resolver/capture/capture10.png)

그럼 JPA는 어떻게 작동할까?

![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/use-resolver/capture/capture11.png)

```
Hibernate: select album0_.id as id1_0_, album0_.musician_id as musician4_0_, album0_.released_year as released2_0_, album0_.title as title3_0_ from album album0_
Hibernate: select musician0_.id as id1_1_0_, musician0_.age as age2_1_0_, musician0_.genre as genre3_1_0_, musician0_.name as name4_1_0_ from musician musician0_ where musician0_.id=?
Hibernate: select album0_.id as id1_0_, album0_.musician_id as musician4_0_, album0_.released_year as released2_0_, album0_.title as title3_0_ from album album0_
Hibernate: select musician0_.id as id1_1_0_, musician0_.age as age2_1_0_, musician0_.genre as genre3_1_0_, musician0_.name as name4_1_0_ from musician musician0_ where musician0_.id=?


```

어라? 나는 뮤지션의 정보를 요청시 쿼리하지 않았지만 내부적으로 뮤지션의 정보를 가져오기 위해 쿼리를 다시 한번 날리는 결과를 가져온다.

그럼 이것을 EAGER에서 LAZY로 바꾸면 어떤 일이 벌어질까??

다음 이미지처럼 한번 날려보자.

![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/use-resolver/capture/capture12.png)

그리고 JPA에서는 어떻게 동작할까?

![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/use-resolver/capture/capture13.png)

```
Hibernate: select album0_.id as id1_0_, album0_.musician_id as musician4_0_, album0_.released_year as released2_0_, album0_.title as title3_0_ from album album0_
Hibernate: select musician0_.id as id1_1_0_, musician0_.age as age2_1_0_, musician0_.genre as genre3_1_0_, musician0_.name as name4_1_0_ from musician musician0_ where musician0_.id=?

```

뭐 당연한 결과이다. 그렇다면 다음과 같이 날려보면 어떨까?

![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/use-resolver/capture/capture14.png)

그럼 JPA에서는 어떻게 동작할까??

![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/use-resolver/capture/capture15.png)


```
Hibernate: select album0_.id as id1_0_, album0_.musician_id as musician4_0_, album0_.released_year as released2_0_, album0_.title as title3_0_ from album album0_
Hibernate: select musician0_.id as id1_1_0_, musician0_.age as age2_1_0_, musician0_.genre as genre3_1_0_, musician0_.name as name4_1_0_ from musician musician0_ where musician0_.id=?
--- 이전에 찍힌 로그 ---

--- 현재 찍힌 로그 ---
Hibernate: select album0_.id as id1_0_, album0_.musician_id as musician4_0_, album0_.released_year as released2_0_, album0_.title as title3_0_ from album album0_

```

아하~ LAZY 전략을 선택했기 때문에 단지 앨범 정보를 가져오는 쿼리 한번만 날아갔다는 것을 확인할 수 있다.

이것이 뭐 얼마나 큰 의미가 있겠냐만은 나름대로 의미가 있다고 생각한다.

물론 예제 자체가 좀 한정적이라 많은 경우를 다루지 못해 놓친 부분들이 분명 있을것이다.

하지만 어떤 면에서 JPA와 GraphQL의 궁합이 잘 맞는다고 봐도 될까???