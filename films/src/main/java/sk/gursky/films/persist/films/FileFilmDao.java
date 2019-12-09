package sk.gursky.films.persist.films;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
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
	private Map<Long, Person> persons = new ConcurrentHashMap<>();
	
	private ObjectMapper mapper = new ObjectMapper();
	private long maxId;
	private long maxPersonId;
	
	public FileFilmDao() {
		if (!loadCustomFilms()) {
			loadDefaultFilms();
		}
		maxId = films.stream().max((f1, f2) -> Long.compare(f1.getId(), f2.getId())).get().getId();
		maxPersonId = 0;
		for (Film film: films) {
			for (Person person: film.getReziser()) {
				persons.put(person.getId(), person);
				if (maxPersonId < person.getId()) maxPersonId = person.getId();
			}
			for (Postava postava: film.getPostava()) {
				persons.put(postava.getHerec().getId(), postava.getHerec());
				if (maxPersonId < postava.getHerec().getId()) maxPersonId = postava.getHerec().getId();
			}
		}
	}

	@Override
	public List<Film> getAll(Optional<String> orderBy, Optional<Boolean> descending,
			Optional<Integer> indexFrom, Optional<Integer> indexTo, Optional<String> search) {
		List<Film> result = films;
		if (search.isPresent()) {
			result = searchFilm(search.get());
		} 
		if (orderBy.isPresent()) {
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
		}
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
		return new ArrayList<Film>(films);
	}

	@Override
	public synchronized List<Film> getSubinterval(int fromIndex, int toIndex) {
		return films.subList(fromIndex, toIndex);
	}

	@Override
	public Film getById(Long id) {
		List<Film> filmsCopy = films;
		if (id == null)
			return null;
		for (Film film : filmsCopy) {
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
			List<Film> newFilms = new ArrayList<Film>(films);
			newFilms.add(film);
			films = newFilms;
		} else {
			for (int i = 0; i < films.size(); i++) {
				if (films.get(i).getId() == film.getId()) {
					List<Film> newFilms = new ArrayList<Film>(films);
					newFilms.set(i, film);
					films = newFilms;
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
				List<Film> newFilms = new ArrayList<Film>(films);
				newFilms.remove(i);
				films = newFilms;
				saveAll();
				break;
			}
		}
		return film;
	}

	@Override
	public List<FilmSimplified> getSimplifiedFilms() {
		List<Film> filmsCopy = films;
		return filmsCopy.stream().map(film -> new FilmSimplified(film)).collect(Collectors.toList());
	}

	@Override
	public List<Person> searchPerson(String search) {
		if (search.trim().isEmpty())
			return new ArrayList<>();
		return persons.values().stream().filter(p -> {
			String[] fields = {p.getKrstneMeno(), p.getStredneMeno(), p.getPriezvisko()};
			return searchTokensInFields(search, Arrays.asList(fields));
		}).collect(Collectors.toList());
	}

	@Override
	public List<Film> searchFilm(String search) {
		if (search.trim().isEmpty())
			return new ArrayList<>();
		List<Film> filmsCopy = films;
		return filmsCopy.stream().filter(f -> {
			List<String> fields = new ArrayList<>();
			fields.add(f.getNazov());
			fields.add(f.getSlovenskyNazov());
			for (Person p: f.getReziser()) {
				fields.add(p.getKrstneMeno());
				fields.add(p.getStredneMeno());
				fields.add(p.getPriezvisko());
			}
			for (Postava p: f.getPostava()) {
				fields.add(p.getPostava());
				fields.add(p.getHerec().getKrstneMeno());
				fields.add(p.getHerec().getStredneMeno());
				fields.add(p.getHerec().getPriezvisko());
			}
			return searchTokensInFields(search, fields);		
		}).collect(Collectors.toList());
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
		final List<Film> filmsToStore = films;
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

	private boolean searchTokensInFields(String search, List<String> fields) {
		List<String> array = Arrays.asList(search.trim().split(" "));
		List<String> tokens = new ArrayList<>(array.size());
		for (String token : array) {
			tokens.add(token.toLowerCase());
		}
		for (String field : fields) {
			if (field  == null)
				continue;
			for (String token: tokens) {
				if (field.toLowerCase().contains(token)) {
					if (tokens.size() == 1) {
						return true;
					}
					tokens.remove(token);
					break;
				}
			}
		}
		return false;
	}
}
