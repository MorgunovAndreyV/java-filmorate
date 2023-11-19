package ru.yandex.practicum.filmorate.exception;

public class FilmServiceException extends RuntimeException{
    public FilmServiceException(String message) {
        super(message);
    }
}
