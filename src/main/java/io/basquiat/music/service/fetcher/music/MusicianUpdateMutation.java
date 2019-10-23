package io.basquiat.music.service.fetcher.music;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.basquiat.music.models.Musician;
import io.basquiat.music.repo.MusicianRepository;

/**
 * 
 * created by basquiat
 *
 * musician update mutation class
 *
 */
@Component
public class MusicianUpdateMutation implements DataFetcher<Musician> {

	@Autowired
	private MusicianRepository musicianRepository;
	
	@Override
	@Transactional
	public Musician get(DataFetchingEnvironment environment) {
		
		// id로 뮤지션을 찾아온다.
		Musician musician = musicianRepository.findById(Long.parseLong(environment.getArgument("id"))).orElse(new Musician());
		
		String name = environment.getArgument("name");
		String genre = environment.getArgument("genre");

		// dirty checking
		if(!StringUtils.isEmpty(name)) {
			musician.setName(name);
		}
		
		if(!StringUtils.isEmpty(genre)) {
			musician.setGenre(genre);
		}
		
		return musician;
	}

}
