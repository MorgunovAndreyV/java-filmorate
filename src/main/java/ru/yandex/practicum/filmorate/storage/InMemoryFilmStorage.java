package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmStorageException;
import ru.yandex.practicum.filmorate.exception.FilmValidationException;
import ru.yandex.practicum.filmorate.exception.RecordNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Component("InMemoryFilmStorage")
public class InMemoryFilmStorage implements FilmStorage {
    private List<Film> films = new ArrayList<>();
    private HashMap<Long, Set<Long>> likeLists = new HashMap<>();

    static final LocalDate LOW_THRESHOLD_DATE =
            LocalDate.parse("28.12.1895", DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    static final int DESCRIPTION_LENGTH = 200;

    @Override
    public List<Film> getAll() {
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

    public HashMap<Long, Set<Long>> getLikeLists() {
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

    @Override
    public Set<Long> getLikeListForFilm(Long filmId) {
        if (!getLikeLists().containsKey(filmId)) {
            getLikeLists().put(filmId, new HashSet<>());
        }

        return getLikeLists().get(filmId);

    }

    public LocalDate getLowThresholdDate() {
        return LOW_THRESHOLD_DATE;
    }

    @Override
    public void createLikeFilmByUser(Long filmId, Long userId) {
        if (!getLikeLists().containsKey(filmId)) {
            getLikeLists().put(filmId, new HashSet<>());
        }
        getLikeListForFilm(filmId).add(userId);
    }

    @Override
    public void deleteLikeFilmByUser(Long filmId, Long userId) {
        if (getLikeLists().containsKey(filmId)) {
            getLikeListForFilm(filmId).remove(userId);
        }
    }

    @Override
    public int getFilmLikeStorageCount(Long filmId) {
        if (getLikeLists().containsKey(filmId)) {
            return getLikeListForFilm(filmId).size();
        }

        return 0;
    }

    @Override
    public List<Genre> getAllGenres() {
        return null;
    }

    @Override
    public List<MPA> getAllMPAs() {
        return null;
    }

    @Override
    public Genre getGenreById(Long id) {
        return null;
    }

    @Override
    public MPA getMPAById(Long id) {
        return null;
    }

}
