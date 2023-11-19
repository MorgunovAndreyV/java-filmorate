package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.HashMap;
import java.util.Set;

public interface UserStorage {
    Set<User> getAll();

    User addNew(User user);

    User change(User user);

    User getUserById(Long id);

    HashMap<Long, Set<User>> getFriendLists();

    boolean existingUser(Long id);
}
