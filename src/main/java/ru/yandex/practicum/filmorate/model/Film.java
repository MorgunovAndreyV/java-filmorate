package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

@Data
@Builder
public class Film {
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private MPA mpa;
    private List<Genre> genres;
    public static Comparator <Film> comparingFilmById = (film1, film2) -> {
        if (film1.getId() > film2.getId()) {
            return 1;
        } else if (film1.getId() < film2.getId()) {
            return -1;
        }
        return 0;
    };

    public Map<String, Object> toMap() {
        Map<String, Object> values = new HashMap<>();
        values.put("id", id);
        values.put("name", name);
        values.put("description", description);
        values.put("release_date", releaseDate);
        values.put("duration_min", duration);
        values.put("mpa_assignment_id", mpa.getId());
        values.put("genre_assignment_id", genres);
        return values;
    }

    public void setGenres(List<Genre> genres) {
        Set<Genre> unifiedGenreList = new HashSet<>(genres);
        List<Genre> sortedGenreList = new ArrayList<>(unifiedGenreList);

        sortedGenreList.sort((genre1, genre2) -> {
            if (genre1.getId() > genre2.getId()) {
                return 1;
            } else if (genre1.getId() < genre2.getId()) {
                return -1;
            }
            return 0;
        });

        this.genres = sortedGenreList;
    }

}
