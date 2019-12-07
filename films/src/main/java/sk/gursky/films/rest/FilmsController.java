package sk.gursky.films.rest;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import sk.gursky.films.persist.DaoFactory;
import sk.gursky.films.persist.films.Film;
import sk.gursky.films.persist.films.FilmDao;
import sk.gursky.films.persist.films.FilmSimplified;
import sk.gursky.films.persist.users.DaoException;
import sk.gursky.films.persist.users.User;
import sk.gursky.films.persist.users.UserDao;

@RestController
public class FilmsController {

	private FilmDao filmDao = DaoFactory.INSTANCE.getFilmDao();
	private UserDao userDao = DaoFactory.INSTANCE.getUserDao();

	@GetMapping("/films")
	public ResponseEntity<?> getFilms(
			@RequestHeader(value = "X-Auth-Token", required = false) Optional<String> token,
			@RequestParam(value = "orderBy", required = false) Optional<String> orderBy,
			@RequestParam(value = "descending", required = false) Optional<Boolean> descending,
			@RequestParam(value = "indexFrom", required = false) Optional<Integer> indexFrom,
			@RequestParam(value = "indexTo", required = false) Optional<Integer> indexTo) {
		try {
			if (token.isPresent()) {
				User user = userDao.authorizeByToken(token.get());
				if (user != null) {
					if (user.hasPermission("show_films")) {
						return new ResponseEntity<List<Film>>(filmDao.getAll(orderBy,descending,indexFrom,indexTo), HttpStatus.OK);
					} else {
						throw new ForbiddenActionException("show_films permission needed");				    				
					}
				} else {
					throw new UnauthorizedActionException("unknown token");
				}
			}
			return new ResponseEntity<List<FilmSimplified>>(filmDao.getSimplifiedFilms(), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<ApiError>(new ApiError(HttpStatus.BAD_REQUEST.value(), e.getMessage()), HttpStatus.BAD_REQUEST);
		}
	}
	
    @GetMapping("/films/{id}")
    public Film getUserById(@RequestHeader(value = "X-Auth-Token", required = false) Optional<String> token, @PathVariable Long id) {
    	if (! token.isPresent())
        	throw new UnauthorizedActionException("no token specified");
    	User user = userDao.authorizeByToken(token.get());
    	if (user != null)
    		if (user.hasPermission("show_films"))
    			return filmDao.getById(id);
    		else
    			throw new ForbiddenActionException("show_films permission needed");
    	throw new UnauthorizedActionException("unknown token");
    }
    
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/films")
    public Film saveFilm(@RequestHeader(value = "X-Auth-Token", required = false) Optional<String> token, @RequestBody Film film) {
    	if (! token.isPresent())
        	throw new UnauthorizedActionException("no token specified");
    	User u = userDao.authorizeByToken(token.get());
    	if (u != null)
    		if (u.hasPermission("manage_films"))
				try {
					return filmDao.save(film);
				} catch (DaoException e) {
					throw new ForbiddenActionException(e.getMessage());
				}
			else
    			throw new ForbiddenActionException("manage_films permission needed");
    	throw new UnauthorizedActionException("unknown token");
    }

    @DeleteMapping(value = "/films/{id}")
    public void deleteFilm(@RequestHeader(value = "X-Auth-Token", required = false) Optional<String> token, @PathVariable long id) {
    	if (! token.isPresent())
        	throw new UnauthorizedActionException("no token specified");
    	User u = userDao.authorizeByToken(token.get());
    	if (u != null)
    		if (u.hasPermission("manage_films"))
				try {
					filmDao.delete(id);
				} catch (DaoException e) {
					throw new ForbiddenActionException(e.getMessage());
				}
			else
    			throw new ForbiddenActionException("manage_films permission needed");
    	throw new UnauthorizedActionException("unknown token");
    }
}
