package com.be.server.infrastructure.constant;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum Role {

    ADMIN("ADMIN"),

    USERS("USERS");

    private final String nameInVietnamese;

    Role(String nameInVietnamese) {
        this.nameInVietnamese = nameInVietnamese;
    }

    // lấy tất cả vai trò mảng ["ADMIN", "QUAN_LY", "THANH_VIEN"]
    public static List<String> getAllRoles() {
        return Arrays.stream(Role.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    // lấy tất cả vai trò  chuỗi "ADMIN, QUAN_LY, THANH_VIEN"
    public static String getAllRolesString() {
        return Arrays.stream(Role.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }

    // lấy tên tiếng việt
    public static List<String> getAllRolesInVietnamese() {
        return Arrays.stream(Role.values())
                .map(Role::getNameInVietnamese)
                .collect(Collectors.toList());
    }

    // lấy tên tiếng việt từng vai trò
    public static String getVietnameseNameByRole(String roleName) {
        return Arrays.stream(Role.values())
                .filter(role -> role.name().equalsIgnoreCase(roleName))
                .map(Role::getNameInVietnamese)
                .findFirst()
                .orElse(null);
    }

}
