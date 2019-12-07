package sk.gursky.films.persist.users;

public class DaoException extends RuntimeException {
	private static final long serialVersionUID = 7760559606357814973L;

	public DaoException(String text) {
		super(text);
	}
}
