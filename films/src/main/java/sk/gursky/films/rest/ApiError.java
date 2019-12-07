package sk.gursky.films.rest;

public class ApiError {

    private int status;
    private String errorMessage;

    public ApiError(int status, String errorMessage) {
        this.status = status;
        this.errorMessage = errorMessage;
    }

    public int getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}