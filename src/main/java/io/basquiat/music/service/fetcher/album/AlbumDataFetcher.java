package io.basquiat.music.service.fetcher.album;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.basquiat.music.models.Album;
import io.basquiat.music.repo.AlbumRepository;

/**
 * 
 * created by basquiat
 *
 * album select query class
 *
 */
@Component
public class AlbumDataFetcher implements DataFetcher<Album> {

	@Autowired
	private AlbumRepository albumRepository;
	
	@Override
	public Album get(DataFetchingEnvironment environment) {
		long id = Long.parseLong(environment.getArgument("id"));
		return albumRepository.findById(id).orElse(null);
	}

}
