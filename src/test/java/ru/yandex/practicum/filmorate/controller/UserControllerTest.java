package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.UserValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

class UserControllerTest {
    private UserController userController;
    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private User user;
    private User existingUser;
    private Long existingUserId;

    @BeforeEach
    void setUp() {
        UserStorage userStorage = new InMemoryUserStorage();
        UserService userService = new UserService(userStorage);
        userController = new UserController(userStorage, userService);
        user = User.builder()
                .email("mail@mail.ru")
                .login("dolore")
                .name("Nick Name")
                .birthday(LocalDate.parse("1946-08-20", dateTimeFormatter))
                .build();

        existingUser = User.builder()
                .email("312mail@mail.ru")
                .login("instantCoffeer")
                .name("Coco Chanelle")
                .birthday(LocalDate.parse("1966-08-20", dateTimeFormatter))
                .build();

        userController.addNew(existingUser);
        existingUserId = userController.getAll()
                .stream().filter(user1 -> user1.equals(existingUser)).findFirst().get().getId();

    }

    @Test
    void checkaddNewValidUser() {
        userController.addNew(user);
        Assertions.assertTrue(userController.getAll().contains(user));
    }

    @Test
    void checkaddUserWithEmptyLogin() {
        user.setLogin("");
        Exception exception = Assertions.assertThrows(UserValidationException.class,
                () -> userController.addNew(user));
        Assertions.assertEquals("Логин не может быть пустым", exception.getMessage());
        Assertions.assertFalse(userController.getAll().contains(user));

        user.setLogin(null);
        exception = Assertions.assertThrows(UserValidationException.class,
                () -> userController.addNew(user));
        Assertions.assertEquals("Логин не может быть пустым", exception.getMessage());
        Assertions.assertFalse(userController.getAll().contains(user));

        user.setLogin("Login with whitespaces");
        exception = Assertions.assertThrows(UserValidationException.class,
                () -> userController.addNew(user));
        Assertions.assertEquals("Логин не может содержать пробелы", exception.getMessage());
        Assertions.assertFalse(userController.getAll().contains(user));

    }

    @Test
    void checkaddUserWithWrongEmail() {
        user.setEmail(null);

        Exception exception = Assertions.assertThrows(UserValidationException.class,
                () -> userController.addNew(user));
        Assertions.assertEquals("Почта не может быть пустой", exception.getMessage());
        Assertions.assertFalse(userController.getAll().contains(user));

        user.setEmail("");

        exception = Assertions.assertThrows(UserValidationException.class,
                () -> userController.addNew(user));
        Assertions.assertEquals("Почта не может быть пустой", exception.getMessage());
        Assertions.assertFalse(userController.getAll().contains(user));

        user.setEmail("asd");

        exception = Assertions.assertThrows(UserValidationException.class,
                () -> userController.addNew(user));
        Assertions.assertEquals("Некорректный формат почты", exception.getMessage());
        Assertions.assertFalse(userController.getAll().contains(user));

    }

    @Test
    void checkaddUserWithWrongBirthdate() {
        user.setBirthday(LocalDate.parse("2099-02-01", dateTimeFormatter));

        Exception exception = Assertions.assertThrows(UserValidationException.class,
                () -> userController.addNew(user));

        Assertions.assertEquals("Дата рождения не может быть в будущем", exception.getMessage());
        Assertions.assertFalse(userController.getAll().contains(user));
    }

    @Test
    void checkaddUserWithEmptyName() {
        user.setName("");
        userController.addNew(user);
        Assertions.assertEquals(user.getName(), user.getLogin());
        Assertions.assertTrue(userController.getAll().contains(user));
    }

    @Test
    void checkModifyUserCorrectly() {
        String newName = "NewName1";
        String newLogin = "NewLogin";
        String newEmail = "newemail@mail.com";
        LocalDate newBirthDate = LocalDate.parse("1990-12-02", dateTimeFormatter);

        userController.change(User.builder()
                .id(existingUserId)
                .email(newEmail)
                .login(newLogin)
                .name(newName)
                .birthday(newBirthDate)
                .build()
        );

        Assertions.assertTrue(newName.equals(existingUser.getName()) &&
                newLogin.equals(existingUser.getLogin()) &&
                newEmail.equals(existingUser.getEmail()) &&
                newBirthDate.equals(existingUser.getBirthday()));
    }

