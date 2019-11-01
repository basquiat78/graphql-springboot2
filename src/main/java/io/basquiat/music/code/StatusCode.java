package io.basquiat.music.code;

import java.util.Arrays;

/**
 * 
 * enum status code
 * 
 * created by basquiat
 *
 */
public enum StatusCode {

	NEW("new"),
	
	UPDATE("update");
	
	/** enum code */
	public String code;
	
	/** String type constructor */
	StatusCode(String code) {
		this.code = code;
	}

	/**
	 * get Enum Object from code
	 * @param code
	 * @return StatusCode
	 */
	public static StatusCode fromString(String code) {
		return Arrays.asList(StatusCode.values())
					 .stream()
					 .filter( statusCode -> statusCode.code.equalsIgnoreCase(code) )
					 .map(statusCode -> statusCode)
					 .findFirst().orElse(null);
    }
	
}
