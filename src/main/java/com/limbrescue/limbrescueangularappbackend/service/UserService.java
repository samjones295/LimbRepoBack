
package com.limbrescue.limbrescueangularappbackend.service;
  
import com.limbrescue.limbrescueangularappbackend.models.User;
// Importing required classes
import java.util.Optional;

  
// Interface
public interface UserService {
  

    public Optional<User> findByUsername(String username);
    
}