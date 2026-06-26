package Common.auth;

import Common.config.AppConfig;
import DTOs.UserDTO;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import java.time.Instant;

public class JwtService {

    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    public JwtService() {
        this.algorithm = Algorithm.HMAC256(AppConfig.jwtSecret());
        this.verifier = JWT.require(algorithm)
                .withIssuer(AppConfig.jwtIssuer())
                .build();
    }

    public String generateToken(UserDTO user) {
        Instant now = Instant.now();
        return JWT.create()
                .withIssuer(AppConfig.jwtIssuer())
                .withSubject(user.id())
                .withClaim("email", user.email())
                .withClaim("name", user.fullName())
                .withIssuedAt(now)
                .withExpiresAt(now.plusSeconds(60L * 60L * 12L))
                .sign(algorithm);
    }

    public DecodedJWT verify(String token) {
        try {
            return verifier.verify(token);
        } catch (JWTVerificationException exception) {
            throw new IllegalArgumentException("Token invalido", exception);
        }
    }
}
