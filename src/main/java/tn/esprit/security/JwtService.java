package tn.esprit.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtService {
    private final String secretKey;
    private final long jwtExpirationMs;

    public JwtService(@Value("${spring.security.jwt.secret-key}") String secretKey,
                      @Value("${spring.security.jwt.expiration}") long jwtExpirationMs) {
        this.secretKey = secretKey;
        this.jwtExpirationMs = jwtExpirationMs;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails, Long userId) {
        return generateToken(new HashMap<>(), userDetails, userId);
    }

   /* public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            Long userId
    ) {
        // Add roles/authorities to claims
        extraClaims.put("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        // Add user ID to claims
        extraClaims.put("userId", userId);

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    */
   public String generateToken(
           Map<String, Object> extraClaims,
           UserDetails userDetails,
           Long userId
   ) {
       // Simplify roles - just collect role names without permissions
       extraClaims.put("roles", userDetails.getAuthorities().stream()
               .map(GrantedAuthority::getAuthority)
               .filter(authority -> authority.startsWith("ROLE_"))
               .collect(Collectors.toList()));

       // Add user ID to claims
       extraClaims.put("userId", userId);

       return Jwts.builder()
               .setClaims(extraClaims)
               .setSubject(userDetails.getUsername())
               .setIssuedAt(new Date(System.currentTimeMillis()))
               .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
               .signWith(getSignInKey(), SignatureAlgorithm.HS256)
               .compact();
   }
    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {

        throw new UnsupportedOperationException("User ID is required for token generation");
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}