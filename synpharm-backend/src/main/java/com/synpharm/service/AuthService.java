package com.synpharm.service;

import com.synpharm.dto.request.LoginRequest;
import com.synpharm.dto.request.RegisterRequest;
import com.synpharm.dto.response.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    LoginResponse login(LoginRequest request, HttpServletRequest httpRequest);

    LoginResponse register(RegisterRequest request, HttpServletRequest httpRequest);

    void resetPassword(String email, String captcha, String newPassword);

    void logout(String token);
}
