package com.synpharm.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * 登录响应DTO
 *
 * <p>用于返回登录成功后的响应数据，包含JWT令牌和用户信息。
 *
 * @author SynPharm Team
 * @version 2.0.0
 */
@Data
@Builder
public class LoginResponse {

    /** 访问令牌（JWT） */
    private String accessToken;

    /** Token类型，固定为 Bearer */
    private String tokenType;

    /** 有效期（秒） */
    private Long expiresIn;

    /** 是否是新注册的用户（前端可据此判断是否需要引导） */
    private Boolean isNewUser;

    /** 用户信息 */
    private UserResponse user;
}
