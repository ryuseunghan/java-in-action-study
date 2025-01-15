package com.example.demo.service;

import com.example.demo.entity.User;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class UserFilter {

    public static List<User> filterUsers(List<User> users, Predicate<User> predicate) {
        return users.stream()
                .filter(predicate) // 조건에 따라 필터링
                .collect(Collectors.toList());
    }
}
