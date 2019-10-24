package io.basquiat.music.resolver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;

import io.basquiat.music.models.Album;
import io.basquiat.music.models.Musician;
import io.basquiat.music.repo.AlbumRepository;
import io.basquiat.music.repo.MusicianRepository;

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
public class MutationResolver implements GraphQLMutationResolver {

	@Autowired
	private MusicianRepository musicianRepository;

	@Autowired
	private AlbumRepository albumRepository;
	
	/**
	 * create musician
	 * 
	 * @param name
	 * @param genre
	 * @return Musician
	 */
	public Musician createMusician(String name, String genre) {
		Musician musician = Musician.builder()
									.name(name)
									.genre(genre)
									.build();
		return musicianRepository.save(musician);
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
	@Transactional
	public Musician updateMusician(long id, String name, String genre) {
		// id로 뮤지션을 찾아온다.
		Musician musician = musicianRepository.findById(id).orElseGet(Musician::new);
		// dirty checking
		if(!StringUtils.isEmpty(name)) {
			musician.setName(name);
		}

		if(!StringUtils.isEmpty(genre)) {
			musician.setGenre(genre);
		}
		return musician;
	}

	/**
	 * 
	 * delete musician
	 * 
	 * @param id
	 * @return boolean
	 */
	public boolean deleteMusician(long id) {
		musicianRepository.deleteById(id);
		return musicianRepository.existsById(id);
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
	public Album createAlbum(long id, String title, String releasedYear) {
		Musician musician = musicianRepository.findById(id).orElseGet(Musician::new);
		Album album = Album.builder()
						   .musician(musician)
						   .title(title)
						   .releasedYear(releasedYear)
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
	@Transactional
	public Album updateAlbum(long id, String title, String releasedYear) {
		Album album = albumRepository.findById(id).orElseGet(Album::new);
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
