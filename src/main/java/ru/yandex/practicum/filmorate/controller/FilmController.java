package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final FilmStorage filmStorage;
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmStorage filmStorage, FilmService filmService) {
        this.filmStorage = filmStorage;
        this.filmService = filmService;
    }

    @GetMapping
    public Set<Film> getAll() {
        return filmStorage.getAll();
    }

    @PostMapping
    public Film addNew(@RequestBody Film film) {
        return filmStorage.addNew(film);
    }

    @PutMapping
    public Film change(@RequestBody Film film) {
        return filmStorage.change(film);
    }

    @GetMapping("/{id}")
    public Film getById(@PathVariable Long id) {
        return filmStorage.getFilmById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public long likeFilmByUser(@PathVariable("id") Long filId,
                               @PathVariable("userId") Long userId) {
        filmService.likeFilmByUser(filId, userId);

        return userId;
    }

    @DeleteMapping("/{id}/like/{userId}")
    public long unlikeFilmByUser(@PathVariable("id") Long filId,
                                 @PathVariable("userId") Long userId) {
        filmService.unlikeFilmByUser(filId, userId);

        return userId;
    }

    @GetMapping("/popular")
    public List<Film> getTopCountPopular(@RequestParam(required = false, name = "count") Integer count) {
        return filmService.topLikes(count);
    }

}