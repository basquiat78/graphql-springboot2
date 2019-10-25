package io.basquiat.music.resolver.album;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;

import io.basquiat.music.models.Album;
import io.basquiat.music.repo.AlbumRepository;

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
@Transactional
public class AlbumQueryResolver implements GraphQLQueryResolver {

	@Autowired
	private AlbumRepository albumRepository;
	
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
