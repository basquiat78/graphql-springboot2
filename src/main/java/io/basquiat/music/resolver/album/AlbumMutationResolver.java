package io.basquiat.music.resolver.album;

import javax.transaction.Transactional;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;

import io.basquiat.exception.GraphqlNotFoundException;
import io.basquiat.music.models.Album;
import io.basquiat.music.repo.AlbumRepository;

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
@Transactional
public class AlbumMutationResolver implements GraphQLMutationResolver {

	private final AlbumRepository albumRepository;

	/**
	 * constructor
	 * 	
	 * @param albumRepository
	 */
	public AlbumMutationResolver(AlbumRepository albumRepository) {
		this.albumRepository = albumRepository;
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
	public Album createAlbum(String title, String releasedYear, long musicianId) {
		Album album = Album.builder()
						   .title(title)
						   .releasedYear(releasedYear)
						   .musicianId(musicianId)
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
	public boolean deleteAlbum(long id) {
		albumRepository.deleteById(id);
		return albumRepository.existsById(id);
	}
	
}
