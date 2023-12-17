package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.RecordNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component("UserDbStorage")
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Set<User> getAll() {
        String sqlQuery = "SELECT id, login, name, birthday, email FROM USERS";

        return new HashSet<>(jdbcTemplate.query(sqlQuery, this::mapRowToUser));
    }

    @Override
    public User addNew(User user) {
        userValidations(user);
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("USERS")
                .usingGeneratedKeyColumns("id");
        Long userRecordId = simpleJdbcInsert.executeAndReturnKey(user.toMap()).longValue();

        user.setId(userRecordId);

        if (getUserFriendlistId(user.getId()) < 0) {
            createUserFriendList(user.getId());
        }

        log.info("Новый пользователь добавлен успешно. id:" + user.getId());

        return user;
    }

    @Override
    public User change(User user) {
        getUserById(user.getId());
        userValidations(user);
        String sqlQuery = "UPDATE users SET login = ?, name = ?, email = ?, birthday = ? WHERE id = ?;";

        jdbcTemplate.update(sqlQuery,
                user.getLogin(),
                user.getName(),
                user.getEmail(),
                user.getBirthday(),
                user.getId());

        log.info("Запись пользователя изменена успешно. id:" + user.getId());
        return user;
    }

    @Override
    public User getUserById(Long id) {
        String sqlQuery = "select id, login, name, birthday, email from USERS where id = ?";

        try {
            return jdbcTemplate.queryForObject(sqlQuery, this::mapRowToUser, id);

        } catch (EmptyResultDataAccessException ex) {
            throw new RecordNotFoundException("Пользователь с ID " + id + " не найден.");

        }
    }

    @Override
    public void addToFriends(User userFirst, User userSecond) {
        createUserFriendListEntry(userFirst.getId(), userSecond.getId());

    }

    @Override
    public void removeFromFriends(User userFirst, User userSecond) {
        deleteUserFriendlistEntry(userFirst.getId(), userSecond.getId());
    }

    @Override
    public List<User> getUserFriendList(Long userId) {
        List<User> userList = new ArrayList<>();

        String sqlQuery = "SELECT u.ID, u.EMAIL, u.LOGIN, u.NAME, u.BIRTHDAY FROM USERS u " +
                "INNER JOIN USER_FRIENDLIST_ENTRIES ufe ON ufe.ASSOCIATED_USER_ID = u.ID " +
                "INNER JOIN USER_FRIENDLISTS uf ON ufe.FRIENDLIST_ID = uf.ID " +
                "WHERE uf.USER_ID = ?";

        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery, userId);

        while (userRows.next()) {
            userList.add(User
                    .builder()
                    .id(userRows.getLong("ID"))
                    .email(userRows.getString("EMAIL"))
                    .login(userRows.getString("LOGIN"))
                    .name(userRows.getString("NAME"))
                    .birthday(userRows.getDate("BIRTHDAY").toLocalDate())
                    .build()
            );
        }

        return userList;
    }

    @Override
    public boolean existingUser(Long id) throws RecordNotFoundException {
        String sqlQuery = "SELECT EXISTS(select ID from USERS where id = ?)";
        if (Boolean.TRUE
                .equals(jdbcTemplate.queryForObject(sqlQuery, new Long[]{id}, Boolean.class))) {
            return true;

        } else {
            throw new RecordNotFoundException("Пользователь с ID " + id + " не найден.");
        }

    }

    private User mapRowToUser(ResultSet resultSet, int rowNum) throws SQLException {
        return User.builder()
                .id(resultSet.getLong("id"))
                .email(resultSet.getString("email"))
                .login(resultSet.getString("login"))
                .name(resultSet.getString("name"))
                .birthday(resultSet.getDate("birthday").toLocalDate())
                .build();
    }

    private void userValidations(User user) {
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

    private int getUserFriendlistId(Long userId) {
        String sqlQuery = "SELECT ID FROM USER_FRIENDLISTS WHERE USER_ID = ?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery, userId);

        if (userRows.next()) {
            return userRows.getInt("id");
        }

        return -1;
    }

    private Long createUserFriendList(Long userId) {
        Map<String, Object> friendlistMap = new HashMap<>();
        friendlistMap.put("USER_ID", userId);

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("USER_FRIENDLISTS")
                .usingGeneratedKeyColumns("id");

        return simpleJdbcInsert.executeAndReturnKey(friendlistMap).longValue();
    }

    private long createUserFriendListEntry(Long userId, Long associatedUserId) {
        long userFriendListEntryId = getUserFriendListEntry(userId, associatedUserId);


        if (userFriendListEntryId < 0) {
            long reversedUserFriendListEntryId = getUserFriendListEntry(associatedUserId, userId);
            String friendShipStatus = "Неподтвердженная";

            if (reversedUserFriendListEntryId > 0) {
                friendShipStatus = "Подтвержденная";
            }

            long userFriendListid = getUserFriendlistId(userId);

            if (userFriendListid < 0) {
                userFriendListid = createUserFriendList(userId);
            }

            Map<String, Object> friendlistEntryMap = new HashMap<>();
            friendlistEntryMap.put("FRIENDLIST_ID", userFriendListid);
            friendlistEntryMap.put("ASSOCIATED_USER_ID", associatedUserId);
            friendlistEntryMap.put("STATUS", friendShipStatus);

            SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                    .withTableName("USER_FRIENDLIST_ENTRIES")
                    .usingGeneratedKeyColumns("id");

            Long friendListEntry = simpleJdbcInsert.executeAndReturnKey(friendlistEntryMap).longValue();
            if (friendListEntry > 0) {
                updateUserFriendListEntry(reversedUserFriendListEntryId, friendShipStatus);
                return friendListEntry;
            }

        }

        return -1L;

    }

    private void updateUserFriendListEntry(Long userFriendListEntryId, String status) {
        String sqlQuery = "UPDATE USER_FRIENDLIST_ENTRIES SET STATUS = ? WHERE id = ?;";

        jdbcTemplate.update(sqlQuery,
                status,
                userFriendListEntryId);
    }

    private void deleteUserFriendlistEntry(Long userId, Long associatedUserId) {
        long userFriendListEntryId = getUserFriendListEntry(userId, associatedUserId);

        if (userFriendListEntryId > 0) {
            long userFriendListid = getUserFriendlistId(userId);

            if (userFriendListid < 0) {
                userFriendListid = createUserFriendList(userId);

            }

            String sqlQueryForFriendList =
                    "DELETE FROM USER_FRIENDLIST_ENTRIES ufe1 WHERE ufe1.ID = " +
                            "(SELECT ufe.ID FROM USER_FRIENDLIST_ENTRIES ufe " +
                            "INNER JOIN USER_FRIENDLISTS uf ON ufe.FRIENDLIST_ID = uf.ID " +
                            "INNER JOIN USERS u ON uf.USER_ID = u.ID " +
                            "WHERE u.ID = ? AND ufe.ASSOCIATED_USER_ID = ?);";

            jdbcTemplate.update(sqlQueryForFriendList,
                    userFriendListid, associatedUserId);
        }

    }

    private long getUserFriendListEntry(Long userId, Long associatedUserId) {
        String sqlQuery = "SELECT ufe.ID FROM USER_FRIENDLIST_ENTRIES ufe " +
                "INNER JOIN USER_FRIENDLISTS uf ON ufe.FRIENDLIST_ID = uf.ID " +
                "INNER JOIN USERS u ON uf.USER_ID = u.ID " +
                "WHERE u.ID = ? AND ufe.ASSOCIATED_USER_ID = ?";

        try {
            return jdbcTemplate.queryForObject(sqlQuery, Long.class, userId, associatedUserId);

        } catch (Exception e) {
            return -1;
        }

    }

}