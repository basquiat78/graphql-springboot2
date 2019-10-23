package io.basquiat.music.service.fetcher.album;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.basquiat.music.repo.AlbumRepository;

/**
 * 
 * created by basquiat
 *
 * album delete mutation class
 *
 */
@Component
public class AlbumDeleteMutation implements DataFetcher<Boolean> {

	@Autowired
	private AlbumRepository albumRepository;
	
	@Override
	public Boolean get(DataFetchingEnvironment environment) {
		long id = Long.parseLong(environment.getArgument("id"));
		albumRepository.deleteById(id);
		return albumRepository.existsById(id);
	}

}
