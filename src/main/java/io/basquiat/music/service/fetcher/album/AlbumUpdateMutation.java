package io.basquiat.music.service.fetcher.album;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.basquiat.music.models.Album;
import io.basquiat.music.repo.AlbumRepository;

/**
 * 
 * created by basquiat
 *
 * album update mutation class
 *
 */
@Component
public class AlbumUpdateMutation implements DataFetcher<Album> {

	@Autowired
	private AlbumRepository albumRepository;
	
	@Override
	@Transactional
	public Album get(DataFetchingEnvironment environment) {
		
		Album album = albumRepository.findById(Long.parseLong(environment.getArgument("id"))).orElse(new Album());
		
		String title = environment.getArgument("title");
		String releasedYear = environment.getArgument("releasedYear");
		
		// dirty checking
		if(!StringUtils.isEmpty(title)) {
			album.setTitle(title);
		}
		
		if(!StringUtils.isEmpty(releasedYear)) {
			album.setReleasedYear(releasedYear);
		}
		
		return album;
		
	}

}
