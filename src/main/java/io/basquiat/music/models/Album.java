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
	private long id;
	
	/** 음반 명 */
	private String title;

	/** 릴리즈된 년도 */
	@Column(name = "released_year")
	private String releasedYear;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "musician_id", nullable = false, updatable = false)
	private Musician musician;
	
}
