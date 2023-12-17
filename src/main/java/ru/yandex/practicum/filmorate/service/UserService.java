package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.RecordNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserStorageException;
import ru.yandex.practicum.filmorate.exception.UserValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserStorage userStorage;
    private static final Comparator<User> userComparatorByID = new UserComparatorById();

    @Autowired
    public UserService(@Qualifier("UserDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Set<User> getAll() {
        return userStorage.getAll();
    }

    public User addNew(User user) throws UserStorageException, UserValidationException {
        return userStorage.addNew(user);
    }

    public User getUserById(Long id) throws RecordNotFoundException {
        return userStorage.getUserById(id);
    }

    public User change(User user) throws RecordNotFoundException, UserValidationException {
        return userStorage.change(user);
    }

    public void makeFriends(Long firstUserId, Long secondUserId)
            throws RecordNotFoundException {
        User firstUser = userStorage.getUserById(firstUserId);
        User secondUser = userStorage.getUserById(secondUserId);

        userStorage.addToFriends(firstUser, secondUser);
    }

    public void deleteFriends(Long firstUserId, Long secondUserId)
            throws RecordNotFoundException {
        User firstUser = userStorage.getUserById(firstUserId);
        User secondUser = userStorage.getUserById(secondUserId);

        userStorage.removeFromFriends(firstUser, secondUser);

    }

    public List<User> getFriendList(Long userId)
            throws RecordNotFoundException {
        if (userStorage.existingUser(userId)) {
            return userStorage.getUserFriendList(userId);

        }
        return new ArrayList<>();
    }

    public Set<User> getCommonFriendList(Long userId, Long otherUserId)
            throws RecordNotFoundException {
        userStorage.existingUser(userId);
        userStorage.existingUser(otherUserId);

        return userStorage.getUserFriendList(userId)
                .stream().filter(user -> userStorage.getUserFriendList(otherUserId).contains(user))
                .collect(Collectors.toSet());
    }

    public static Comparator<User> getUserComparatorByID() {
        return userComparatorByID;
    }

    static class UserComparatorById implements Comparator<User> {

        @Override
        public int compare(User o1, User o2) {
            if (o1.getId() != null && o2.getId() != null) {
                return o1.getId().compareTo(o2.getId());

            } else {
                if (o1.getId() == null && o2.getId() != null) {
                    return -1;
                } else if (o1.getId() != null) {
                    return 1;
                } else return 0;
            }

        }
    }

}
