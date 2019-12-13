package sk.gursky.films.persist.films;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

public class MysqlFilmDao implements FilmDao {

	private JdbcTemplate jdbcTemplate;
	
	public MysqlFilmDao(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate= jdbcTemplate;
	}
	
	@Override
	public List<Film> getAll() {
		String sql = "SELECT film.id, film.nazov, film.slovenskyNazov, film.rok, rebricek.nazov AS rnazov, poradie.poradie " + 
				"FROM film " + 
				"JOIN poradie ON film.id = poradie.idFilm " + 
				"JOIN rebricek ON poradie.idRebricek = rebricek.id " + 
				"ORDER BY film.id";
		final Map<Long, Film> map = new HashMap<Long, Film>();
		final List<Film> films = jdbcTemplate.query(sql, (ResultSetExtractor<List<Film>>) rs -> {
			List<Film> result = new ArrayList<>();
			Film film = null;
			while(rs.next()) {
				long id = rs.getLong("id");
				if (film == null || film.getId() != id) {
					film = new Film();
					film.setId(id);
					film.setNazov(rs.getString("nazov"));
					film.setSlovenskyNazov(rs.getString("slovenskyNazov"));
					film.setRok(rs.getInt("rok"));
					result.add(film);
					map.put(id, film);
				}
				film.getPoradieVRebricku().put(rs.getString("rnazov"), rs.getInt("poradie"));
			}
			return result;
		});
		
		String sql2 = "SELECT film.id, reziser.id AS rid, reziser.krstneMeno, reziser.stredneMeno, reziser.priezvisko " + 
				"FROM film " + 
				"JOIN rezia ON film.id = rezia.idFilm " + 
				"JOIN osoba AS reziser ON rezia.idReziser = reziser.id " + 
				"ORDER BY film.id";
		jdbcTemplate.query(sql2, (ResultSetExtractor<Void>) rs -> {
			while(rs.next()) {
				long id = rs.getLong("id");
				Film film = map.get(id);
				Person reziser = new Person();
				reziser.setId(rs.getLong("rid"));
				reziser.setKrstneMeno(rs.getString("krstneMeno"));
				reziser.setStredneMeno(rs.getString("stredneMeno"));
				reziser.setPriezvisko(rs.getString("priezvisko"));
				film.getReziser().add(reziser);
			}
			return null;
		});
		
		String sql3 = "SELECT film.id, postava.postava, dolezitost.popis as dolezitost, herec.id AS hid, "
				+ "herec.krstneMeno, herec.stredneMeno, herec.priezvisko " + 
				"FROM film " + 
				"JOIN postava ON film.id = postava.idFilm " + 
				"JOIN dolezitost ON postava.idDolezitost = dolezitost.id " + 
				"LEFT JOIN osoba AS herec ON postava.idHerec = herec.id " + 
				"ORDER BY film.id, postava.id";
		jdbcTemplate.query(sql3, (ResultSetExtractor<Void>) rs -> {
			while(rs.next()) {
				long id = rs.getLong("id");
				Film film = map.get(id);
				Postava postava = new Postava();
				postava.setPostava(rs.getString("postava"));
				postava.setDolezitost(rs.getString("dolezitost"));
				Person herec = new Person();
				herec.setId(rs.getLong("hid"));
				herec.setKrstneMeno(rs.getString("krstneMeno"));
				herec.setStredneMeno(rs.getString("stredneMeno"));
				herec.setPriezvisko(rs.getString("priezvisko"));
				postava.setHerec(herec);
				film.getPostava().add(postava);
			}
			return null;
		});
		return films;
	}

	@Override
	public Film delete(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Film save(Film film) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Film getById(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Film> getSubinterval(int fromIndex, int toIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FilmsResponse getAll(Optional<String> orderBy, Optional<Boolean> descending, Optional<Integer> indexFrom,
			Optional<Integer> indexTo, Optional<String> search) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Person> searchPerson(String search) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FilmsSimplifiedResponse getSimplifiedFilms(Optional<String> orderBy, Optional<Boolean> descending,
			Optional<Integer> indexFrom, Optional<Integer> indexTo, Optional<String> search) {
		// TODO Auto-generated method stub
		return null;
	}
}
