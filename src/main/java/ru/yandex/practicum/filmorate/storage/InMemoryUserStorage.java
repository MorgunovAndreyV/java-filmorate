package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.RecordNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserStorageException;
import ru.yandex.practicum.filmorate.exception.UserValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component("InMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {
    private Set<User> users = new HashSet<>();
    private HashMap<Long, Set<User>> friendLists = new HashMap<>();

    @Override
    public Set<User> getAll() {
        return users;
    }

    @Override
    public User addNew(User user) throws UserStorageException, UserValidationException {
        userValidations(user);

        if (users.contains(user)) {
            throw new UserStorageException("Такой пользователь уже добавлен");
        }

        assignNewId(user);
        users.add(user);
        friendLists.put(user.getId(), new HashSet<>());

        log.info("Новый пользователь добавлен успешно. id:" + user.getId());

        return user;
    }

    @Override
    public User change(User user) throws RecordNotFoundException, UserValidationException {
        User userFromBase = getUserById(user.getId());

        userValidations(user);
        userFromBase.setName(user.getName());
        userFromBase.setLogin(user.getLogin());
        userFromBase.setEmail(user.getEmail());
        userFromBase.setBirthday(user.getBirthday());

        log.info("Запись пользователя изменен успешно. id:" + user.getId());

        return userFromBase;
    }

    public HashMap<Long, Set<User>> getFriendLists() {
        return friendLists;
    }

    @Override
    public void addToFriends(User userFirst, User userSecond) {
        getFriendLists().get(userFirst.getId()).add(userSecond);
    }

    @Override
    public void removeFromFriends(User userFirst, User userSecond) {
        if (getFriendLists().containsKey(userFirst.getId())) {
            getFriendLists().get(userFirst.getId()).remove(userSecond);
        }

        if (getFriendLists().containsKey(userSecond.getId())) {
            getFriendLists().get(userSecond.getId()).remove(userFirst);
        }


    }

    @Override
    public ArrayList<User> getUserFriendList(Long userId) {
        if (getFriendLists().containsKey(userId)) {
            ArrayList<User> sortedList = new ArrayList<>(getFriendLists().get(userId));
            sortedList.sort(UserService.getUserComparatorByID());

            return sortedList;

        } else return new ArrayList<>();

    }

    private void assignNewId(User user) {
        if (users != null) {
            user.setId((long) (users.size() + 1));
        }

    }

    private void userValidations(User user) throws RecordNotFoundException {
        if (user.getLogin() == null || user.getLogin().isEmpty()) {
            throw new UserValidationException("Логин не может быть пустым");

        } else if ((user.getLogin().contains(" "))) {
            throw new UserValidationException("Логин не может содержать пробелы");

        }

        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());

        }

        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new UserValidationException("Почта не может быть пустой");

        } else if (!user.getEmail().contains("@")) {
            throw new UserValidationException("Некорректный формат почты");

        }

        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new UserValidationException("Дата рождения не может быть в будущем");

        }

    }

    public User getUserById(Long id) throws RecordNotFoundException {
        Optional<User> possibleFilm = users.stream()
                .filter(user1 -> user1.getId().equals(id))
                .findFirst();

        if (possibleFilm.isEmpty()) {
            throw new RecordNotFoundException("Пользователь с ID " + id + " не найден.");

        }

        return possibleFilm.get();
    }

    public boolean existingUser(Long id) throws RecordNotFoundException {
        if (users.stream()
                .filter(user1 -> user1.getId().equals(id))
                .findFirst().orElse(null) != null) {
            return true;
        } else {
            throw new RecordNotFoundException("Пользователь с ID " + id + " не найден.");
        }

    }

}
