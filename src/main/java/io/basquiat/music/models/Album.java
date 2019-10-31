package io.basquiat.music.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import io.leangen.graphql.annotations.GraphQLQuery;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * 
 * Album Entity
 * 
 * created by basquiat
 *
 */
@Builder
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "album")
public class Album {

	/** 앨범 아이디 */
	@Id
	@GeneratedValue(strategy= GenerationType.AUTO)
	@GraphQLQuery(name = "id")
	private long id;
	
	/** 음반 명 */
	@GraphQLQuery(name = "title")
	private String title;

	/** 릴리즈된 년도 */
	@Column(name = "released_year")
	@GraphQLQuery(name = "releasedYear")
	private String releasedYear;

	/** 해당 앨범의 뮤지션 정보 */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "musician_id", nullable = false, updatable = false)
	@GraphQLQuery(name = "musician")
	private Musician musician;
	
}
