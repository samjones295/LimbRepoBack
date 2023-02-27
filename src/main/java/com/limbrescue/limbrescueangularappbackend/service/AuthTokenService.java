
package com.limbrescue.limbrescueangularappbackend.service;
  
import com.limbrescue.limbrescueangularappbackend.models.AuthToken;
// Importing required classes
import java.util.Optional;

  
// Interface
public interface AuthTokenService {
  

    public Optional<AuthToken> findById(int id);
    public String insertToken(String uuid, String username, String accessToken, String expiryDate);
    
}