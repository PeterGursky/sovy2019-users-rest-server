package sk.gursky.films.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import sk.gursky.films.persist.users.DaoException;

@ControllerAdvice
public class UsersAdvice {

    @ExceptionHandler(DaoException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    @ResponseBody
    public ApiError handleDaoException(DaoException e) {
        return new ApiError(HttpStatus.NOT_ACCEPTABLE.value(), e.getMessage());
    }

    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ApiError handleNullPointerException(NullPointerException e) {
        return new ApiError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }
	
    @ExceptionHandler(UnauthorizedActionException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public ApiError handleUnauthorizedActionException(UnauthorizedActionException e) {
        return new ApiError(HttpStatus.UNAUTHORIZED.value(), e.getMessage());
    }

    @ExceptionHandler(ForbiddenActionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public ApiError handleForbiddenActionException(ForbiddenActionException e) {
        return new ApiError(HttpStatus.FORBIDDEN.value(), e.getMessage());
    }
}
