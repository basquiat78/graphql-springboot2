package io.basquiat.music.service.album;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import io.basquiat.exception.GraphqlNotFoundException;
import io.basquiat.music.models.Album;
import io.basquiat.music.models.Musician;
import io.basquiat.music.repo.AlbumRepository;
import io.basquiat.music.repo.MusicianRepository;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi;

@Service("albumService")
@GraphQLApi
@Transactional
public class AlbumService {

	private final MusicianRepository musicianRepository;

	private final AlbumRepository albumRepository;

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
		
		return albumRepository.save(album);
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
	
}
