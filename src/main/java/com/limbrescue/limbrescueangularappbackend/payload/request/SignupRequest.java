package com.limbrescue.limbrescueangularappbackend.payload.request;

import java.util.Set;

import javax.validation.constraints.*;

public class SignupRequest {
  @NotBlank
  @Size(max = 255)
  private String username;

  @NotBlank
  @Size(max = 255)
  private String password;

  @Size(max = 35)
  private String givenname;

  @Size(max = 35)
  private String familyname;

  @Size(max = 35)
  private String middlename;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

    public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getGivenname() {
    return givenname;
  }

  public void setGivenname(String givenname) {
    this.givenname = givenname;
  }

  public String getFamilyname() {
    return familyname;
  }

  public void setFamilyname(String familyname) {
    this.familyname = familyname;
  }

  public String getMiddlename() {
    return middlename;
  }

  public void setMiddlename(String middlename) {
    this.middlename = middlename;
  }
}
