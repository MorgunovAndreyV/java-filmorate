package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.comparator.FilmComparators;
import ru.yandex.practicum.filmorate.exception.FilmServiceException;
import ru.yandex.practicum.filmorate.exception.FilmStorageException;
import ru.yandex.practicum.filmorate.exception.FilmValidationException;
import ru.yandex.practicum.filmorate.exception.RecordNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {
    @Qualifier("UserDbStorage") private final UserStorage userStorage;
    @Qualifier("FilmDbStorage") private final FilmStorage filmStorage;

    public List<Film> getAll() {
        return filmStorage.getAll();
    }

    public List<Genre> getAllGenres() {
        return filmStorage.getAllGenres();
    }

    public List<MPA> getAllMPAs() {
        return filmStorage.getAllMPAs();
    }


    public Film addNew(Film film) throws FilmValidationException, FilmStorageException {
        return filmStorage.addNew(film);
    }

    public Film change(Film film) throws FilmValidationException, FilmStorageException {
        return filmStorage.change(film);
    }

    public Film getFilmById(Long id) throws RecordNotFoundException {
        return filmStorage.getFilmById(id);
    }

    public Genre getGenreById(Long id) throws RecordNotFoundException {
        return filmStorage.getGenreById(id);
    }

    public MPA getMPAById(Long id) throws RecordNotFoundException {
        return filmStorage.getMPAById(id);
    }

    public void likeFilmByUser(Long filmId, Long userId) throws RecordNotFoundException {
        userStorage.getUserById(userId);
        filmStorage.getFilmById(filmId);
        filmStorage.createLikeFilmByUser(filmId, userId);

    }

    public void unlikeFilmByUser(Long filmId, Long userId) throws RecordNotFoundException {
        userStorage.getUserById(userId);
        filmStorage.getFilmById(filmId);
        filmStorage.deleteLikeFilmByUser(filmId, userId);
    }

    public int getFilmLikeCount(Long filmId) throws RecordNotFoundException {
        filmStorage.getFilmById(filmId);

        return filmStorage.getFilmLikeStorageCount(filmId);
    }

    public List<Film> topLikes(Integer count) throws FilmServiceException {
        if (count != null) {
            if (count < 0) {
                throw new FilmServiceException("Количество фильмов не может быть отрицательным");
            }
        }

        List<Film> likedFilms = filmStorage.getAll().stream()
                .sorted(FilmComparators.getComparatorByFilmLikes(this)).collect(Collectors.toList());

        if (count == null || count > likedFilms.size()) {
            return likedFilms;
        }

        return likedFilms.subList(0, count);
    }

}
