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
