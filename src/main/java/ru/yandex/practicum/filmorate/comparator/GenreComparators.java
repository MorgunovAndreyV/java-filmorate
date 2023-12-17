package ru.yandex.practicum.filmorate.comparator;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Comparator;

public class GenreComparators {

    public static Comparator<Genre> compareGenreById = (genre1, genre2) -> {
        if (genre1.getId() > genre2.getId()) {
            return 1;
        } else if (genre1.getId() < genre2.getId()) {
            return -1;
        }
        return 0;
    };
}
