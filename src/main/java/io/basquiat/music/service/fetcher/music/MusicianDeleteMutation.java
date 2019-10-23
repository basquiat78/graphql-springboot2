package io.basquiat.music.service.fetcher.music;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.basquiat.music.repo.MusicianRepository;

/**
 * 
 * created by basquiat
 *
 * musician delete mutation class
 *
 */
@Component
public class MusicianDeleteMutation implements DataFetcher<Boolean> {

	@Autowired
	private MusicianRepository musicianRepository;
	
	@Override
	public Boolean get(DataFetchingEnvironment environment) {
		long id = Long.parseLong(environment.getArgument("id"));
		musicianRepository.deleteById(id);
		return musicianRepository.existsById(id);
	}

}
