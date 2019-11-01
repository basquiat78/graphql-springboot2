## Subscription

Subscription은 Websocket을 구현한 것이다.

하지만 찾아 본 방법들을 보면서 테스트할 방법이 딱히 없어서 현재 이 브랜치는 다음과 같은 방식을 통해서 테스트 해볼 것이다.

이전 브랜치인 [using-annotation](https://github.com/basquiat78/graphql-springboot2/tree/using-annotation)을 확장할 것이다.

이번 테스트는 웹 ide의 기능을 이용해야 하기 때문인데...

무엇보다 다음과 같은 시나리오를 갖을 생각이다.


## Subscription Scenario


1. 특정 엔트포인트를 리스너를 한다. 총 두개의 엔트포인트를 리스너 할 예정인데 다음과 같다.
	- statusMusician
	- statusAlbum
	
2. 각 엔트포인트는 다음과 같은 코드값을 통해서 각 액션에 해당하는 부분을 구독할 것이다.
	- 뮤지션이 새로 등록될 때
	- 뮤지션의 정보가 변경될 때
	- 앨범이 새로 등록될 때
	- 앨범의 정보가 변경될 때
	
위에 정의한 것처럼 뮤지션 (또는 음반)이 새로 등록되거나 뮤지션 (또는 음반)의 정보가 될 때마다 그 정보를 받아서 보여주는 방식의 테스트를 할 것이다.


## Configuration

Websocket과 Reactor를 이용하기 때문에 다음과 같이 pom.xml에 추가를 하자.

```
	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-websocket</artifactId>
	</dependency>
	
	<dependency>
		<groupId>io.projectreactor</groupId>
		<artifactId>reactor-core</artifactId>
	</dependency>

```

pub/sub에서 어떤 액션인지에 대한 코드값을 구분하기 위해 StatusCode를 관리하는 enum을 만든다.


StatusCode.java

```
package io.basquiat.music.code;

import java.util.Arrays;

/**
 * 
 * enum status code
 * 
 * created by basquiat
 *
 */
public enum StatusCode {

	NEW("new"),
	
	UPDATE("update");
	
	/** enum code */
	public String code;
	
	/** String type constructor */
	StatusCode(String code) {
		this.code = code;
	}

	/**
	 * get Enum Object from code
	 * @param code
	 * @return StatusCode
	 */
	public static StatusCode fromString(String code) {
		return Arrays.asList(StatusCode.values())
					 .stream()
					 .filter( statusCode -> statusCode.code.equalsIgnoreCase(code) )
					 .map(statusCode -> statusCode)
					 .findFirst().orElse(null);
    }
	
}

```

서비스 레벨에서 pub/sub을 구현해야 한다.

그 중에 MusicianService.java만 살펴보자. 앨범 쪽도 똑같기 때문이다.


```
package io.basquiat.music.service.musician;

import java.util.List;

import javax.transaction.Transactional;

import org.reactivestreams.Publisher;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import io.basquiat.exception.GraphqlNotFoundException;
import io.basquiat.music.code.StatusCode;
import io.basquiat.music.models.Musician;
import io.basquiat.music.repo.MusicianRepository;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.GraphQLSubscription;
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi;
import io.leangen.graphql.spqr.spring.util.ConcurrentMultiMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

@Service("musicianService")
@GraphQLApi
@Transactional
public class MusicianService {

	private final MusicianRepository musicianRepository;

	private final ConcurrentMultiMap<String, FluxSink<Musician>> musicianSubscribers = new ConcurrentMultiMap<>();
	
	public MusicianService(MusicianRepository musicianRepository) {
		this.musicianRepository = musicianRepository;
	}
	
	/** Query Type */
	
	/**
	 * get musician by id
	 * 
	 * @param id
	 * @return Musician
	 */
	@GraphQLQuery(name = "musician")
	public Musician musician(long id) {
		return musicianRepository.findById(id).orElseGet(Musician::new);
	}
	
	/**
	 * 
	 * get musician list
	 * 
	 * @return List<Musician>
	 */
	@GraphQLQuery(name = "musicians")
	public List<Musician> musicians() {
		return musicianRepository.findAll();
	}
	
	
	/** Mutation Type */
	
	/**
	 * create musician
	 * 
	 * @param name
	 * @param genre
	 * @return Musician
	 */
	@GraphQLMutation(name = "createMusician")
	public Musician createMusician(String name, String genre) {
		Musician musician = Musician.builder()
									.name(name)
									.genre(genre)
									.build();
		
		// 새로운 뮤지션을 생성한다.
		Musician newMusician = musicianRepository.save(musician);
		
		// 새로운 뮤지션이 생성되면 subscriber에 그 정보를 알려준다.
		musicianSubscribers.get(StatusCode.NEW.code).forEach(subscriber -> subscriber.next(newMusician));
		return newMusician;

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
	@GraphQLMutation(name = "updateMusician")
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
		// 뮤지션 정보가 변경되면 subscriber에 그 정보를 알려준다.
		musicianSubscribers.get(StatusCode.UPDATE.code).forEach(subscriber -> subscriber.next(musician));
		return musician;
	}

	/**
	 * 
	 * delete musician
	 * 
	 * @param id
	 * @return boolean
	 */
	@GraphQLMutation(name = "deleteMusician")
	public boolean deleteMusician(long id) {
		musicianRepository.deleteById(id);
		return musicianRepository.existsById(id);
	}
	
	/**
	 * 
	 * 뮤지션이 새로 등록되거나 또는 update될때 해당 정보를 subscription으로 리스닝하고 있는 클라이언트에 해당 정보를 보내주는 역할을 하게 된다.
	 * 
	 * code는 new, update로 StatusCode.java를 참조하자.
	 * 
	 * @param code
	 * @return Publisher<Musician>
	 */
	@GraphQLSubscription
    public Publisher<Musician> statusMusician(String code) {
		return Flux.create(subscriber -> musicianSubscribers.add(code, subscriber.onDispose(() -> musicianSubscribers.remove(code, subscriber))), FluxSink.OverflowStrategy.LATEST);
    }
	
}


```

Sink라는 의미는 보통 싱크대의 그 싱크이다. 달리 말하면 물을 버리는 곳이라고 할 수 있지만 리액티브 프로그래밍이나 또는 스프링 부트의 WebFlux를 다뤄봤다면 이것은 스트림의 데이터 흐름과 연관이 있다는 것을 알수 있다.

뭐 몰라도 대충 물이 싱크대에서 하수구로 빠져나가는 것을 연상해 보자.

그리고 물을 하나의 데이터로 보면 싱크대의 구멍으로 데이터가 흘러들어서 목적지인 하수구까지 흘러가는 것을 생각해 보면 될거 같다.

예시가 좀 거시기 했지만 자연스럽게 흘러가게 두는 것이다.

위 코드에 보면 FluxSink의 제너릭 타입이 Musician이다. 결국 


```
/**
	 * 
	 * 뮤지션이 새로 등록되거나 또는 update될때 해당 정보를 subscription으로 리스닝하고 있는 클라이언트에 해당 정보를 보내주는 역할을 하게 된다.
	 * 
	 * code는 new, update로 StatusCode.java를 참조하자.
	 * 
	 * @param code
	 * @return Publisher<Musician>
	 */
	@GraphQLSubscription
    public Publisher<Musician> statusMusician(String code) {
		return Flux.create(subscriber -> musicianSubscribers.add(code, subscriber.onDispose(() -> musicianSubscribers.remove(code, subscriber))), FluxSink.OverflowStrategy.LATEST);
    }

```

이 코드는 자연스럽게 구독한 클라이언트의 구독 정보를 가지고 있다가 publisher가 발생하면 Sink를 통해 구독 채널에 있는 클라이언트들에게 Musician정보를 흘려보내겠다고 생각하면 좀 더 쉬울까?

즉 채널을 통해서 정보를 흘려준다고 하면 이해하기 쉬울 수도 있을거 같다. 아닐수도....

사실 이건 스프링 리액터에 대한 공부가 필요하기 때문에 링크로만 대체할 생각이다.

밑에 링크는 아마도 자바 개발자라면 잘 알려진 최범균님의 블로그이다.

[자바캔](https://javacan.tistory.com/entry/Reactor-Start-3-RS-create-stream)


대충 이것은 하나의 이야기로 설명해야 하는게 맞을 거 같다.

1. @GraphQLSubscription으로 설정된 엔드포인트로 클라이언트에서 구독을 하게 될것이다.    
    
2. 이때 구독을 할때는 code값을 넘길 것이고 이 코드값을 musicianSubscribers에 맵핑할 것이다.     
   아마도 내부적으로 sessionId같 유니크한 값을 가질것이다.    
    
3. 위 코드에 보면 createMusician, updateMusician메소드에서 최종 생성 또는 변경이후에 각 메소드에 따른 코드값을 통해서 구독자의 리스트를 가져온다.    
    
4. 그리고 그 구독자들에게 생성 또는 변경된 정보를 흘려준다. 즉 Publish 할것이다.     
     

암튼 @GraphQLSubscription붙이면 된다. 필드명은 따로 설정하지 않으면 메소드명이 필드명이 된다.


## 테스트를 해보자.

일단 [http://localhost:8080/gui](http://localhost:8080/gui)을 4개의 탭으로 각각 열어두자.

그리고 다음과 같이 각 탭에 실행을 한다.


```

subscription {
    statusMusician(code: "new") {
         id
    		 name
         age
    		 genre
         albums {
          title
          releasedYear
        }
    
    }
}



subscription {
    statusMusician(code: "update") {
         id
    		 name
         age
    		 genre
         albums {
          title
          releasedYear
        }
  
    }
}



subscription {
    statusAlbum(code: "new") {
         id
    		 title
         releasedYear
         musician {
          name
          age
          genre
        }
    
    }
}


subscription {
    statusAlbum(code: "update") {
         id
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

![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/graphql-subscription/capture/capture1.png)

그 중에 이미지 하나를 보자면 다음과 같이 화면에 우측에는 무언가 빙글빙글 돌면서 실행되고 있으며 하단에 'Listening ...'이라는 문구가 보일 것이다.

해당 채널로 구독을 하고 있다는 의미를 알 수 있다.

자 그럼 이제 실제 뮤지션을 생성하면 어떻게 되는지 확인해 보자.

요청 쿼리는 Postman으로 테스트했다.

테스트 이미지에서는 Body -> raw -> json을 선택해서 테스트했는데 기존의 브랜치에서 했던 방식으로 해도 상관없다.

원래 html을 통해서 websocket통신을 해볼까 해서 테스트를 준비중에 귀찮아서 방향을 급선회했기 때문이다.


```
{"query": "mutation { createMusician(name:\"Charlie Parker\", genre:\"jazz\") { name genre } }" }

```

다음과 같이 한번 날려보자.

![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/graphql-subscription/capture/capture2.png)

예상대로 뮤지션의 정보가 생성되었다.

자 그럼 웹 ide에서는 무슨일이?

![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/graphql-subscription/capture/capture3.png)

구독한 정보가 우측에 나타났다.

그럼 새로운 뮤지션을 한번 더 넣어볼까?

```
{"query": "mutation { createMusician(name:\"John Coltrne\", genre:\"jazz\") { name genre } }" }

```

![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/graphql-subscription/capture/capture4.png)

오~~~ 제대로 작동한다.

어? 근데 이름을 잘못 넣었다. John Coltrane인데 오타가 났으니 수정을 해야한다.


```
{"query": "mutation { updateMusician(id:2, name:\"John Coltrane\") { name genre } }" }
```

![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/graphql-subscription/capture/capture5.png)

위에서 처럼 업데이트를 했다.

이제는 code값을 'update'로 넘겨서 업데이트에 대한 구독을 요청했던 탭을 한번 보자.


![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/graphql-subscription/capture/capture6.png)

실수로 오타난 이름으로 한번 업데이트해서 이미지처럼 나왔는데 업데이트가 될 때마다 구독하고 있다 서버에서 publish가 발생하면 정보를 받고 있다는 것을 알 수 있다.


이제는 앨범을 한번 새로 등록을 해보자

```
{"query" : "mutation { createAlbum(title:\"With Strign\", releasedYear:\"1950\", musicianId:1) { title } }" }

mutation { 
    createAlbum(title:"Lush Life", releasedYear:"1961", musicianId:2) { 
        title 
        
    } 
    
}

```

![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/graphql-subscription/capture/capture7.png)



이제는 웹 ide를 확인해 보자.

![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/graphql-subscription/capture/capture8.png)

예상대로 잘 되었다.

하지만 어라 음반명이 오타가 났으니 또 업데이트로 제대로 그 부분도 잘되고 있는지 확인해 보자.


With Strign ->에서 With String으로 업데이트를 하자.

```
{"query": "mutation { updateAlbum(id:3, title:\"With String\") { title } }" }
```

![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/graphql-subscription/capture/capture9.png)


gui로 보면 구독 채널을 통해서 수정된 정보를 받았다는 것을 확인할 수 있다.

![실행이미지](https://github.com/basquiat78/graphql-springboot2/blob/graphql-subscription/capture/capture10.png)



## At A Glance

지금까지 GraphQL과 관련된 테스트 코드를 작성하고 테스트를 하면서 생각이 드는 고민은 과연 프론트쪽에서 어떻게 이 부분을 처리해야 하는지에 대한 것이다.

아직 프론트를 작성해서 테스트 한것이 아니고 툴을 이용한 테스트 방식이라서 감이 안온다.

시간이 되면 그 부분도 진행해 볼까 한다.

어째든 Subscription의 작동 방식도 상당히 유용하다.


