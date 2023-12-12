package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmStorageException;
import ru.yandex.practicum.filmorate.exception.FilmValidationException;
import ru.yandex.practicum.filmorate.exception.RecordNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private Set<Film> films = new HashSet<>();
    private HashMap<Long, Set<Long>> likeLists = new HashMap<>();

    static final LocalDate LOW_THRESHOLD_DATE =
            LocalDate.parse("28.12.1895", DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    static final int DESCRIPTION_LENGTH = 200;

    @Override
    public Set<Film> getAll() {
        return films;
    }

    @Override
    public Film addNew(Film film) throws FilmValidationException, FilmStorageException {
        filmValidations(film);
        if (films.contains(film)) {
            throw new FilmStorageException("Такой фильм уже добавлен");
        }

        assignNewId(film);
        films.add(film);
        log.info("Новый фильм добавлен успешно. id:" + film.getId());

        return film;
    }

    @Override
    public Film change(Film film) throws FilmValidationException, FilmStorageException {
        Film filmFromBase = getFilmById(film.getId());

        filmValidations(film);
        filmFromBase.setName(film.getName());
        filmFromBase.setDescription(film.getDescription());
        filmFromBase.setReleaseDate(film.getReleaseDate());
        filmFromBase.setDuration(film.getDuration());

        log.info("Запись фильма изменена успешно. id:" + film.getId());
        return filmFromBase;
    }

    @Override
    public HashMap<Long, Set<Long>> getLikeList() {
        return likeLists;
    }

    private void filmValidations(Film film) throws FilmValidationException {
        if (film.getName() == null || film.getName().isEmpty()) {
            throw new FilmValidationException("Название фильма не может быть пустым");
        }

        if (film.getDescription() != null) {
            if (film.getDescription().length() >= DESCRIPTION_LENGTH) {
                throw new FilmValidationException("Описание фильма не может быть длиннее 200 символов");
            }

        }

        if (film.getReleaseDate() != null) {
            if (film.getReleaseDate().isBefore(LOW_THRESHOLD_DATE)) {
                throw new FilmValidationException("Дата релиза не может быть раньше " + LOW_THRESHOLD_DATE.toString());
            }

        }

        if (film.getDuration() != null) {
            if (film.getDuration() < 0) {
                throw new FilmValidationException("Продолжительность фильма не может быть отрицательной");
            }

        }

    }

    private void assignNewId(Film film) {
        if (films != null) {
            film.setId((long) (films.size() + 1));
        }

    }

    public Film getFilmById(Long id) throws RecordNotFoundException {
        Optional<Film> possibleFilm = films.stream()
                .filter(film1 -> film1.getId().equals(id))
                .findFirst();

        if (possibleFilm.isEmpty()) {
            throw new RecordNotFoundException("Фильм с ID " + id + " не найден.");
        }

        return possibleFilm.get();
    }

    public LocalDate getLowThresholdDate() {
        return LOW_THRESHOLD_DATE;
    }

}
