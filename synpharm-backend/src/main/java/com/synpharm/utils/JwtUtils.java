package com.synpharm.utils;

import com.synpharm.exception.BusinessException;
import com.synpharm.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * JWT工具类
 *
 * <p>提供JWT令牌的生成、解析和验证功能。
 * 验证失败会抛出具体的业务异常，便于前端区分Token过期/无效等不同场景。
 *
 * @author SynPharm Team
 * @version 2.0.0
 */
@Slf4j
@Component
public class JwtUtils {

    /** JWT签名密钥 */
    @Value("${jwt.secret}")
    private String secret;

    /** JWT过期时间（毫秒） */
    @Getter
    @Value("${jwt.expiration}")
    private long expiration;

    /** 缓存的签名密钥（初始化时创建一次，避免重复生成） */
    private SecretKey signingKey;

    /** HS256要求密钥至少32字节（256位） */
    private static final int MIN_KEY_LENGTH = 32;

    /**
     * 初始化签名密钥
     * <p>在Bean创建后执行，校验密钥长度并缓存SecretKey对象。
     *
     * @throws IllegalStateException 密钥长度不足32字节时抛出
     */
    @PostConstruct
    public void init() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < MIN_KEY_LENGTH) {
            throw new IllegalStateException(
                    "JWT密钥长度不足，HS256要求至少" + MIN_KEY_LENGTH + "字节（当前" + keyBytes.length + "字节）");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        log.info("JWT签名密钥初始化完成");
    }

    /**
     * 获取签名密钥（返回缓存的实例）
     *
     * @return SecretKey 签名密钥
     */
    private SecretKey getSigningKey() {
        return signingKey;
    }

    /**
     * 生成JWT令牌
     *
     * <p>Token中包含 jti（JWT ID）声明，用于唯一标识每个Token，
     * 可用于黑名单机制，比使用完整Token作为Key更节省Redis内存。
     *
     * @param userId 用户ID
     * @param email  用户邮箱
     * @param role   用户角色
     * @return JWT令牌
     */
    public String generateToken(Long userId, String email, String role) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expiration);
        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .id(jti)
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expireDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 解析JWT令牌
     *
     * @param token JWT令牌
     * @return Claims 声明信息
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 验证JWT令牌有效性
     * <p>验证失败会抛出具体的业务异常（TOKEN_EXPIRED/TOKEN_INVALID/SIGNATURE_INVALID），
     * 便于前端区分错误类型。
     *
     * @param token JWT令牌
     * @return 是否有效
     * @throws BusinessException Token过期或无效时抛出
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Token已过期");
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        } catch (UnsupportedJwtException | MalformedJwtException e) {
            log.warn("Token格式错误: {}", e.getMessage());
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        } catch (SignatureException e) {
            log.warn("Token签名验证失败");
            throw new BusinessException(ErrorCode.SIGNATURE_INVALID);
        } catch (IllegalArgumentException e) {
            log.warn("Token参数为空");
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        } catch (JwtException e) {
            log.warn("Token验证失败: {}", e.getMessage());
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
    }

    /**
     * 从令牌中获取用户ID
     *
     * @param token JWT令牌
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 从令牌中获取用户邮箱
     *
     * @param token JWT令牌
     * @return 用户邮箱
     */
    public String getEmailFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.get("email", String.class);
    }

    /**
     * 从令牌中获取用户角色
     *
     * @param token JWT令牌
     * @return 用户角色
     */
    public String getRoleFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.get("role", String.class);
    }

    /**
     * 从令牌中获取 jti（JWT ID）
     *
     * <p>jti 是每个Token的唯一标识，用于黑名单机制。
     *
     * @param token JWT令牌
     * @return Token唯一ID，解析失败返回null
     */
    public String getJtiFromToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getId();
        } catch (Exception e) {
            log.warn("获取Token jti失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取Token剩余有效时间（毫秒）
     * <p>用于登出时设置黑名单的TTL。
     *
     * @param token JWT令牌
     * @return 剩余毫秒数，Token无效返回0
     */
    public long getRemainingTime(String token) {
        try {
            Claims claims = parseClaims(token);
            Date expirationDate = claims.getExpiration();
            return Math.max(0, expirationDate.getTime() - System.currentTimeMillis());
        } catch (Exception e) {
            log.warn("获取Token剩余时间失败: {}", e.getMessage());
            return 0;
        }
    }
}
