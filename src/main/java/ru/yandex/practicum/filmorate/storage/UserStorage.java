package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Set;

public interface UserStorage {
    Set<User> getAll();

    User addNew(User user);

    User change(User user);

    User getUserById(Long id);

    void addToFriends(User userFirst, User userSecond);

    void removeFromFriends(User userFirst, User userSecond);

    List<User> getUserFriendList(Long userId);

    boolean existingUser(Long id);
}
