package org.moper.cap.example.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.moper.cap.example.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    private UserService service;

    @BeforeEach
    public void setUp() {
        service = new UserService();
    }

    @Test
    public void testCreateUser() {
        User user = new User("David", "david@example.com");
        User created = service.createUser(user);

        assertNotNull(created.getId());
        assertEquals("David", created.getName());
        assertEquals("david@example.com", created.getEmail());
    }

    @Test
    public void testGetUserById() {
        User user = service.getUserById(1);
        assertNotNull(user);
        assertEquals("Alice", user.getName());
    }

    @Test
    public void testGetUserById_NotFound() {
        User user = service.getUserById(999);
        assertNull(user);
    }

    @Test
    public void testGetAllUsers() {
        List<User> users = service.getAllUsers();
        assertTrue(users.size() >= 3);
    }

    @Test
    public void testUpdateUser() {
        User updated = new User("UpdatedName", "updated@example.com");
        service.updateUser(1, updated);

        User user = service.getUserById(1);
        assertEquals("UpdatedName", user.getName());
    }

    @Test
    public void testDeleteUser() {
        boolean deleted = service.deleteUser(1);
        assertTrue(deleted);

        User user = service.getUserById(1);
        assertNull(user);
    }

    @Test
    public void testUserExists() {
        assertTrue(service.userExists(1));
        assertFalse(service.userExists(999));
    }
}
