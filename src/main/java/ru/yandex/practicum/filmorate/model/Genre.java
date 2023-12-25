package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
public class Genre {
    private Long id;
    private String name;

    public Map<String, Object> toMap() {
        Map<String, Object> values = new HashMap<>();
        values.put("id", id);
        values.put("name", name);

        return values;
    }

}
