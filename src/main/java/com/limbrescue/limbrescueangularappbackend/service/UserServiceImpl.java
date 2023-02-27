package com.limbrescue.limbrescueangularappbackend.service;
import com.limbrescue.limbrescueangularappbackend.repository.UserRepository;
import com.limbrescue.limbrescueangularappbackend.models.User;


import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
@Transactional()
public class UserServiceImpl implements UserService {

  @PersistenceContext
  private EntityManager em;

  @Autowired
  private UserRepository user;


  @Override
  public Optional<User> findByUsername(String username) {
    return user.findByUsername(username);
  }

}