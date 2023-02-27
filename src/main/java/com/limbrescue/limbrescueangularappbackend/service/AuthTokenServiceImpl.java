package com.limbrescue.limbrescueangularappbackend.service;
import com.limbrescue.limbrescueangularappbackend.repository.AuthTokenRepository;
import com.limbrescue.limbrescueangularappbackend.models.AuthToken;
import com.limbrescue.limbrescueangularappbackend.security.encryption.AES;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.sql.Timestamp;

@Service
@Transactional()
public class AuthTokenServiceImpl implements AuthTokenService {

  @PersistenceContext
  private EntityManager em;

  @Autowired
  private AuthTokenRepository authtoken;


  @Override
  public Optional<AuthToken> findById(int id) {
    return authtoken.findById(id);
  }

  @Override
  public String insertToken(String uuid, String accessToken, String expiryDate, String username) {
    AES aes_encryption = new AES();
    String key = aes_encryption.init();
    String token = aes_encryption.encrypt(accessToken, key);
    Timestamp exp = new Timestamp(Long.parseLong(expiryDate)*1000);
    Timestamp createdat = new Timestamp(System.currentTimeMillis());
    authtoken.insertToken(uuid, token, exp, createdat, username);
    return key;
  }

        
}