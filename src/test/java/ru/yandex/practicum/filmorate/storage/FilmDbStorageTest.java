package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.RecordNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {
    private final JdbcTemplate jdbcTemplate;
    private FilmDbStorage filmDbStorage;
    private UserDbStorage userDbStorage;
    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private Film film1;
    private Film film2;
    private Genre genre1;
    private Genre genre2;
    private Genre genre3;
    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    public void setUp() {
        userDbStorage = new UserDbStorage(jdbcTemplate);
        filmDbStorage = new FilmDbStorage(jdbcTemplate);

        film1 = Film.builder()
                .name("Кикуджиро")
                .description("Кикуджиро не любит работать, зато обожает азартные игры и " +
                        "часто решает проблемы с помощью кулаков.")
                .releaseDate(LocalDate.parse("1999-05-20", dateTimeFormatter))
                .duration(122)
                .mpa(MPA.builder().id(1L).build())
                .genres(new ArrayList<>())
                .build();
        genre1 = filmDbStorage.getGenreById(1L);
        genre2 = filmDbStorage.getGenreById(2L);
        genre3 = filmDbStorage.getGenreById(3L);
        user1 = User.builder()
                .login("testlogn")
                .name("Jane Doe")
                .email("janedoe@gmail.com")
                .birthday(LocalDate.parse("1967-02-13", dateTimeFormatter))
                .build();

        user2 = User.builder()
                .login("cckrch")
                .name("Cockroach Joe")
                .email("cckrch@ya.ru")
                .birthday(LocalDate.parse("1988-12-09", dateTimeFormatter))
                .build();

        user3 = User.builder()
                .login("yaruu2")
                .name("Yari Ulmanen")
                .email("yaru@yahoo.com")
                .birthday(LocalDate.parse("1987-08-07", dateTimeFormatter))
                .build();


    }

    @Test
    public void checkNewFilmWasAdded() {
        filmDbStorage.addNew(film1);
        film1.getMpa()
                .setName(filmDbStorage.getMPAById(film1.getMpa().getId()).getName());
        List<Film> filmList = filmDbStorage.getAll();

        assertNotNull(filmList);
        assertTrue(filmList.contains(film1));
    }

    @Test
    public void checkChangeForNotObjectFilmFields() {
        filmDbStorage.addNew(film1);
        List<Film> filmList = filmDbStorage.getAll();
        assertNotNull(filmList);

        Film filmFromDb = filmList.stream()
                .filter(film -> film.getId().equals(film1.getId()))
                .findFirst().orElse(null);

        Long id = filmFromDb.getId();
        String newFilmName = film1.getName() + "testname!";
        String newFilmDescription = film1.getDescription() + "testDescr!";
        Integer newFilmDuration = film1.getDuration() + 1;
        LocalDate newFilmReleaseDate = film1.getReleaseDate().plusMonths(1);

        filmDbStorage.change(Film.builder()
                .id(id)
                .name(newFilmName)
                .description(newFilmDescription)
                .duration(newFilmDuration)
                .releaseDate(newFilmReleaseDate)
                .build());

        filmList = filmDbStorage.getAll();
        assertNotNull(filmList);

        filmFromDb = filmList.stream()
                .filter(film -> film.getId().equals(film1.getId()))
                .findFirst().orElse(null);

        assertEquals(newFilmName, filmFromDb.getName());
        assertEquals(newFilmDescription, filmFromDb.getDescription());
        assertEquals(newFilmDuration, filmFromDb.getDuration());
        assertEquals(newFilmReleaseDate, filmFromDb.getReleaseDate());

    }

    @Test
    public void checkChangeForMPAFilmFields() {
        filmDbStorage.addNew(film1);
        assertNotNull(film1.getId());
        assertTrue(film1.getId() > 0);

        Film filmFromDb = filmDbStorage.getFilmById(film1.getId());

        assertNotNull(filmFromDb);
        assertTrue(film1.getMpa().getId().equals(filmFromDb.getMpa().getId()));

        Long newMPAId = 2L;

        filmDbStorage.change(Film.builder()
                .id(film1.getId())
                .name(film1.getName())
                .description(film1.getDescription())
                .duration(film1.getDuration())
                .releaseDate(film1.getReleaseDate())
                .mpa(MPA.builder()
                        .id(newMPAId)
                        .build())
                .build());

        filmFromDb = filmDbStorage.getFilmById(film1.getId());

        assertEquals(filmFromDb.getMpa().getId(), newMPAId);
        assertEquals(filmFromDb.getMpa().getName(), filmDbStorage.getMPAById(newMPAId).getName());

    }

    @Test
    public void checkChangeForGenreFilmFieldsByErasing() {
        film1.getGenres().add(genre1);
        film1.getGenres().add(genre2);

        filmDbStorage.addNew(film1);
        assertNotNull(film1.getId());
        assertTrue(film1.getId() > 0);

        Film filmFromDb = filmDbStorage.getFilmById(film1.getId());

        assertNotNull(filmFromDb);
        assertTrue(film1.getGenres().contains(genre1));
        assertTrue(film1.getGenres().contains(genre2));

        filmDbStorage.change(Film.builder()
                .id(film1.getId())
                .name(film1.getName())
                .description(film1.getDescription())
                .duration(film1.getDuration())
                .releaseDate(film1.getReleaseDate())
                .mpa(film1.getMpa())
                .genres(new ArrayList<>())
                .build());

        filmFromDb = filmDbStorage.getFilmById(film1.getId());

        assertTrue(filmFromDb.getGenres().isEmpty());

    }

    @Test
    public void checkChangeForGenreFilmFieldsByReplacing() {
        film1.getGenres().add(genre1);
        film1.getGenres().add(genre2);

        filmDbStorage.addNew(film1);
        assertNotNull(film1.getId());
        assertTrue(film1.getId() > 0);

        Film filmFromDb = filmDbStorage.getFilmById(film1.getId());

        assertNotNull(filmFromDb);
        assertTrue(film1.getGenres().contains(genre1));
        assertTrue(film1.getGenres().contains(genre2));

        List<Genre> newGenreList = new ArrayList<>();
        newGenreList.add(genre3);

        filmDbStorage.change(Film.builder()
                .id(film1.getId())
                .name(film1.getName())
                .description(film1.getDescription())
                .duration(film1.getDuration())
                .releaseDate(film1.getReleaseDate())
                .mpa(film1.getMpa())
                .genres(newGenreList)
                .build());

        filmFromDb = filmDbStorage.getFilmById(film1.getId());

        assertFalse(filmFromDb.getGenres().isEmpty());
        assertTrue(filmFromDb.getGenres().contains(genre3));
        assertEquals(1, filmFromDb.getGenres().size());

    }

    @Test
    void checkNotExistingMPA() {
        Long inExistingMPAId = 99999L;
        Exception e = Assertions.assertThrows(RecordNotFoundException.class,
                () -> filmDbStorage.getMPAById(inExistingMPAId));

        Assertions.assertEquals("Рейтинг с ID " + inExistingMPAId + " не найден.", e.getMessage());
    }

    @Test
    void checkNotExistingGenre() {
        Long inexistingGenreId = 99999L;
        Exception e = Assertions.assertThrows(RecordNotFoundException.class,
                () -> filmDbStorage.getGenreById(inexistingGenreId));

        Assertions.assertEquals("Жанр с ID " + inexistingGenreId + " не найден.", e.getMessage());
    }

    @Test
    void checkLikeCreation() {
        filmDbStorage.addNew(film1);
        userDbStorage.addNew(user1);

        int initialFilmLikeCount = filmDbStorage.getFilmLikeStorageCount(film1.getId());

        filmDbStorage.createLikeFilmByUser(film1.getId(), user1.getId());
        filmDbStorage.createLikeFilmByUser(film1.getId(), user1.getId());
        filmDbStorage.createLikeFilmByUser(film1.getId(), user1.getId());

        int newFilmLikeCount = filmDbStorage.getFilmLikeStorageCount(film1.getId());
        Set<Long> likesForFilm = filmDbStorage.getLikeListForFilm(film1.getId());

        assertEquals(1, newFilmLikeCount - initialFilmLikeCount);
        assertTrue(likesForFilm.contains(user1.getId()));
    }

    @Test
    void checkLikeDeletion() {
        filmDbStorage.addNew(film1);
        userDbStorage.addNew(user1);
        userDbStorage.addNew(user2);
        userDbStorage.addNew(user3);

        int initialFilmLikeCount = filmDbStorage.getFilmLikeStorageCount(film1.getId());

        filmDbStorage.createLikeFilmByUser(film1.getId(), user1.getId());
        filmDbStorage.createLikeFilmByUser(film1.getId(), user2.getId());
        filmDbStorage.createLikeFilmByUser(film1.getId(), user3.getId());

        int newFilmLikeCount = filmDbStorage.getFilmLikeStorageCount(film1.getId());
        Set<Long> likesForFilm = filmDbStorage.getLikeListForFilm(film1.getId());

        assertEquals(3, newFilmLikeCount - initialFilmLikeCount);
        assertTrue(likesForFilm.contains(user1.getId()));

        filmDbStorage.deleteLikeFilmByUser(film1.getId(), user2.getId());
        newFilmLikeCount = filmDbStorage.getFilmLikeStorageCount(film1.getId());
        likesForFilm = filmDbStorage.getLikeListForFilm(film1.getId());

        assertEquals(2, newFilmLikeCount - initialFilmLikeCount);
        assertTrue(likesForFilm.contains(user1.getId()));
        assertTrue(likesForFilm.contains(user3.getId()));
        assertFalse(likesForFilm.contains(user2.getId()));

    }

    @Test
    void likeFilmNotExisting() {
        userDbStorage.addNew(user1);
        Exception e = Assertions.assertThrows(RecordNotFoundException.class, () -> {
            filmDbStorage.createLikeFilmByUser(film1.getId(), user1.getId());
        });

        assertEquals("Фильм с ID " + film1.getId() + " не найден.", e.getMessage());

    }

    @Test
    void getAllMPAs() {
        assertFalse(filmDbStorage.getAllMPAs().isEmpty());
    }

    @Test
    void getAllGenres() {
        assertFalse(filmDbStorage.getAllGenres().isEmpty());
    }

}