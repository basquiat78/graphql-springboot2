package io.basquiat.music.service.musician;

import java.util.List;

import javax.transaction.Transactional;

import org.reactivestreams.Publisher;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import io.basquiat.exception.GraphqlNotFoundException;
import io.basquiat.music.code.StatusCode;
import io.basquiat.music.models.Musician;
import io.basquiat.music.repo.MusicianRepository;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.GraphQLSubscription;
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi;
import io.leangen.graphql.spqr.spring.util.ConcurrentMultiMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

@Service("musicianService")
@GraphQLApi
@Transactional
public class MusicianService {

	private final MusicianRepository musicianRepository;

	private final ConcurrentMultiMap<String, FluxSink<Musician>> musicianSubscribers = new ConcurrentMultiMap<>();
	
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
		
		// 새로운 뮤지션을 생성한다.
		Musician newMusician = musicianRepository.save(musician);
		
		// 새로운 뮤지션이 생성되면 subscriber에 그 정보를 알려준다.
		musicianSubscribers.get(StatusCode.NEW.code).forEach(subscriber -> subscriber.next(newMusician));
		return newMusician;

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
		// 뮤지션 정보가 변경되면 subscriber에 그 정보를 알려준다.
		musicianSubscribers.get(StatusCode.UPDATE.code).forEach(subscriber -> subscriber.next(musician));
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
	
	/**
	 * 
	 * 뮤지션이 새로 등록되거나 또는 update될때 해당 정보를 subscription으로 리스닝하고 있는 클라이언트에 해당 정보를 보내주는 역할을 하게 된다.
	 * 
	 * code는 new, update로 StatusCode.java를 참조하자.
	 * 
	 * @param code
	 * @return Publisher<Musician>
	 */
	@GraphQLSubscription
    public Publisher<Musician> statusMusician(String code) {
		return Flux.create(subscriber -> musicianSubscribers.add(code, subscriber.onDispose(() -> musicianSubscribers.remove(code, subscriber))), FluxSink.OverflowStrategy.LATEST);
    }
	
}
