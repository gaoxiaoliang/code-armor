package com.code.armor.controller;

import com.code.armor.dto.UpdateUserRequest;
import com.code.armor.entity.User;
import com.code.armor.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public Map<String, Object> me(HttpServletRequest request, Authentication auth) {
        String email = auth.getName();
        String ip = request.getRemoteAddr();

        User user = userService.getByEmail(email);
        return Map.of(
                "username", user.getUsername(),
                "email", user.getEmail(),
                "ip", ip
        );
    }

    @PutMapping("/me")
    public Map<String, Object> updateMe(@RequestBody UpdateUserRequest req,
                                        Authentication auth) {
        User user = userService.updateUsername(auth.getName(), req.getUsername());
        return Map.of(
                "username", user.getUsername(),
                "email", user.getEmail()
        );
    }
}
