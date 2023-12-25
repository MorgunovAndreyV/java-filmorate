package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@Slf4j
@RequiredArgsConstructor
public class MPAController {
    private final FilmService filmService;

    @GetMapping
    public List<MPA> getAll() {
        return filmService.getAllMPAs();
    }

    @GetMapping("/{id}")
    public MPA getById(@PathVariable Long id) {
        return filmService.getMPAById(id);
    }


}
