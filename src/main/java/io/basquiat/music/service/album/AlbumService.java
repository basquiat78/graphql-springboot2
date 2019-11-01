package io.basquiat.music.service.album;

import java.util.List;

import javax.transaction.Transactional;

import org.reactivestreams.Publisher;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import io.basquiat.exception.GraphqlNotFoundException;
import io.basquiat.music.code.StatusCode;
import io.basquiat.music.models.Album;
import io.basquiat.music.models.Musician;
import io.basquiat.music.repo.AlbumRepository;
import io.basquiat.music.repo.MusicianRepository;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.GraphQLSubscription;
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi;
import io.leangen.graphql.spqr.spring.util.ConcurrentMultiMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

@Service("albumService")
@GraphQLApi
@Transactional
public class AlbumService {

	private final MusicianRepository musicianRepository;

	private final AlbumRepository albumRepository;

	private final ConcurrentMultiMap<String, FluxSink<Album>> albumSubscribers = new ConcurrentMultiMap<>();
	
	public AlbumService(MusicianRepository musicianRepository, AlbumRepository albumRepository) {
		this.musicianRepository = musicianRepository;
		this.albumRepository = albumRepository;
	}
	
	/** Query Type */
	
	
	/**
	 * get album by id
	 * 
	 * @param id
	 * @return Album
	 */
	@GraphQLQuery(name = "album")
	public Album getAlbumById(long id) {
		return albumRepository.findById(id).orElseGet(Album::new);
	}
	
	/**
	 * get album list
	 * 
	 * @return List<Album>
	 */
	@GraphQLQuery(name = "albums")
	public List<Album> getAlbumList() {
		return albumRepository.findAll();
	}
	
	
	/** Mutation Type */
	
	/**
	 * 
	 * save album info
	 * 
	 * @param title
	 * @param releasedYear
	 * @param musicianId
	 * @return Album
	 */
	@GraphQLMutation(name = "createAlbum")
	public Album createAlbum(String title, String releasedYear, long musicianId) {
		
		Musician musician = musicianRepository.findById(musicianId).orElseGet(Musician::new);
		
		if(musician.getName() == null) {
			throw new GraphqlNotFoundException("not found musician by id, it doesn't create album", musicianId);
		}
		
		Album album = Album.builder()
						   .musician(musician)
						   .title(title)
						   .releasedYear(releasedYear)
						   .build();
		
		Album newAlbum = albumRepository.save(album);
		// 새로운 앨범이 생성되면 subscriber에 그 정보를 알려준다.
		albumSubscribers.get(StatusCode.NEW.code).forEach(subscriber -> subscriber.next(newAlbum));
		return newAlbum;
	}
	
	/**
	 * 
	 * update album
	 * 
	 * @param id
	 * @param title
	 * @param releasedYear
	 * @return Album
	 */
	@GraphQLMutation(name = "updateAlbum")
	public Album updateAlbum(long id, String title, String releasedYear) {
		Album album = albumRepository.findById(id).orElseGet(Album::new);
		
		if(album.getTitle() == null) {
			throw new GraphqlNotFoundException("not found album by id, it doesn't update", id);
		}
		
		// dirty checking
		if(!StringUtils.isEmpty(title)) {
			album.setTitle(title);
		}
		
		if(!StringUtils.isEmpty(releasedYear)) {
			album.setReleasedYear(releasedYear);
		}
		
		// 앨범 정보가 변경되면 subscriber에 그 정보를 알려준다.
		albumSubscribers.get(StatusCode.UPDATE.code).forEach(subscriber -> subscriber.next(album));
		return album;
	}
	
	/**
	 * delete album
	 * 
	 * @param id
	 * @return boolean
	 */
	@GraphQLMutation(name = "deleteAlbum")
	public boolean deleteAlbum(long id) {
		albumRepository.deleteById(id);
		return albumRepository.existsById(id);
	}
	
	/**
	 * 
	 * 앨범이 새로 등록되거나 또는 update될때 해당 정보를 subscription으로 리스닝하고 있는 클라이언트에 해당 정보를 보내주는 역할을 하게 된다.
	 * 
	 * code는 new, update로 StatusCode.java를 참조하자.
	 * 
	 * @param code
	 * @return Publisher<Album>
	 */
	@GraphQLSubscription
    public Publisher<Album> statusAlbum(String code) {
		return Flux.create(subscriber -> albumSubscribers.add(code, subscriber.onDispose(() -> albumSubscribers.remove(code, subscriber))), FluxSink.OverflowStrategy.LATEST);
    }
	
}
