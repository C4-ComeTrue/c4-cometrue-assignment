package org.c4marathon.assignment.auth.jwt;

import java.util.Date;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JwtTokenUtil {

    private static final String CLAIM_KEY_MEMBER_ID = "memberId";

    // JWT Token 발급
    public static String createToken(Long memberId, String key, long expireTimeMs) {
        // Claim = Jwt Token에 들어갈 정보
        Claims claims = Jwts.claims();
        claims.put(CLAIM_KEY_MEMBER_ID, memberId);

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + expireTimeMs))
            .signWith(SignatureAlgorithm.HS256, key)
            .compact();
    }

    // Claims에서 memberEmail 꺼내기
    public static Long getMemberEmail(String token, String secretKey) {
        return (Long)extractClaims(token, secretKey).get(CLAIM_KEY_MEMBER_ID);
    }

    // 발급된 Token이 만료 시간이 지났는지 체크
    public static boolean isExpired(String token, String secretKey) {
        Date expiredDate = extractClaims(token, secretKey).getExpiration();
        // Token의 만료 날짜가 지금보다 이전인지 check
        return expiredDate.before(new Date());
    }

    // SecretKey를 사용해 Token Parsing
    private static Claims extractClaims(String token, String secretKey) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
    }
}