package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public Set<User> getAll() {
        return userService.getAll();
    }

    @PostMapping
    public User addNew(@RequestBody User user) {
        return userService.addNew(user);
    }

    @PutMapping
    public User change(@RequestBody User user) {
        return userService.change(user);
    }

    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) {
        return userService.getUserById(id);
    }


    @PutMapping("/{id}/friends/{friendId}")
    public List<User> makeFriends(@PathVariable("id") Long userId,
                                  @PathVariable("friendId") Long newFriendId) {
        userService.makeFriends(userId, newFriendId);

        return userService.getFriendList(userId);
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
