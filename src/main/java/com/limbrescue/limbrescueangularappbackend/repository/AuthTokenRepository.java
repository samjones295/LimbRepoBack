package com.limbrescue.limbrescueangularappbackend.repository;

import java.util.Optional;
import java.sql.Timestamp;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import com.limbrescue.limbrescueangularappbackend.models.AuthToken;

@Repository
public interface AuthTokenRepository extends JpaRepository<AuthToken, Integer> {
  Optional<AuthToken> findById(int id);

  @Modifying
  @Query(value = "INSERT INTO auth_token (uuid, userid, token, exp, createdat) SELECT :uuid, id, :accessToken, :expiryDate, :createdat FROM user WHERE username = :username", nativeQuery = true)
  public void insertToken(String uuid, String accessToken, Timestamp expiryDate, Timestamp createdat, String username);
}