package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.RecordNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {
    private final JdbcTemplate jdbcTemplate;
    private UserDbStorage userDbStorage;
    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    public void setUp() {
        userDbStorage = new UserDbStorage(jdbcTemplate);
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
    public void checkNewUserWasAdded() {
        userDbStorage.addNew(user1);

        assertTrue(userDbStorage.getAll().contains(user1));
    }

    @Test
    public void checkUserChangedWasAdded() {
        userDbStorage.addNew(user1);
        Set<User> users = userDbStorage.getAll();
        User userFromDb = users.stream()
                .filter(user -> user.equals(user1))
                .findFirst().orElse(null);

        assertNotNull(userFromDb);

        Long id = userFromDb.getId();
        String newUserLogin = user1.getLogin() + "testlogin!";
        String newUserName = user1.getName() + "testname!";
        String newUserEmail = user1.getEmail() + "emailtest!";
        LocalDate newUserBday = user1.getBirthday().plusMonths(1);

        User user2 = User.builder()
                .id(id)
                .login(newUserLogin)
                .name(newUserName)
                .email(newUserEmail)
                .birthday(newUserBday)
                .build();

        userDbStorage.change(user2);

        assertTrue(userDbStorage.getAll().contains(user2));
        assertFalse(userDbStorage.getAll().contains(user1));
    }

    @Test
    public void checkGettingUserById() {
        userDbStorage.addNew(user1);
        Set<User> users = userDbStorage.getAll();
        User userFromDb = users.stream()
                .filter(user -> user.equals(user1))
                .findFirst().orElse(null);

        assertNotNull(userFromDb);

        Long id = userFromDb.getId();

        assertEquals(userDbStorage.getUserById(id), user1);
    }

    @Test
    public void checkMakingFriends() {
        userDbStorage.addNew(user1);
        userDbStorage.addNew(user2);
        userDbStorage.addNew(user3);

        assertTrue(userDbStorage.getAll().contains(user1));
        assertTrue(userDbStorage.getAll().contains(user2));
        assertTrue(userDbStorage.getAll().contains(user3));

        User user1FromDb = userDbStorage.getAll().stream()
                .filter(user -> user.equals(user1))
                .findFirst().orElse(null);

        User user3FromDb = userDbStorage.getAll().stream()
                .filter(user -> user.equals(user3))
                .findFirst().orElse(null);

        assertNotNull(user1FromDb);
        assertNotNull(user3FromDb);

        Long user1Id = user1FromDb.getId();
        Long user3Id = user3FromDb.getId();

        userDbStorage.addToFriends(user1, user2);
        userDbStorage.addToFriends(user1, user3);
        userDbStorage.addToFriends(user3, user1);

        List<User> user1Friendlist = userDbStorage.getUserFriendList(user1Id);
        List<User> user2Friendlist = userDbStorage.getUserFriendList(user3Id);

        assertFalse(user1Friendlist.isEmpty());
        assertTrue(user1Friendlist.contains(user2));
        assertTrue(user1Friendlist.contains(user3));

        assertFalse(user2Friendlist.isEmpty());
        assertTrue(user2Friendlist.contains(user1));

    }

    @Test
    public void checkDeletingFriend() {
        userDbStorage.addNew(user1);
        userDbStorage.addNew(user2);

        assertTrue(userDbStorage.getAll().contains(user1));
        assertTrue(userDbStorage.getAll().contains(user2));

        User user1FromDb = userDbStorage.getAll().stream()
                .filter(user -> user.equals(user1))
                .findFirst().orElse(null);

        User user2FromDb = userDbStorage.getAll().stream()
                .filter(user -> user.equals(user2))
                .findFirst().orElse(null);

        assertNotNull(user1FromDb);
        assertNotNull(user2FromDb);

        Long user1Id = user1FromDb.getId();

        userDbStorage.addToFriends(user1, user2);

        assertTrue(userDbStorage.getUserFriendList(user1Id).contains(user2));

        userDbStorage.removeFromFriends(user1, user2);

        assertFalse(userDbStorage.getUserFriendList(user1Id).contains(user2));
    }

    @Test
    void checkUserNotExisting() {
        userDbStorage.addNew(user1);
        assertTrue(userDbStorage.getAll().contains(user1));

        User user1FromDb = userDbStorage.getAll().stream()
                .filter(user -> user.equals(user1))
                .findFirst().orElse(null);

        assertNotNull(user1FromDb);


        Exception exception = Assertions.assertThrows(RecordNotFoundException.class,
                () -> userDbStorage.getUserById(user1.getId() + 1));
        Assertions.assertEquals("Пользователь с ID " + (user1.getId() + 1) + " не найден.", exception.getMessage());

    }
}