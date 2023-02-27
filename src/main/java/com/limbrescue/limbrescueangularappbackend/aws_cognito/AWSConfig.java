package com.limbrescue.limbrescueangularappbackend.aws_cognito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration

public class AWSConfig { 
  @Bean
  public CognitoHelper CognitoHelper() {
    return new CognitoHelper();
  }
}
