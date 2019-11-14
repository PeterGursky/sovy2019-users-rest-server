package sk.gursky.users.rest;

public class UnauthorizedActionException extends RuntimeException {
	private static final long serialVersionUID = 7263168775566582187L;

	public UnauthorizedActionException() {
		super();
	}

	public UnauthorizedActionException(String message) {
		super(message);
	}
}
