package ru.yandex.practicum.filmorate.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.ExceptionResponse;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleWrongFilmData(final FilmValidationException e) {
        log.error(String.format(e.getMessage()));
        return new ExceptionResponse(
                String.format(e.getMessage())
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleWrongUserData(final UserValidationException e) {
        log.error(String.format(e.getMessage()));
        return new ExceptionResponse(
                String.format(e.getMessage())
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionResponse handleCommonFilmStorageException(final FilmStorageException e) {
        log.error(String.format(e.getMessage()));
        return new ExceptionResponse(
                String.format(e.getMessage())
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionResponse handleCommonFilmServiceException(final FilmServiceException e) {
        log.error(String.format(e.getMessage()));
        return new ExceptionResponse(
                String.format(e.getMessage())
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionResponse handleCommonUserStorageException(final UserStorageException e) {
        log.error(String.format(e.getMessage()));
        return new ExceptionResponse(
                String.format(e.getMessage())
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ExceptionResponse handleRecordNotFoundException(final RecordNotFoundException e) {
        log.error(String.format(e.getMessage()));
        return new ExceptionResponse(
                String.format(e.getMessage())
        );
    }


}
