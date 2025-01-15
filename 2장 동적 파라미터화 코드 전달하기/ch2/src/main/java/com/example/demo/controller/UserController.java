package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.UserFilter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.Arrays;
import java.util.List;

@RestController
public class UserController {
    // 필터링 조건을 동적으로 전달해주세요
    @GetMapping("/filtered-users")
    public List<User> getFilteredUsers() {
        List<User> users = Arrays.asList(
                new User("Alice", 25, "Admin"),
                new User("Bob", 30, "User"),
                new User("Charlie", 22, "Moderator")
        );

        // 동적 필터링 조건: 나이가 25 이상인 사용자

        return UserFilter.filterUsers(users, user -> user.getAge() >= 25);
    }
}
