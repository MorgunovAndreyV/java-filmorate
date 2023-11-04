package ru.yandex.practicum.filmorate.exception;

public class UserControllerException extends RuntimeException {
    public UserControllerException(String message) {
        super(message);
    }
}
