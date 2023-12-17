package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/mpa")
@Slf4j
public class MPAController {
    private final FilmService filmService;

    @Autowired
    public MPAController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public List<MPA> getAll() {
        return filmService.getAllMPAs();
    }

    @GetMapping("/{id}")
    public MPA getById(@PathVariable Long id) {
        return filmService.getMPAById(id);
    }



}
