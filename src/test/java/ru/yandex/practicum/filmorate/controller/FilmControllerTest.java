package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.FilmControllerException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

class FilmControllerTest {
    private FilmController filmController;
    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private Film film;
    private Film existingFilm;
    private Long existingFilmId;

    @BeforeEach
    void setUp() {
        filmController = new FilmController();
        film = Film.builder()
                .name("Sleepy hollow")
                .description("That creepy movie")
                .releaseDate(LocalDate.parse("1999-02-01", dateTimeFormatter))
                .duration(120)
                .build();
        existingFilm = Film.builder()
                .name("Hackers")
                .description("Crash n burn stuff")
                .releaseDate(LocalDate.parse("1996-07-12", dateTimeFormatter))
                .duration(90)
                .build();

        filmController.addNew(existingFilm);
        existingFilmId = filmController.getAll()
                .stream().filter(film1 -> film1.equals(existingFilm)).findFirst().get().getId();

    }

    @Test
    void checkaddNewValidFilm() {
        filmController.addNew(film);
        Assertions.assertTrue(filmController.getAll().contains(film));
    }

    @Test
    void checkaddFilmWithEmptyName() {
        film.setName("");
        Exception exception = Assertions.assertThrows(FilmControllerException.class,
                () -> filmController.addNew(film));
        Assertions.assertEquals("Название фильма не может быть пустым", exception.getMessage());
        Assertions.assertFalse(filmController.getAll().contains(film));

        film.setName(null);
        exception = Assertions.assertThrows(FilmControllerException.class,
                () -> filmController.addNew(film));
        Assertions.assertEquals("Название фильма не может быть пустым", exception.getMessage());
        Assertions.assertFalse(filmController.getAll().contains(film));
    }

    @Test
    void checkaddFilmWithBigDescription() {
        film.setDescription("That creepy movie" +
                "That creepy movie" + "That creepy movie" + "That creepy movie" + "That creepy movie" +
                "That creepy movie" + "That creepy movie" + "That creepy movie" + "That creepy movie" +
                "That creepy movie" + "That creepy movie" + "That creepy movie" + "That creepy movie" +
                "That creepy movie" + "That creepy movie");

        Exception exception = Assertions.assertThrows(FilmControllerException.class,
                () -> filmController.addNew(film));
        Assertions.assertEquals("Описание фильма не может быть длиннее 200 символов", exception.getMessage());
        Assertions.assertFalse(filmController.getAll().contains(film));
    }

    @Test
    void checkaddFilmWithWrongDate() {
        film.setReleaseDate(LocalDate.parse("1699-02-01", dateTimeFormatter));

        Exception exception = Assertions.assertThrows(FilmControllerException.class,
                () -> filmController.addNew(film));
        Assertions.assertEquals("Дата релиза не может быть раньше " + filmController.getLowThreshholdDate(),
                exception.getMessage());
        Assertions.assertFalse(filmController.getAll().contains(film));
    }

    @Test
    void checkaddFilmWithWrongDuration() {
        film.setDuration(-2);
        Exception exception = Assertions.assertThrows(FilmControllerException.class,
                () -> filmController.addNew(film));
        Assertions.assertEquals("Продолжительность фильма не может быть отрицательной",
                exception.getMessage());
        Assertions.assertFalse(filmController.getAll().contains(film));
    }

    @Test
    void checkModifyFilmCorrectly() {
        String newName = "The Hackers";
        String newDescription = "Zero cool is back";
        LocalDate newDate = LocalDate.parse("1996-07-12", dateTimeFormatter);
        Integer newDuration = 92;
        filmController.change(Film.builder()
                .id(existingFilmId)
                .name(newName)
                .description(newDescription)
                .releaseDate(newDate)
                .duration(newDuration)
                .build()
        );

        Assertions.assertTrue(newName.equals(existingFilm.getName()) &&
                newDescription.equals(existingFilm.getDescription()) &&
                newDate.equals(existingFilm.getReleaseDate()) &&
                newDuration.equals(existingFilm.getDuration()));
    }

    @Test
    void checkModifyNameWrongly() {
        final String finalNewName = "";

        Exception exception = Assertions.assertThrows(FilmControllerException.class,
                () -> filmController.change(
                        Film.builder()
                                .id(existingFilmId)
                                .name(finalNewName)
                                .description(existingFilm.getDescription())
                                .releaseDate(existingFilm.getReleaseDate())
                                .duration(existingFilm.getDuration())
                                .build()
                )
        );

        Assertions.assertEquals("Название фильма не может быть пустым", exception.getMessage());
        Assertions.assertNotEquals(finalNewName, existingFilm.getName());

        final String finalNewName2 = null;

        exception = Assertions.assertThrows(FilmControllerException.class,
                () -> filmController.change(
                        Film.builder()
                                .id(existingFilmId)
                                .name(finalNewName2)
                                .description(existingFilm.getDescription())
                                .releaseDate(existingFilm.getReleaseDate())
                                .duration(existingFilm.getDuration()).build()
                )
        );

        Assertions.assertEquals("Название фильма не может быть пустым", exception.getMessage());
        Assertions.assertNotEquals(finalNewName2, existingFilm.getName());

    }

    @Test
    void checkModifyDescriptionWrongly() {
        final String finalNewDescription = "New description is too big New description is too big New description is " +
                "too big New description is too big New description is too big New description is too big New " +
                "description is too big New description is too big New description is too big New description is too big ";

        Exception exception = Assertions.assertThrows(FilmControllerException.class,
                () -> filmController.change(
                        Film.builder()
                                .id(existingFilmId)
                                .name(existingFilm.getName())
                                .description(finalNewDescription)
                                .releaseDate(existingFilm.getReleaseDate())
                                .duration(existingFilm.getDuration())
                                .build()
                )
        );

        Assertions.assertEquals("Описание фильма не может быть длиннее 200 символов", exception.getMessage());
        Assertions.assertNotEquals(finalNewDescription, existingFilm.getDescription());

    }

    @Test
    void checkModifyReleaseDateWrongly() {
        final LocalDate finalNewReleaseDate = LocalDate.parse("1299-02-01", dateTimeFormatter);

        Exception exception = Assertions.assertThrows(FilmControllerException.class,
                () -> filmController.change(
                        Film.builder()
                                .id(existingFilmId)
                                .name(existingFilm.getName())
                                .description(existingFilm.getDescription())
                                .releaseDate(finalNewReleaseDate)
                                .duration(existingFilm.getDuration())
                                .build()
                )
        );

        Assertions.assertEquals("Дата релиза не может быть раньше " + filmController.getLowThreshholdDate(),
                exception.getMessage());
        Assertions.assertNotEquals(finalNewReleaseDate, existingFilm.getReleaseDate());
    }

    @Test
    void checkModifyDurationWrongly() {
        final Integer finalNewDuration = -2;

        Exception exception = Assertions.assertThrows(FilmControllerException.class,
                () -> filmController.change(
                        Film.builder()
                                .id(existingFilmId)
                                .name(existingFilm.getName())
                                .description(existingFilm.getDescription())
                                .releaseDate(existingFilm.getReleaseDate())
                                .duration(finalNewDuration)
                                .build()
                )
        );

        Assertions.assertEquals("Продолжительность фильма не может быть отрицательной",
                exception.getMessage());
        Assertions.assertNotEquals(finalNewDuration, existingFilm.getDuration());
    }

}