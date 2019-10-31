package io.basquiat.music.models;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import io.leangen.graphql.annotations.GraphQLQuery;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * 
 * Musician Entity
 * 
 * created by basquiat
 *
 */
@Builder
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "musician")
public class Musician {

	/** 뮤지션 유니크 아이디 */
	@Id
	@GeneratedValue(strategy= GenerationType.AUTO)
	@GraphQLQuery(name = "id")
	private long id;
	
	/** 뮤지션 이름 */
	@GraphQLQuery(name = "name")
	private String name;
	
	/** 뮤지션 나이 */
	@GraphQLQuery(name = "age")
	private int age;
	
	/** 뮤지션의 주요 음악 장르 */
	@GraphQLQuery(name = "genre")
	private String genre;

	/** 뮤지션의 앨범 리스트 */
	@OneToMany(fetch = FetchType.LAZY, cascade=CascadeType.ALL, mappedBy="musician")
	@GraphQLQuery(name = "albums")
	private List<Album> albums;
	
}
