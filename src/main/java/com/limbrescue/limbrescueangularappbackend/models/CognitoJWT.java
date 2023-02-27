package com.limbrescue.limbrescueangularappbackend.models;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


import java.util.List;
import javax.validation.constraints.NotBlank;

@JsonIgnoreProperties(value = {
"sub",
"cognito:groups",
"email_verified",
"cognito:username",
"given_name",
"origin_jti",
"cognito:roles",
"aud",
"event_id",
"token_use",
"auth_time",
"iat",
"family_name",
"jti",
"email"})

public class CognitoJWT {
  @NotBlank
  private String iss;
  @NotBlank
  private String exp;



  public String getIss() {
    return iss;
  }

  public void setIss(String iss) {
    this.iss = iss.replace("https://", "");
  }

  public String getExp() {
    return exp;
  }

  public void setExp(String exp) {
    this.exp = exp;
  }

  //ToString
  @Override
  public String toString(){
    return "{" +
            "iss: " + iss + ", " +
            "exp: " + exp + ", " +
            "}";
  }


}