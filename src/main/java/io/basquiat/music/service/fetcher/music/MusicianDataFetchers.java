package io.basquiat.music.service.fetcher.music;

import graphql.schema.DataFetcher;
import io.basquiat.music.models.Musician;
import io.basquiat.music.repo.MusicianRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MusicianDataFetchers {

    private final MusicianRepository musicianRepository;

    public MusicianDataFetchers(MusicianRepository musicianRepository) {
        this.musicianRepository = musicianRepository;
    }

    public DataFetcher<Musician> getMusician() {
        return environment -> {
            long id = Long.parseLong(environment.getArgument("id"));
            return musicianRepository.findById(id).orElse(null);
        };
    }

    public DataFetcher<List<Musician>> getMusicianList() {
        return environment -> musicianRepository.findAll();
    }
}
