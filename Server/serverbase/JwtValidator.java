package Server.serverbase;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.Key;

public class JwtValidator {

    // Lê a chave secreta do .env
    private static final Key SECRET_KEY = Keys.hmacShaKeyFor(ServerConfig.JWT_SECRET.getBytes());

    public static String validate(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token).getBody();
        return claims.getSubject(); // userId
    }
}