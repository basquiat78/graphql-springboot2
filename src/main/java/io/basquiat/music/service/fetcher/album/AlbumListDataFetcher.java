package io.basquiat.music.service.fetcher.album;

import java.util.List;

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
 * album list select query class
 *
 */
@Component
public class AlbumListDataFetcher implements DataFetcher<List<Album>> {

	@Autowired
	private AlbumRepository albumRepository;
	
	@Override
	public List<Album> get(DataFetchingEnvironment environment) {
		return albumRepository.findAll();
	}

}
