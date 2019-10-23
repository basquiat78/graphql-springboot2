package io.basquiat.music.service.fetcher;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
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
import io.basquiat.music.service.fetcher.album.AlbumCreateMutation;
import io.basquiat.music.service.fetcher.album.AlbumDataFetcher;
import io.basquiat.music.service.fetcher.album.AlbumDeleteMutation;
import io.basquiat.music.service.fetcher.album.AlbumListDataFetcher;
import io.basquiat.music.service.fetcher.album.AlbumUpdateMutation;
import io.basquiat.music.service.fetcher.music.MusicianCreateMutation;
import io.basquiat.music.service.fetcher.music.MusicianDataFetcher;
import io.basquiat.music.service.fetcher.music.MusicianDeleteMutation;
import io.basquiat.music.service.fetcher.music.MusicianListDataFetcher;
import io.basquiat.music.service.fetcher.music.MusicianUpdateMutation;

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
	
	@Autowired
	private MusicianDataFetcher musicianDataFetcher;
	
	@Autowired
	private MusicianListDataFetcher musicianListDataFetcher;
	
	@Autowired
	private MusicianCreateMutation musicianCreateMutation;
	
	@Autowired
	private MusicianUpdateMutation musicianUpdateMutation;
	
	@Autowired
	private MusicianDeleteMutation musicianDeleteMutation;
	
	@Autowired
	private AlbumDataFetcher albumDataFetcher;
	
	@Autowired
	private AlbumListDataFetcher albumListDataFetcher;
	
	@Autowired
	private AlbumCreateMutation albumCreateMutation;
	
	@Autowired
	private AlbumUpdateMutation albumUpdateMutation;
	
	@Autowired
	private AlbumDeleteMutation albumDeleteMutation;
	
	private GraphQL graphQL;
	
	@PostConstruct
	public void setupSchema() throws IOException {
		
		File musicianSchema = musician.getFile();
		File albumSchema = album.getFile();
		
        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(musicianSchema);
        typeRegistry.merge(new SchemaParser().parse(albumSchema));
        
        RuntimeWiring wiring = RuntimeWiring.newRuntimeWiring()
                							.type(newTypeWiring("Query").dataFetcher("musician", musicianDataFetcher)
									                        		    .dataFetcher("musicians", musicianListDataFetcher)
									                        		    .dataFetcher("album", albumDataFetcher)
									                        		    .dataFetcher("albums", albumListDataFetcher)
									                        		    
        									)
                							.type(newTypeWiring("Mutation").dataFetcher("createMusician", musicianCreateMutation)
			                											   .dataFetcher("updateMusician", musicianUpdateMutation)
			                											   .dataFetcher("deleteMusician", musicianDeleteMutation)
			                											   .dataFetcher("createAlbum", albumCreateMutation)
			                											   .dataFetcher("updateAlbum", albumUpdateMutation)
			                											   .dataFetcher("deleteAlbum", albumDeleteMutation)
                									)
            								.build();
        GraphQLSchema graphQLSchema = new SchemaGenerator().makeExecutableSchema(typeRegistry, wiring);
        graphQL = GraphQL.newGraphQL(graphQLSchema).build();
	
	}

	@Bean
	public GraphQL graphQL() {
		return this.graphQL;
	}
	
}
