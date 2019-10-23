package io.basquiat.music.service.fetcher.music;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.basquiat.music.models.Musician;
import io.basquiat.music.repo.MusicianRepository;

/**
 * 
 * created by basquiat
 *
 * musician create mutation class
 *
 */
@Component
public class MusicianCreateMutation implements DataFetcher<Musician> {

	@Autowired
	private MusicianRepository musicianRepository;
	
	@Override
	public Musician get(DataFetchingEnvironment environment) {
		Musician musician = Musician.builder()
									.name(environment.getArgument("name"))
									.genre(environment.getArgument("genre"))
									.build();
		return musicianRepository.save(musician);
	}

}
