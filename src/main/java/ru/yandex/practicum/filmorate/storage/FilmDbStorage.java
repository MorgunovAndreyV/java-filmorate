package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.comparator.FilmComparators;
import ru.yandex.practicum.filmorate.comparator.GenreComparators;
import ru.yandex.practicum.filmorate.comparator.MPAComparators;
import ru.yandex.practicum.filmorate.exception.FilmValidationException;
import ru.yandex.practicum.filmorate.exception.RecordNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Component("FilmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    static final LocalDate LOW_THRESHOLD_DATE =
            LocalDate.parse("28.12.1895", DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    static final int DESCRIPTION_LENGTH = 200;

    @Override
    public List<Film> getAll() {
        String sqlQuery = "SELECT f.ID AS \"FILM_ID\", f.NAME, f.DESCRIPTION, f.RELEASE_DATE, " +
                "f.DURATION_MIN, m.ID AS \"MPA_ID\" , m.NAME AS \"MPA_NAME\" " +
                "from FILMS f " +
                "LEFT JOIN MPA_ASSIGNMENTS ma ON ma.FILM_ID = f.ID " +
                "LEFT JOIN MPAS m ON m.ID = ma.MPA_ID";

        List<Film> fetchedFilms = jdbcTemplate.query(sqlQuery, this::mapRowToFilm);

        fetchedFilms
                .forEach(film -> {
                    film.setGenres(getGenresForFilm(film));
                });

        fetchedFilms.sort(FilmComparators.compareFilmById);

        return fetchedFilms;
    }

    public List<Genre> getAllGenres() {
        String sqlQuery = "SELECT ID, NAME from GENRES";
        List<Genre> outPutList = new ArrayList<>(jdbcTemplate.query(sqlQuery, this::mapRowToGenre));

        outPutList.sort(GenreComparators.compareGenreById);
        return outPutList;
    }

    @Override
    public List<MPA> getAllMPAs() {
        String sqlQuery = "SELECT ID, NAME from MPAs";
        List<MPA> outPutList = new ArrayList<>(jdbcTemplate.query(sqlQuery, this::mapRowToMPA));

        outPutList.sort(MPAComparators.compareMPAById);
        return outPutList;
    }

    @Override
    public Genre getGenreById(Long id) {
        String sqlQuery = "SELECT ID, NAME from GENRES " +
                "WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sqlQuery, this::mapRowToGenre, id);

        } catch (EmptyResultDataAccessException ex) {
            throw new RecordNotFoundException("Жанр с ID " + id + " не найден.");

        }
    }

    @Override
    public MPA getMPAById(Long id) {
        String sqlQuery = "SELECT ID, NAME from MPAs " +
                "WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sqlQuery, this::mapRowToMPA, id);

        } catch (EmptyResultDataAccessException ex) {
            throw new RecordNotFoundException("Рейтинг с ID " + id + " не найден.");

        }
    }

    @Override
    public Film addNew(Film film) {
        filmValidations(film);

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("FILMS")
                .usingGeneratedKeyColumns("id");
        Long filmRecordId = simpleJdbcInsert.executeAndReturnKey(film.toMap()).longValue();

        film.setId(filmRecordId);

        String sqlQuery =
                "INSERT INTO MPA_ASSIGNMENTS (FILM_ID, MPA_ID) " +
                        "VALUES (?, ?);";

        jdbcTemplate.update(sqlQuery, filmRecordId, film.getMpa().getId());

        setGenresForFilm(film);

        log.info("Новый фильм добавлен успешно. id:" + film.getId());

        return film;
    }

    @Override
    public Film change(Film film) {
        getFilmById(film.getId());
        filmValidations(film);
        String sqlQuery = "UPDATE FILMS SET NAME = ?, DESCRIPTION = ?, RELEASE_DATE = ?, DURATION_MIN = ? WHERE id = ?;";

        jdbcTemplate.update(sqlQuery,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getId());

        String sqlQueryMPAAssignment =
                "UPDATE MPA_ASSIGNMENTS SET MPA_ID = ? WHERE ID = (SELECT ID FROM MPA_ASSIGNMENTS WHERE FILM_ID = ?);";

        if (film.getMpa() != null) {
            jdbcTemplate.update(sqlQueryMPAAssignment,
                    film.getMpa().getId(),
                    film.getId());
        }

        setGenresForFilm(film);

        log.info("Запись фильма изменена успешно. id:" + film.getId());
        return film;
    }

    @Override
    public Film getFilmById(Long id) {
        String sqlQuery = "SELECT f.ID AS \"FILM_ID\", f.NAME, f.DESCRIPTION, f.RELEASE_DATE, " +
                "f.DURATION_MIN, m.ID AS \"MPA_ID\" , m.NAME AS \"MPA_NAME\" " +
                "from FILMS f " +
                "LEFT JOIN MPA_ASSIGNMENTS ma ON ma.FILM_ID = f.ID " +
                "LEFT JOIN MPAS m ON m.ID = ma.MPA_ID " +
                "WHERE f.id = ?";
        try {
            Film film = jdbcTemplate.queryForObject(sqlQuery, this::mapRowToFilm, id);
            film.setGenres(getGenresForFilm(film));

            return film;

        } catch (EmptyResultDataAccessException ex) {
            throw new RecordNotFoundException("Фильм с ID " + id + " не найден.");

        }
    }

    @Override
    public LocalDate getLowThresholdDate() {
        return LOW_THRESHOLD_DATE;
    }

    @Override
    public Set<Long> getLikeListForFilm(Long filmId) {
        Set<Long> usersWithLikes = new HashSet<>();
        String sqlQuery = "SELECT USER_ID FROM FILM_LIKES WHERE FILM_ID = ?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery, filmId);

        while (userRows.next()) {
            usersWithLikes.add(userRows.getLong("USER_ID"));

        }

        return usersWithLikes;
    }

    @Override
    public void createLikeFilmByUser(Long filmId, Long userId) {
        try {
            getFilmById(filmId);

            if (getLikeRecordMadeByUser(filmId, userId) < 0) {
                String sqlQuery =
                        "INSERT INTO FILM_LIKES (FILM_ID, USER_ID) " +
                                "VALUES (?, ?);";

                jdbcTemplate.update(sqlQuery, filmId, userId);
            }

        } catch (EmptyResultDataAccessException e) {
            throw new RecordNotFoundException("Фильм с ID " + filmId + " не найден.");
        }
    }

    @Override
    public void deleteLikeFilmByUser(Long filmId, Long userId) {
        String sqlQuery =
                "DELETE FROM FILM_LIKES WHERE FILM_ID = ? AND USER_ID =?";

        jdbcTemplate.update(sqlQuery, filmId, userId);
    }

    @Override
    public int getFilmLikeStorageCount(Long filmId) {
        String sqlQuery = "SELECT COUNT(DISTINCT ID) FROM FILM_LIKES WHERE FILM_ID = ?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery, filmId);

        if (userRows.next()) {
            return userRows.getInt("COUNT(DISTINCT ID)");
        }

        return -1;
    }

    public Long getLikeRecordMadeByUser(Long filmId, Long userId) {
        String sqlQuery = "SELECT ID FROM FILM_LIKES WHERE FILM_ID = ? AND USER_ID = ?";
        try {
            return jdbcTemplate.queryForObject(sqlQuery, Long.class, filmId, userId);

        } catch (Exception e) {
            return -1L;
        }

    }

    private Film mapRowToFilm(ResultSet resultSet, int rowNum) throws SQLException {
        return Film.builder()
                .id(resultSet.getLong("FILM_ID"))
                .name(resultSet.getString("name"))
                .description(resultSet.getString("description"))
                .releaseDate(resultSet.getDate("release_date").toLocalDate())
                .duration(resultSet.getInt("duration_min"))
                .mpa(MPA.builder()
                        .id(resultSet.getLong("MPA_ID"))
                        .name(resultSet.getString("MPA_NAME")).build())
                .build();
    }

    private Genre mapRowToGenre(ResultSet resultSet, int rowNum) throws SQLException {
        return Genre.builder()
                .id(resultSet.getLong("ID"))
                .name(resultSet.getString("NAME"))
                .build();
    }

    private MPA mapRowToMPA(ResultSet resultSet, int rowNum) throws SQLException {
        return MPA.builder()
                .id(resultSet.getLong("ID"))
                .name(resultSet.getString("NAME"))
                .build();
    }

    private void filmValidations(Film film) throws FilmValidationException {
        if (film.getName() == null || film.getName().isEmpty()) {
            throw new FilmValidationException("Название фильма не может быть пустым");
        }

        if (film.getDescription() != null) {
            if (film.getDescription().length() >= DESCRIPTION_LENGTH) {
                throw new FilmValidationException("Описание фильма не может быть длиннее 200 символов");
            }

        }

        if (film.getReleaseDate() != null) {
            if (film.getReleaseDate().isBefore(LOW_THRESHOLD_DATE)) {
                throw new FilmValidationException("Дата релиза не может быть раньше " + LOW_THRESHOLD_DATE.toString());
            }

        }

        if (film.getDuration() != null) {
            if (film.getDuration() < 0) {
                throw new FilmValidationException("Продолжительность фильма не может быть отрицательной");
            }

        }

    }

    private List<Long> setGenresForFilm(Film film) {
        List<Long> responseRecordIds = new ArrayList<>();

        if (film.getGenres() != null) {
            clearGenresForFilm(film.getId());
            film.setGenres(film.getGenres());
            if (film.getGenres().size() > 0) {
                film.getGenres().forEach(genre -> {
                    Map<String, Object> genreAssignment = new HashMap<>();
                    genreAssignment.put("FILM_ID", film.getId());
                    genreAssignment.put("GENRE_ID", genre != null ? genre.getId() : null);

                    SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                            .withTableName("GENRE_ASSIGNMENTS")
                            .usingGeneratedKeyColumns("ID");

                    responseRecordIds.add(simpleJdbcInsert.executeAndReturnKey(genreAssignment).longValue());

                });

            }
        }


        return responseRecordIds;
    }

    private List<Genre> getGenresForFilm(Film film) {
        Set<Genre> genreSet = new HashSet<>();

        String sqlQuery = "SELECT g.ID, g.NAME " +
                "FROM GENRES g " +
                "LEFT JOIN GENRE_ASSIGNMENTS ga ON ga.GENRE_ID = g.ID " +
                "LEFT JOIN FILMS f  ON ga.FILM_ID  = f.ID " +
                "WHERE f.ID = ?";

        SqlRowSet filmRows = jdbcTemplate.queryForRowSet(sqlQuery, film.getId());

        while (filmRows.next()) {
            genreSet.add(Genre
                    .builder()
                    .id(filmRows.getLong("ID"))
                    .name(filmRows.getString("NAME"))
                    .build()
            );
        }

        return new ArrayList<>(genreSet);
    }

    private void clearGenresForFilm(Long filmId) {
        String sqlQuery =
                "DELETE FROM GENRE_ASSIGNMENTS WHERE FILM_ID = ?";

        jdbcTemplate.update(sqlQuery, filmId);
    }

}
