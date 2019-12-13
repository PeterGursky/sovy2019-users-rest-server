package sk.gursky.films.persist.films;

import java.util.List;
import java.util.Optional;

public interface FilmDao {
	List<Film> getAll();

	Film delete(long id);

	Film save(Film film);

	Film getById(Long id);

	List<Film> getSubinterval(int fromIndex, int toIndex);

	FilmsResponse getAll(Optional<String> orderBy, Optional<Boolean> descending, Optional<Integer> indexFrom,
			Optional<Integer> indexTo, Optional<String> search);
	
	FilmsSimplifiedResponse getSimplifiedFilms(Optional<String> orderBy, Optional<Boolean> descending, Optional<Integer> indexFrom,
			Optional<Integer> indexTo, Optional<String> search);
	
	List<Person> searchPerson(String search);
}
