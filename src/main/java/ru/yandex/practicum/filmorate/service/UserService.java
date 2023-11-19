package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.RecordNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserStorage userStorage;
    private final Comparator<User> userComparatorByID = new UserComparatorById();

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void makeFriends(Long firstUserId, Long secondUserId)
            throws RecordNotFoundException {
        User firstUser = userStorage.getUserById(firstUserId);
        User secondUser = userStorage.getUserById(secondUserId);

        if (!userStorage.getFriendLists().containsKey(firstUserId)) {
            userStorage.getFriendLists().put(firstUserId, new HashSet<>());
        }

        if (!userStorage.getFriendLists().containsKey(secondUserId)) {
            userStorage.getFriendLists().put(secondUserId, new HashSet<>());
        }

        userStorage.getFriendLists().get(firstUserId).add(secondUser);
        userStorage.getFriendLists().get(secondUserId).add(firstUser);
    }

    public void deleteFriends(Long firstUserId, Long secondUserId)
            throws RecordNotFoundException {
        User firstUser = userStorage.getUserById(firstUserId);
        User secondUser = userStorage.getUserById(secondUserId);

        if (userStorage.getFriendLists().containsKey(firstUserId)) {
            userStorage.getFriendLists().get(firstUserId).remove(secondUser);
        }

        if (userStorage.getFriendLists().containsKey(secondUserId)) {
            userStorage.getFriendLists().get(secondUserId).remove(firstUser);
        }

    }

    public List<User> getFriendList(Long userId)
            throws RecordNotFoundException {
        if (userStorage.existingUser(userId)) {
            if (userStorage.getFriendLists().containsKey(userId)) {
                ArrayList<User> sortedList = new ArrayList<>(userStorage.getFriendLists().get(userId));
                sortedList.sort(userComparatorByID);
                return sortedList;
            }

        }
        return new ArrayList<>();
    }

    public Set<User> getCommonFriendList(Long userId, Long otherUserId)
            throws RecordNotFoundException {
        userStorage.existingUser(userId);
        userStorage.existingUser(otherUserId);

        if (!(userStorage.getFriendLists().containsKey(userId)
                && userStorage.getFriendLists().containsKey(otherUserId))) {
            return new HashSet<>();
        }

        return userStorage.getFriendLists().get(userId)
                .stream().filter(user -> userStorage.getFriendLists().get(otherUserId).contains(user))
                .collect(Collectors.toSet());
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
