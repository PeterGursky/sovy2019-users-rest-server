package sk.gursky.films.persist.films;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import sk.gursky.films.persist.users.DaoException;

public class FileFilmDao implements FilmDao {
	private static Logger logger = LoggerFactory.getLogger(FileFilmDao.class);

	private static final File CUSTOM_FILE = new File("filmy.json");
	private List<Film> films = new ArrayList<>();
	private ObjectMapper mapper = new ObjectMapper();
	private long maxId;

	public FileFilmDao() {
		if (!loadCustomFilms()) {
			loadDefaultFilms();
		}
		maxId = films.stream().max((f1, f2) -> Long.compare(f1.getId(), f2.getId())).get().getId();
	}

	@Override
	public synchronized List<Film> getAll(Optional<String> orderBy, Optional<Boolean> descending,
			Optional<Integer> indexFrom, Optional<Integer> indexTo) {
		List<Film> result = films;
		Comparator<Film> comparator = (f1, f2) -> Long.compare(f1.getId(), f2.getId());
		if (orderBy.isPresent()) {
			switch (orderBy.get()) {
			case "nazov":
				comparator = (f1, f2) -> f1.getNazov().compareTo(f2.getNazov());
			case "slovenskyNazov":
				comparator = (f1, f2) -> Collator.getInstance(new Locale("sk")).compare(f1.getSlovenskyNazov(),
						f2.getSlovenskyNazov());
			case "rok":
				comparator = (f1, f2) -> Integer.compare(f1.getRok(), f2.getRok());
			default:
				if (orderBy.get().startsWith("poradieVRebricku.")) {
					String rebricek = orderBy.get().substring("poradieVRebricku.".length());
					result = result.stream().filter(film -> film.getPoradieVRebricku().containsKey(rebricek))
							.collect(Collectors.toList());
					comparator = (f1, f2) -> Integer.compare(f1.getPoradieVRebricku().get(rebricek),
							f2.getPoradieVRebricku().get(rebricek));
				}
				break;
			}
		}
		if (descending.isPresent() && descending.get() == true) {
			comparator = Collections.reverseOrder(comparator);
		}
		Collections.sort(result, comparator);
		int iFrom = 0;
		if (indexFrom.isPresent()) {
			iFrom = Math.max(0, indexFrom.get());
			if (iFrom >= result.size())
				return new ArrayList<Film>();
		}
		int iTo = result.size();
		if (indexTo.isPresent()) {
			iTo = Math.min(result.size(), indexTo.get());
			if (iTo <= iFrom)
				return new ArrayList<Film>();
		}
		return result.subList(iFrom, iTo);
	}

	@Override
	public synchronized List<Film> getAll() {
		return films;
	}

	@Override
	public synchronized List<Film> getSubinterval(int fromIndex, int toIndex) {
		return films.subList(fromIndex, toIndex);
	}

	@Override
	public synchronized Film getById(Long id) {
		if (id == null)
			return null;
		for (Film film : films) {
			if (film.getId() == id) {
				return film;
			}
		}
		return null;
	}

	@Override
	public synchronized Film save(Film film) {
		if (film.getId() == null) {
			film.setId(++maxId);
			films.add(film);
		} else {
			for (int i = 0; i < films.size(); i++) {
				if (films.get(i).getId() == film.getId()) {
					films.set(i, film);
					saveAll();
					return film;
				}
			}
		}
		saveAll();
		return film;
	}

	@Override
	public synchronized Film delete(long id) {
		Film film = null;
		for (int i = 0; i < films.size(); i++) {
			if (films.get(i).getId() == id) {
				film = films.remove(i);
				saveAll();
				break;
			}
		}
		return film;
	}

	private boolean loadCustomFilms() {
		if (!CUSTOM_FILE.exists())
			return false;
		try (FileInputStream fis = new FileInputStream(CUSTOM_FILE)) {
			loadFilms(fis);
		} catch (IllegalArgumentException | IOException e) {
			logger.warn("Custom file filmy.json present, but cannot be loaded. Malformed?");
			return false;
		}
		return true;
	}

	private void loadDefaultFilms() {
		try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("filmy.json")) {
			loadFilms(inputStream);
		} catch (IllegalArgumentException | IOException e) {
			e.printStackTrace();
		}
	}

	private void loadFilms(InputStream inputStream) throws IOException, IllegalArgumentException {
		JsonNode list = mapper.readTree(inputStream);
		if (list != null && list.isArray()) {
			Iterator<JsonNode> elementsIt = list.elements();
			while (elementsIt.hasNext()) {
				films.add(mapper.convertValue(elementsIt.next(), Film.class)); // throws IllegalArgumentException
			}
		}
	}

	private void saveAll() {
		final List<Film> filmsToStore = new ArrayList<>(films);
		new Thread(() -> {
			synchronized (CUSTOM_FILE) {
				try {
					mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
					mapper.writeValue(CUSTOM_FILE, filmsToStore);
				} catch (IOException e) {
					throw new DaoException("cannot save file films.json");
				}
			}
		}).start();
	}

	@Override
	public synchronized List<FilmSimplified> getSimplifiedFilms() {
		return films.stream().map(film -> new FilmSimplified(film)).collect(Collectors.toList());
	}

}
