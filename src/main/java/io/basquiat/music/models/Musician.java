package io.basquiat.music.models;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

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
	private long id;
	
	/** 뮤지션 이 */
	private String name;
	
	/** 뮤지션의 주요 음악 장르 */
	private String genre;
	
	@OneToMany(cascade=CascadeType.ALL, mappedBy="musician")
	private List<Album> albums;

}
