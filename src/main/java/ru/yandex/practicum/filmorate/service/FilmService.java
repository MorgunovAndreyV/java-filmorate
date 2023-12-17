package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmServiceException;
import ru.yandex.practicum.filmorate.exception.FilmStorageException;
import ru.yandex.practicum.filmorate.exception.FilmValidationException;
import ru.yandex.practicum.filmorate.exception.RecordNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final Comparator<Film> filmComparatorByLikes = new FilmComparatorByLikes();

    @Autowired

    public FilmService(@Qualifier("UserDbStorage") UserStorage userStorage,
                       @Qualifier("FilmDbStorage") FilmStorage filmStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

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
                .sorted(filmComparatorByLikes).collect(Collectors.toList());

        if (count == null || count > likedFilms.size()) {
            return likedFilms;
        }

        return likedFilms.subList(0, count);
    }


    class FilmComparatorByLikes implements Comparator<Film> {

        @Override
        public int compare(Film o1, Film o2) {
            if (getFilmLikeCount(o1.getId()) > getFilmLikeCount(o2.getId())) {
                return -1;
            } else if (getFilmLikeCount(o1.getId()) < getFilmLikeCount(o2.getId())) {
                return 1;
            }
            return 0;

        }

    }

}
