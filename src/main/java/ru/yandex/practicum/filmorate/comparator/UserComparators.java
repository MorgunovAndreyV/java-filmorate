package ru.yandex.practicum.filmorate.comparator;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Comparator;

public class UserComparators {

    public static Comparator<User> compareUsersById = (user1, user2) -> {
        if (user1.getId() != null && user2.getId() != null) {
            return user1.getId().compareTo(user2.getId());

        } else {
            if (user1.getId() == null && user2.getId() != null) {
                return -1;
            } else if (user1.getId() != null) {
                return 1;
            } else return 0;
        }

    };
}
