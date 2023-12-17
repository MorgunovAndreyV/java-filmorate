package ru.yandex.practicum.filmorate.model;


import lombok.Getter;

@Getter
public class ExceptionResponse {
    private final String error;

    public ExceptionResponse(String error) {
        this.error = error;
    }

}
