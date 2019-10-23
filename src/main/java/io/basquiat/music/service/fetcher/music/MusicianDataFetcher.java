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
 * musician select query class
 *
 */
@Component
public class MusicianDataFetcher implements DataFetcher<Musician> {

	@Autowired
	private MusicianRepository musicianRepository;
	
	@Override
	public Musician get(DataFetchingEnvironment environment) {
		long id = Long.parseLong(environment.getArgument("id"));
		return musicianRepository.findById(id).orElse(null);
	}

}
