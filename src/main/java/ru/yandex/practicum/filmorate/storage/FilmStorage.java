package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Set;

public interface FilmStorage {
    Set<Film> getAll();

    Film addNew(Film film);

    Film change(Film film);

    Film getFilmById(Long id);

    HashMap<Long, Set<Long>> getLikeList();

    LocalDate getLowThresholdDate();

}
