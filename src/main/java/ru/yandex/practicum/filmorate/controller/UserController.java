package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final UserStorage userStorage;
    private final UserService userService;

    @Autowired
    public UserController(UserStorage userStorage, UserService userService) {
        this.userStorage = userStorage;
        this.userService = userService;

    }

    @GetMapping
    public Set<User> getAll() {
        return userStorage.getAll();
    }

    @PostMapping
    public User addNew(@RequestBody User user) {
        return userStorage.addNew(user);
    }

    @PutMapping
    public User change(@RequestBody User user) {
        return userStorage.change(user);
    }

    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) {
        return userStorage.getUserById(id);
    }


    @PutMapping("/{id}/friends/{friendId}")
    public Set<User> makeFriends(@PathVariable("id") Long userId,
                                 @PathVariable("friendId") Long newFriendId) {
        userService.makeFriends(userId, newFriendId);

        return userStorage.getFriendLists().get(userId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public long deleteFriend(@PathVariable("id") Long userId,
                             @PathVariable("friendId") Long newFriendId) {
        userService.deleteFriends(userId, newFriendId);

        return userId;
    }

    @GetMapping("{id}/friends")
    public List<User> getFriendList(@PathVariable Long id) {
        return userService.getFriendList(id);
    }

    @GetMapping("{id}/friends/common/{otherId}")
    public Set<User> getCommonFriendList(@PathVariable("id") Long userId,
                                         @PathVariable("otherId") Long otherId) {
        return userService.getCommonFriendList(userId, otherId);
    }

}
