package io.basquiat.music.service.fetcher.album;

import graphql.schema.DataFetcher;
import io.basquiat.music.models.Album;
import io.basquiat.music.models.Musician;
import io.basquiat.music.repo.AlbumRepository;
import io.basquiat.music.repo.MusicianRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Component
public class AlbumMutations {

    private final AlbumRepository albumRepository;
    private final MusicianRepository musicianRepository;

    public AlbumMutations(AlbumRepository albumRepository, MusicianRepository musicianRepository) {
        this.albumRepository = albumRepository;
        this.musicianRepository = musicianRepository;
    }

    public DataFetcher<Album> createAlbum() {
        return environment -> {
            Musician musician = musicianRepository
                    .findById(Long.parseLong(environment.getArgument("id")))
                    .orElse(new Musician());

            Album album = Album.builder()
                    .musician(musician)
                    .title(environment.getArgument("title"))
                    .releasedYear(environment.getArgument("releasedYear"))
                    .build();

            return albumRepository.save(album);
        };
    }

    @Transactional
    public DataFetcher<Album> updateAlbum() {
        return environment -> {
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
        };
    }

    public DataFetcher<Boolean> deleteAlbum() {
        return environment -> {
            long id = Long.parseLong(environment.getArgument("id"));
            albumRepository.deleteById(id);
            return albumRepository.existsById(id);
        };
    }
}
