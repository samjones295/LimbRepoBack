package com.limbrescue.limbrescueangularappbackend.models;

import java.util.HashSet;
import java.util.Set;
import java.sql.Timestamp;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Entity
@Table(name = "auth_token", 
    uniqueConstraints = { 
      @UniqueConstraint(columnNames = "id"),
      @UniqueConstraint(columnNames = "uuid") 
    })

public class AuthToken {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @NotBlank
  @Size(max = 128)
  private String uuid;

  private int userid;

  @Size(max = 2047)
  private String token;

  private Timestamp exp;

  private Timestamp createdat;

  public AuthToken() {
  }


  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

}
