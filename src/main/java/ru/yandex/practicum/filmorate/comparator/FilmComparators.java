package ru.yandex.practicum.filmorate.comparator;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Comparator;

public class FilmComparators {

    public static Comparator<Film> compareFilmById = (film1, film2) -> {
        if (film1.getId() > film2.getId()) {
            return 1;
        } else if (film1.getId() < film2.getId()) {
            return -1;
        }
        return 0;
    };

    public static Comparator<Film> getComparatorByFilmLikes(FilmService filmService) {
        return new FilmComparatorByLikes(filmService);
    }

    static class FilmComparatorByLikes implements Comparator<Film> {
        private final FilmService filmService;

        public FilmComparatorByLikes(FilmService filmService) {
            this.filmService = filmService;
        }

        @Override
        public int compare(Film o1, Film o2) {
            if (filmService.getFilmLikeCount(o1.getId()) > filmService.getFilmLikeCount(o2.getId())) {
                return -1;
            } else if (filmService.getFilmLikeCount(o1.getId()) < filmService.getFilmLikeCount(o2.getId())) {
                return 1;
            }
            return 0;

        }

    }


}
