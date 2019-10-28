package io.basquiat.music.service.fetcher.album;

import graphql.schema.DataFetcher;
import io.basquiat.music.models.Album;
import io.basquiat.music.repo.AlbumRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AlbumDataFetchers {

    private final AlbumRepository albumRepository;

    public AlbumDataFetchers(AlbumRepository albumRepository) {
        this.albumRepository = albumRepository;
    }

    public DataFetcher<Album> getAlbum() {
        return environment -> {
            long id = Long.parseLong(environment.getArgument("id"));
            return albumRepository.findById(id).orElse(null);
        };
    }

    public DataFetcher<List<Album>> getAlbumList() {
        return environment -> albumRepository.findAll();
    }
}
