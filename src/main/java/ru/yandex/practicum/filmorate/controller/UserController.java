package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.UserControllerException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private Set<User> users = new HashSet<>();

    @GetMapping
    public Set<User> getAll() {
        return users;
    }

    @PostMapping
    public User addNew(@RequestBody User user) {
        userValidations(user);

        if (users.contains(user)) {
            throw new UserControllerException("Такой пользователь уже добавлен");
        }

        assignNewId(user);
        users.add(user);
        log.info("Новый пользователь добавлен успешно. id:" + user.getId());

        return user;
    }

    @PutMapping
    public User change(@RequestBody User user) {
        User userFromBase = getUserById(user.getId());

        userValidations(user);
        userFromBase.setName(user.getName());
        userFromBase.setLogin(user.getLogin());
        userFromBase.setEmail(user.getEmail());
        userFromBase.setBirthday(user.getBirthday());

        log.info("Запись пользователя изменен успешно. id:" + user.getId());

        return userFromBase;
    }

    private void assignNewId(User user) {
        if (users != null) {
            user.setId((long) (users.size() + 1));
        }

    }

    private void userValidations(User user) {
        if (user.getLogin() == null || user.getLogin().isEmpty()) {
            throw new UserControllerException("Логин не может быть пустым");

        } else if ((user.getLogin().contains(" "))) {
            throw new UserControllerException("Логин не может содержать пробелы");

        }

        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());

        }

        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new UserControllerException("Почта не может быть пустой");

        } else if (!user.getEmail().contains("@")) {
            throw new UserControllerException("Некорректный формат почты");

        }

        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new UserControllerException("Дата рождения не может быть в будущем");

        }

    }

    private User getUserById(Long id) {
        Optional<User> possibleFilm = users.stream()
                .filter(user1 -> user1.getId().equals(id))
                .findFirst();

        if (possibleFilm.isEmpty()) {
            throw new UserControllerException("Пользователь с ID " + id + " не найден.");

        }

        return possibleFilm.get();
    }

}