    @Test
    void checkModifyLoginWrongly() {
        final String finalNewLogin = "";
        Exception exception = Assertions.assertThrows(UserValidationException.class,
                () -> userController.change(User.builder()
                        .id(existingUserId)
                        .email(existingUser.getEmail())
                        .login(finalNewLogin)
                        .name(existingUser.getName())
                        .birthday(existingUser.getBirthday())
                        .build()
                )
        );

        Assertions.assertEquals("Логин не может быть пустым", exception.getMessage());
        Assertions.assertNotEquals(finalNewLogin, existingUser.getLogin());

        final String finalNewLogin2 = null;
        exception = Assertions.assertThrows(UserValidationException.class,
                () -> userController.change(User.builder()
                        .id(existingUserId)
                        .email(existingUser.getEmail())
                        .login(finalNewLogin2)
                        .name(existingUser.getName())
                        .birthday(existingUser.getBirthday())
                        .build()
                )
        );

        Assertions.assertEquals("Логин не может быть пустым", exception.getMessage());
        Assertions.assertNotEquals(finalNewLogin2, existingUser.getLogin());

        final String finalNewLogin3 = "Login whitespace";

        exception = Assertions.assertThrows(UserValidationException.class,
                () -> userController.change(User.builder()
                        .id(existingUserId)
                        .email(existingUser.getEmail())
                        .login(finalNewLogin3)
                        .name(existingUser.getName())
                        .birthday(existingUser.getBirthday())
                        .build()
                )
        );

        Assertions.assertEquals("Логин не может содержать пробелы", exception.getMessage());
        Assertions.assertNotEquals(finalNewLogin3, existingUser.getLogin());

    }

    @Test
    void checkModifyUserWithWrongBirthdate() {
        final LocalDate finalDate = LocalDate.parse("2099-02-01", dateTimeFormatter);
        Exception exception = Assertions.assertThrows(UserValidationException.class,
                () -> userController.change(User.builder()
                        .id(existingUserId)
                        .email(existingUser.getEmail())
                        .login(existingUser.getLogin())
                        .name(existingUser.getName())
                        .birthday(finalDate)
                        .build()
                )
        );

        Assertions.assertEquals("Дата рождения не может быть в будущем", exception.getMessage());
        Assertions.assertNotEquals(finalDate, existingUser.getBirthday());

    }

    @Test
    void checkModifyUserWithWrongEmail() {
        final String newFinalEmail = "";
        Exception exception = Assertions.assertThrows(UserValidationException.class,
                () -> userController.change(User.builder()
                        .id(existingUserId)
                        .email(newFinalEmail)
                        .login(existingUser.getLogin())
                        .name(existingUser.getName())
                        .birthday(existingUser.getBirthday())
                        .build()
                )
        );

        Assertions.assertEquals("Почта не может быть пустой", exception.getMessage());
        Assertions.assertNotEquals(newFinalEmail, existingUser.getBirthday());

        final String newFinalEmail2 = null;
        exception = Assertions.assertThrows(UserValidationException.class,
                () -> userController.change(User.builder()
                        .id(existingUserId)
                        .email(newFinalEmail2)
                        .login(existingUser.getLogin())
                        .name(existingUser.getName())
                        .birthday(existingUser.getBirthday())
                        .build()
                )
        );

        Assertions.assertEquals("Почта не может быть пустой", exception.getMessage());
        Assertions.assertNotEquals(newFinalEmail2, existingUser.getBirthday());

        final String newFinalEmail3 = "email.com";
        exception = Assertions.assertThrows(UserValidationException.class,
                () -> userController.change(User.builder()
                        .id(existingUserId)
                        .email(newFinalEmail3)
                        .login(existingUser.getLogin())
                        .name(existingUser.getName())
                        .birthday(existingUser.getBirthday())
                        .build()
                )
        );

        Assertions.assertEquals("Некорректный формат почты", exception.getMessage());
        Assertions.assertNotEquals(newFinalEmail3, existingUser.getBirthday());

    }

}