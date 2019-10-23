package io.basquiat.music.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.basquiat.music.models.Musician;

/**
 * 
 * Musician Repository
 * 
 * created by basquiat
 *
 */
@Repository
public interface MusicianRepository extends JpaRepository<Musician, Long> {

}
