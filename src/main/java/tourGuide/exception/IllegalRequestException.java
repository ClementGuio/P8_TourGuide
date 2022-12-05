package tourGuide.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.CONFLICT)
public class IllegalRequestException extends Exception{
	
	public IllegalRequestException(String errorMessage) {
		super(errorMessage);
	}
}
