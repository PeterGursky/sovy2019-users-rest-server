package sk.gursky.films.rest;

public class ForbiddenActionException extends RuntimeException {

	private static final long serialVersionUID = -548318547800151211L;

	public ForbiddenActionException(String message) {
		super(message);
	}
	
}
