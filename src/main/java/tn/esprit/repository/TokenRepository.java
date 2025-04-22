package tn.esprit.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import tn.esprit.entity.token.Token;
import tn.esprit.entity.token.TokenTypes;
import tn.esprit.entity.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Integer> {
    Optional<Token> findByToken(String token);
    Optional<Token> findByTokenAndTokenType(String token, TokenTypes type);

    List<Token> findAllByUserAndTokenType(User user, TokenTypes type);
    List<Token> findAllByUserAndRevokedFalse(User user);
    List<Token> findAllByUserAndTokenTypeAndRevokedFalse(User user, TokenTypes type);
    void deleteByExpiresAtBefore(LocalDateTime expiryDate);
    List<Token> findAllByExpiresAtBefore(LocalDateTime expiryDate);
}