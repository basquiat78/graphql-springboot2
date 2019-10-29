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
