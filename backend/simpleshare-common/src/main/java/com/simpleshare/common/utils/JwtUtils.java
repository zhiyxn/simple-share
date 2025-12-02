package com.simpleshare.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 *
 * @author SimpleShare
 */
public class JwtUtils {
    
    /**
     * 令牌自定义标识
     */
    private static final String TOKEN_HEADER = "Authorization";
    
    /**
     * 令牌前缀
     */
    private static final String TOKEN_PREFIX = "Bearer ";
    
    /**
     * 令牌秘钥
     */
    private static final String SECRET = "abcdefuvwxyzABCDEFGHIJxDDXsssKLMNOPQRSTUVWXYZ0123456789";
    
    /**
     * 令牌有效期（默认30分钟）
     */
    private static final int EXPIRETIME = 30 * 60 * 1000;
    
    /**
     * 生成令牌
     *
     * @param claims 数据声明
     * @return 令牌
     */
    public static String createToken(Map<String, Object> claims) {
        return createToken(claims, SECRET, EXPIRETIME / (60 * 1000));
    }
    
    /**
     * 使用指定秘钥生成令牌（默认有效期）
     *
     * @param claims 数据声明
     * @param secret 秘钥
     * @return 令牌
     */
    public static String createToken(Map<String, Object> claims, String secret) {
        return createToken(claims, secret, EXPIRETIME / (60 * 1000));
    }
    
    /**
     * 使用指定秘钥和有效期生成令牌
     *
     * @param claims 数据声明
     * @param secret 秘钥
     * @param expireTime 有效期（分钟）
     * @return 令牌
     */
    public static String createToken(Map<String, Object> claims, String secret, int expireTime) {
        String keySecret = StringUtils.isNotEmpty(secret) ? secret : SECRET;
        String token = Jwts.builder()
                .setClaims(claims)
                .setExpiration(new Date(System.currentTimeMillis() + expireTime * 60L * 1000L))
                .signWith(SignatureAlgorithm.HS512, getSigningKey(keySecret))
                .compact();
        return token;
    }
    
    /**
     * 从数据声明生成令牌
     *
     * @param claims 数据声明
     * @return 令牌
     */
    public static String createToken(String subject) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", subject);
        return createToken(claims);
    }
    
    /**
     * 从令牌中获取数据声明
     *
     * @param token 令牌
     * @return 数据声明
     */
    public static Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey(SECRET))
                .parseClaimsJws(token)
                .getBody();
    }
    
    /**
     * 从令牌中获取数据声明（使用指定秘钥）
     *
     * @param token 令牌
     * @param secret 秘钥
     * @return 数据声明
     */
    public static Claims parseToken(String token, String secret) {
        String keySecret = StringUtils.isNotEmpty(secret) ? secret : SECRET;
        return Jwts.parser()
                .setSigningKey(getSigningKey(keySecret))
                .parseClaimsJws(token)
                .getBody();
    }
    
    /**
     * 根据令牌获取用户标识
     *
     * @param token 令牌
     * @return 用户ID
     */
    public static String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }
    
    /**
     * 验证令牌
     *
     * @param token 令牌
     * @return 是否有效
     */
    public static Boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            return !isTokenExpired(claims);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 验证令牌（使用指定秘钥）
     *
     * @param token 令牌
     * @param secret 秘钥
     * @return 是否有效
     */
    public static Boolean validateToken(String token, String secret) {
        try {
            Claims claims = parseToken(token, secret);
            return !isTokenExpired(claims);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 判断令牌是否过期
     *
     * @param claims 令牌
     * @return 是否过期
     */
    private static Boolean isTokenExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        return expiration.before(new Date());
    }
    
    /**
     * 获取请求token
     *
     * @param token
     * @return token
     */
    public static String getToken(String token) {
        if (StringUtils.isNotEmpty(token) && token.startsWith(TOKEN_PREFIX)) {
            token = token.replace(TOKEN_PREFIX, "");
        }
        return token;
    }
    
    /**
     * 获取密钥
     *
     * @return 密钥
     */
    private static String getSecretKey() {
        return SECRET;
    }
    
    /**
     * 构建签名密钥
     *
     * @param secret 秘钥字符串
     * @return Key
     */
    public static Key getSigningKey(String secret) {
        String keyMaterial = StringUtils.isNotEmpty(secret) ? secret : SECRET;
        byte[] keyBytes = keyMaterial.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 64) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-512");
                keyBytes = digest.digest(keyBytes);
            } catch (NoSuchAlgorithmException ignored) {
                keyBytes = Arrays.copyOf(keyBytes, 64);
            }
        }
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS512.getJcaName());
    }
}
