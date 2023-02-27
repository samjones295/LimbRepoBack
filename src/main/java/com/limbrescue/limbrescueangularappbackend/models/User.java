package com.limbrescue.limbrescueangularappbackend.models;

import java.util.HashSet;
import java.util.Set;
import java.sql.Timestamp;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Entity
@Table(name = "user", 
    uniqueConstraints = { 
      @UniqueConstraint(columnNames = "id"),
      @UniqueConstraint(columnNames = "username") 
    })

public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @NotBlank
  @Size(max = 255)
  private String username;

  @Size(max = 35)
  private String givenname;

  @Size(max = 35)
  private String middlename;

  @Size(max = 35)
  private String familyname;

  private Boolean confirmed;

  private Timestamp createdat;

  private Timestamp updatedat;


  public User() {
  }

  public User(String username) {
    this.username = username;

  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

}
