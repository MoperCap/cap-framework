package org.moper.cap.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.annotation.Capper;
import org.moper.cap.bean.annotation.Inject;
import org.moper.cap.example.model.ApiResponse;
import org.moper.cap.example.model.User;
import org.moper.cap.example.service.UserService;
import org.moper.cap.web.annotation.controller.RestController;
import org.moper.cap.web.annotation.mapping.DeleteMapping;
import org.moper.cap.web.annotation.mapping.GetMapping;
import org.moper.cap.web.annotation.mapping.PostMapping;
import org.moper.cap.web.annotation.mapping.PutMapping;
import org.moper.cap.web.annotation.mapping.RequestMapping;
import org.moper.cap.web.annotation.request.PathVariable;
import org.moper.cap.web.annotation.request.RequestBody;

import java.util.List;

@Slf4j
@Capper
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Inject
    UserService userService;

    /**
     * 获取所有用户
     * GET /api/users
     */
    @GetMapping
    public ApiResponse<List<User>> getAllUsers() {
        log.info("获取所有用户");
        List<User> users = userService.getAllUsers();
        return ApiResponse.success("获取用户列表成功", users);
    }

    /**
     * 获取指定用户
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ApiResponse<User> getUserById(@PathVariable("id") long id) {
        log.info("获取用户: id={}", id);
        User user = userService.getUserById(id);
        if (user == null) {
            return ApiResponse.error("用户不存在");
        }
        return ApiResponse.success("获取用户成功", user);
    }

    /**
     * 创建用户
     * POST /api/users
     */
    @PostMapping
    public ApiResponse<User> createUser(@RequestBody User user) {
        log.info("创建用户: name={}", user.getName());
        User created = userService.createUser(user);
        return ApiResponse.success("创建用户成功", created);
    }

    /**
     * 更新用户
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    public ApiResponse<User> updateUser(@PathVariable("id") long id, @RequestBody User user) {
        log.info("更新用户: id={}", id);
        User updated = userService.updateUser(id, user);
        return ApiResponse.success("更新用户成功", updated);
    }

    /**
     * 删除用户
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable("id") long id) {
        log.info("删除用户: id={}", id);
        boolean deleted = userService.deleteUser(id);
        if (!deleted) {
            return ApiResponse.error("用户不存在");
        }
        return ApiResponse.success("删除用户成功", null);
    }
}
