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
