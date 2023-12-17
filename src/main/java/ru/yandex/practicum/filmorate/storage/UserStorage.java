package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public interface UserStorage {
    Set<User> getAll();

    User addNew(User user);

    User change(User user);

    User getUserById(Long id);

    void addToFriends(User user_first, User user_second);

    void removeFromFriends(User user_first, User user_second);

    List<User> getUserFriendList(Long userId);

    boolean existingUser(Long id);
}
