package org.moper.cap.example.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.moper.cap.example.model.ApiResponse;
import org.moper.cap.example.model.User;
import org.moper.cap.example.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserControllerTest {

    private UserController controller;
    private UserService service;

    @BeforeEach
    public void setUp() {
        service = new UserService();
        controller = new UserController();
        controller.userService = service;
    }

    @Test
    public void testGetAllUsers() {
        ApiResponse<List<User>> response = controller.getAllUsers();

        assertNotNull(response);
        assertEquals(0, response.getCode());
        assertNotNull(response.getData());
        assertTrue(response.getData().size() >= 3);
    }

    @Test
    public void testGetUserById_Found() {
        ApiResponse<User> response = controller.getUserById(1);

        assertNotNull(response);
        assertEquals(0, response.getCode());
        assertNotNull(response.getData());
        assertEquals("Alice", response.getData().getName());
    }

    @Test
    public void testGetUserById_NotFound() {
        ApiResponse<User> response = controller.getUserById(999);

        assertNotNull(response);
        assertEquals(1, response.getCode());
        assertEquals("用户不存在", response.getMessage());
    }

    @Test
    public void testCreateUser() {
        User newUser = new User("Edward", "edward@example.com");
        ApiResponse<User> response = controller.createUser(newUser);

        assertNotNull(response);
        assertEquals(0, response.getCode());
        assertNotNull(response.getData());
        assertNotNull(response.getData().getId());
        assertEquals("Edward", response.getData().getName());
    }

    @Test
    public void testUpdateUser() {
        User updateData = new User("NewAlice", "newalice@example.com");
        ApiResponse<User> response = controller.updateUser(1, updateData);

        assertNotNull(response);
        assertEquals(0, response.getCode());
        assertEquals("NewAlice", response.getData().getName());
    }

    @Test
    public void testDeleteUser_Success() {
        ApiResponse<Void> response = controller.deleteUser(1);

        assertNotNull(response);
        assertEquals(0, response.getCode());
        assertNull(service.getUserById(1));
    }

    @Test
    public void testDeleteUser_NotFound() {
        ApiResponse<Void> response = controller.deleteUser(999);

        assertNotNull(response);
        assertEquals(1, response.getCode());
    }
}
