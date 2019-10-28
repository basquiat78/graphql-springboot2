package io.basquiat.music.service.fetcher.music;

import graphql.schema.DataFetcher;
import io.basquiat.music.models.Musician;
import io.basquiat.music.repo.MusicianRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Component
public class MusicianMutations {

    private final MusicianRepository musicianRepository;

    public MusicianMutations(MusicianRepository musicianRepository) {
        this.musicianRepository = musicianRepository;
    }

    public DataFetcher<Musician> createMusician() {
        return environment -> {
            Musician musician = Musician.builder()
                    .name(environment.getArgument("name"))
                    .genre(environment.getArgument("genre"))
                    .build();
            return musicianRepository.save(musician);
        };
    }

    @Transactional
    public DataFetcher<Musician> updateMusician() {
        return environment -> {
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
        };
    }

    public DataFetcher<Boolean> deleteMusician() {
        return environment -> {
            long id = Long.parseLong(environment.getArgument("id"));
            musicianRepository.deleteById(id);
            return musicianRepository.existsById(id);
        };
    }
}
