package io.basquiat.music.service.fetcher;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;

import io.basquiat.music.service.fetcher.album.AlbumDataFetchers;
import io.basquiat.music.service.fetcher.album.AlbumMutations;
import io.basquiat.music.service.fetcher.music.MusicianDataFetchers;
import io.basquiat.music.service.fetcher.music.MusicianMutations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

/**
 * 
 * created by basquiat
 * 
 * graphQL provider
 * classPath의 정의된 스키마를 읽어와 해당 타입 (Query, Mutation)에 정의된 로직을 매핑하는 클래스
 * 
 * @see classPath: musician.graphqls
 * @see classPath: album.graphqls
 *
 */
@Component
public class GraphQLProvider {

	@Value("classpath:musician.graphqls")
	Resource musician;
	
	@Value("classpath:album.graphqls")
	Resource album;

	private final AlbumDataFetchers albumDataFetchers;
	private final MusicianDataFetchers musicianDataFetchers;
	private final AlbumMutations albumMutations;
	private final MusicianMutations musicianMutations;

	private GraphQL graphQL;

	public GraphQLProvider(AlbumDataFetchers albumDataFetchers, MusicianDataFetchers musicianDataFetchers,
						   AlbumMutations albumMutations, MusicianMutations musicianMutations) {
		this.albumDataFetchers = albumDataFetchers;
		this.musicianDataFetchers = musicianDataFetchers;
		this.albumMutations = albumMutations;
		this.musicianMutations = musicianMutations;
	}

	@PostConstruct
	public void setupSchema() throws IOException {
		
		File musicianSchema = musician.getFile();
		File albumSchema = album.getFile();
		
        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(musicianSchema);
        typeRegistry.merge(new SchemaParser().parse(albumSchema));
        
        RuntimeWiring wiring = customRuntimeWiring();
        GraphQLSchema graphQLSchema = new SchemaGenerator().makeExecutableSchema(typeRegistry, wiring);
        graphQL = GraphQL.newGraphQL(graphQLSchema).build();
	
	}

	private RuntimeWiring customRuntimeWiring() {
		return RuntimeWiring.newRuntimeWiring()
				.type(newTypeWiring("Query")
						.dataFetcher("musician", this.musicianDataFetchers.getMusician())
						.dataFetcher("musicians", this.musicianDataFetchers.getMusicianList())
						.dataFetcher("album", this.albumDataFetchers.getAlbum())
						.dataFetcher("albums", this.albumDataFetchers.getAlbumList())

				)
				.type(newTypeWiring("Mutation")
						.dataFetcher("createMusician", this.musicianMutations.createMusician())
						.dataFetcher("updateMusician", this.musicianMutations.updateMusician())
						.dataFetcher("deleteMusician", this.musicianMutations.deleteMusician())
						.dataFetcher("createAlbum", this.albumMutations.createAlbum())
						.dataFetcher("updateAlbum", this.albumMutations.updateAlbum())
						.dataFetcher("deleteAlbum", this.albumMutations.deleteAlbum())
				)
				.build();
	}

	@Bean
	public GraphQL graphQL() {
		return this.graphQL;
	}
	
}
