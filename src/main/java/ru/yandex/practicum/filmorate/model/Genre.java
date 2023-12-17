package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
public class Genre {
    private Long id;
    private String name;
    public static Comparator<Genre> comparingGenreById = (genre1, genre2) -> {
        if (genre1.getId() > genre2.getId()) {
            return 1;
        } else if (genre1.getId() < genre2.getId()) {
            return -1;
        }
        return 0;
    };

    public Map<String, Object> toMap() {
        Map<String, Object> values = new HashMap<>();
        values.put("id", id);
        values.put("name", name);

        return values;
    }

}
