package tn.esprit.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import tn.esprit.entity.Token;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Integer> {
    Optional<Token> findByToken(String token);
}