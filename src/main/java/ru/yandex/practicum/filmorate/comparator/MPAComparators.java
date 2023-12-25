package ru.yandex.practicum.filmorate.comparator;

import ru.yandex.practicum.filmorate.model.MPA;

import java.util.Comparator;

public class MPAComparators {

    public static Comparator<MPA> compareMPAById = (mpa1, mpa2) -> {
        if (mpa1.getId() > mpa2.getId()) {
            return 1;
        } else if (mpa1.getId() < mpa2.getId()) {
            return -1;
        }
        return 0;
    };
}
