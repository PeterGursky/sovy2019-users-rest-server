package sk.gursky.films.persist.films;

public class FilmSimplified {
	private Long id;
	private String nazov;
	private int rok;
	
	public FilmSimplified(Film film) {
		id = film.getId();
		nazov = film.getNazov();
		rok = film.getRok();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNazov() {
		return nazov;
	}

	public void setNazov(String nazov) {
		this.nazov = nazov;
	}

	public int getRok() {
		return rok;
	}

	public void setRok(int rok) {
		this.rok = rok;
	}
	
}
