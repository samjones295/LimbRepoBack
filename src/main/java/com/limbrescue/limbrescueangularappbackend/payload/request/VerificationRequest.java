package com.limbrescue.limbrescueangularappbackend.payload.request;

import java.util.Set;

import javax.validation.constraints.*;

public class VerificationRequest {
  @NotBlank
  @Size(max = 255)
  private String username;

  @NotBlank
  @Size(max = 12)
  private String verificationcode;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

    public String getVerificationcode() {
    return verificationcode;
  }

  public void setVerificationcode(String verificationcode) {
    this.verificationcode = verificationcode;
  }

}
