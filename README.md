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
