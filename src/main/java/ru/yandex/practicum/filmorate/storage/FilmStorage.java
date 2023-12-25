package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface FilmStorage {
    List<Film> getAll();

    Film addNew(Film film);

    Film change(Film film);

    Film getFilmById(Long id);

    Set<Long> getLikeListForFilm(Long id);

    LocalDate getLowThresholdDate();

    void createLikeFilmByUser(Long filmId, Long userId);

    void deleteLikeFilmByUser(Long filmId, Long userId);

    int getFilmLikeStorageCount(Long filmId);

    List<Genre> getAllGenres();

    List<MPA> getAllMPAs();

    Genre getGenreById(Long id);

    MPA getMPAById(Long id);

}
