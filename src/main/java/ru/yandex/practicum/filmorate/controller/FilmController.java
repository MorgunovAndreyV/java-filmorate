package ru.yandex.practicum.filmorate.controller;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.FilmControllerException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private Collection<Film> films = new HashSet<>();
    @Getter
    private final LocalDate lowThreshholdDate = LocalDate.parse("28.12.1895", DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    private final int DESCRIPTION_LENGTH = 200;

    @GetMapping
    public Collection<Film> getAll() {
        return films;
    }

    @PostMapping
    public Film addNew(@RequestBody Film film) throws FilmControllerException {
        filmValidations(film);

        if (films.contains(film)) {
            throw new FilmControllerException("Такой фильм уже добавлен");
        }
        assignNewId(film);
        films.add(film);
        log.info("Новый фильм добавлен успешно. id:" + film.getId());

        return film;
    }

    @PutMapping
    public Film change(@RequestBody Film film) throws FilmControllerException {
        Film filmFromBase = getFilmByIdString(film.getId());

        filmValidations(film);
        filmFromBase.setName(film.getName());
        filmFromBase.setDescription(film.getDescription());
        filmFromBase.setReleaseDate(film.getReleaseDate());
        filmFromBase.setDuration(film.getDuration());

        log.info("Запись фильма изменена успешно. id:" + film.getId());

        return filmFromBase;
    }

    private void filmValidations(Film film) throws FilmControllerException {
        if (film.getName() == null || film.getName().isEmpty()) {
            throw new FilmControllerException("Название фильма не может быть пустым");
        }

        if (film.getDescription() != null) {
            if (film.getDescription().length() >= DESCRIPTION_LENGTH) {
                throw new FilmControllerException("Описание фильма не может быть длиннее 200 символов");
            }

        }

        if (film.getReleaseDate() != null) {
            if (film.getReleaseDate().isBefore(lowThreshholdDate)) {
                throw new FilmControllerException("Дата релиза не может быть раньше " + lowThreshholdDate.toString());
            }

        }

        if (film.getDuration() != null) {
            if (film.getDuration() < 0) {
                throw new FilmControllerException("Продолжительность фильма не может быть отрицательной");
            }

        }

    }

    private void assignNewId(Film film) {
        if (films != null) {
            film.setId((long) (films.size() + 1));
        }

    }

    private Film getFilmByIdString(Long id) throws FilmControllerException {
        Optional<Film> possibleFilm = films.stream()
                .filter(film1 -> film1.getId().equals(id))
                .findFirst();

        if (possibleFilm.isEmpty()) {
            throw new FilmControllerException("Фильм с ID " + id + " не найден.");
        } else return possibleFilm.get();

    }

}
