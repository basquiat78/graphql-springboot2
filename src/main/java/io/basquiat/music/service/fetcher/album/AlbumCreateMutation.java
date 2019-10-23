package io.basquiat.music.service.fetcher.album;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.basquiat.music.models.Album;
import io.basquiat.music.models.Musician;
import io.basquiat.music.repo.AlbumRepository;
import io.basquiat.music.repo.MusicianRepository;

/**
 * 
 * created by basquiat
 *
 * album create muation class
 *
 */
@Component
public class AlbumCreateMutation implements DataFetcher<Album> {

	@Autowired
	private MusicianRepository musicianRepository;
	
	@Autowired
	private AlbumRepository albumRepository;
	
	@Override
	public Album get(DataFetchingEnvironment environment) {
		
		Musician musician = musicianRepository.findById(Long.parseLong(environment.getArgument("id"))).orElse(new Musician());

		Album album = Album.builder()
						   .musician(musician)
						   .title(environment.getArgument("title"))
						   .releasedYear(environment.getArgument("releasedYear"))
						   .build();
		
		return albumRepository.save(album);
	}

}
