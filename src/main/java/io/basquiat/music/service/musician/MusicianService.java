package io.basquiat.music.service.musician;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import io.basquiat.exception.GraphqlNotFoundException;
import io.basquiat.music.models.Musician;
import io.basquiat.music.repo.MusicianRepository;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi;

@Service("musicianService")
@GraphQLApi
@Transactional
public class MusicianService {

	private final MusicianRepository musicianRepository;

	public MusicianService(MusicianRepository musicianRepository) {
		this.musicianRepository = musicianRepository;
	}
	
	/** Query Type */
	
	/**
	 * get musician by id
	 * 
	 * @param id
	 * @return Musician
	 */
	@GraphQLQuery(name = "musician")
	public Musician musician(long id) {
		return musicianRepository.findById(id).orElseGet(Musician::new);
	}
	
	/**
	 * 
	 * get musician list
	 * 
	 * @return List<Musician>
	 */
	@GraphQLQuery(name = "musicians")
	public List<Musician> musicians() {
		return musicianRepository.findAll();
	}
	
	
	/** Mutation Type */
	
	/**
	 * create musician
	 * 
	 * @param name
	 * @param genre
	 * @return Musician
	 */
	@GraphQLMutation(name = "createMusician")
	public Musician createMusician(String name, String genre) {
		Musician musician = Musician.builder()
									.name(name)
									.genre(genre)
									.build();
		return musicianRepository.save(musician);
	}
	
	/**
	 * 
	 * update musician
	 * 
	 * @param id
	 * @param name
	 * @param genre
	 * @return Musician
	 */
	@GraphQLMutation(name = "updateMusician")
	public Musician updateMusician(long id, String name, String genre) {
		// id로 뮤지션을 찾아온다.
		Musician musician = musicianRepository.findById(id).orElseGet(Musician::new);
		if(musician.getName() == null) {
			throw new GraphqlNotFoundException("not found musician by id, it doesn't update musician", id);
		}
		// dirty checking
		if(!StringUtils.isEmpty(name)) {
			musician.setName(name);
		}

		if(!StringUtils.isEmpty(genre)) {
			musician.setGenre(genre);
		}
		return musician;
	}

	/**
	 * 
	 * delete musician
	 * 
	 * @param id
	 * @return boolean
	 */
	@GraphQLMutation(name = "deleteMusician")
	public boolean deleteMusician(long id) {
		musicianRepository.deleteById(id);
		return musicianRepository.existsById(id);
	}
	
}
