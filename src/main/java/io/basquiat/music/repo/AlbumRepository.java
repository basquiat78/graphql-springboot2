package io.basquiat.music.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.basquiat.music.models.Album;

/**
 * 
 * Album Repository
 * 
 * created by basquiat
 *
 */
@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {

}
