package io.basquiat.music.service.fetcher.music;

import java.util.List;

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
 * musician list select mutation class
 *
 */
@Component
public class MusicianListDataFetcher implements DataFetcher<List<Musician>> {

	@Autowired
	private MusicianRepository musicianRepository;
	
	@Override
	public List<Musician> get(DataFetchingEnvironment environment) {
		return musicianRepository.findAll();
	}

}
