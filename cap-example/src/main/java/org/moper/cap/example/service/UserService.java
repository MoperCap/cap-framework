package org.moper.cap.example.service;

import lombok.extern.slf4j.Slf4j;
import org.moper.cap.bean.annotation.Capper;
import org.moper.cap.example.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Capper
public class UserService {

    private final Map<Long, User> userStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    public UserService() {
        log.info("UserService 初始化");
        createUser(new User("Alice", "alice@example.com"));
        createUser(new User("Bob", "bob@example.com"));
        createUser(new User("Charlie", "charlie@example.com"));
    }

    public User getUserById(long id) {
        log.debug("获取用户: {}", id);
        return userStore.get(id);
    }

    public List<User> getAllUsers() {
        log.debug("获取所有用户");
        return new ArrayList<>(userStore.values());
    }

    public User createUser(User user) {
        long id = idGenerator.incrementAndGet();
        user.setId(id);
        userStore.put(id, user);
        log.info("创建用户: id={}, name={}", id, user.getName());
        return user;
    }

    public User updateUser(long id, User user) {
        user.setId(id);
        userStore.put(id, user);
        log.info("更新用户: id={}", id);
        return user;
    }

    public boolean deleteUser(long id) {
        boolean removed = userStore.remove(id) != null;
        if (removed) {
            log.info("删除用户: id={}", id);
        }
        return removed;
    }

    public boolean userExists(long id) {
        return userStore.containsKey(id);
    }
}

