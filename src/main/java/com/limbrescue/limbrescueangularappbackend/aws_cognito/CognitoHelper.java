package com.limbrescue.limbrescueangularappbackend.aws_cognito;

import com.limbrescue.limbrescueangularappbackend.security.EnvironmentCredentials;
import com.limbrescue.limbrescueangularappbackend.aws_cognito.CognitoJWTParser;
import com.limbrescue.limbrescueangularappbackend.aws_cognito.SRPAuthenticationHelper;


import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidentity.AmazonCognitoIdentity;
import com.amazonaws.services.cognitoidentity.AmazonCognitoIdentityClientBuilder;
import com.amazonaws.services.cognitoidentity.model.*;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import org.json.JSONObject;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;


/**
 * The CognitoHelper class abstracts the functionality of connecting to the Cognito user pool and Federated Identities.
 */
public class CognitoHelper {
    private String POOL_ID;
    private String CLIENTAPP_ID;
    private String CLIENT_SECRET;
    private String FED_POOL_ID;
    private String CUSTOMDOMAIN;
    private String REGION;
    private String File = "src/main/resources/AWSCredentials.properties";

    private static final Properties p = new Properties();

    
    public CognitoHelper() {
        try {
            EnvironmentCredentials AWSCredentials = new EnvironmentCredentials(File);
            // Read the property values
            POOL_ID = AWSCredentials.getProp("POOL_ID");
            CLIENTAPP_ID = AWSCredentials.getProp("CLIENTAPP_ID");
            FED_POOL_ID = AWSCredentials.getProp("FED_POOL_ID");
            CLIENT_SECRET = AWSCredentials.getProp("CLIENT_SECRET");
            REGION = AWSCredentials.getProp("REGION");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sign up the user to the user pool
     *
     * @param username    User name for the sign up
     * @param password    Password for the sign up
     * @param email       email used to sign up
     * @param phonenumber phone number to sign up.
     * @return whether the call was successful or not.
     */
    public boolean SignUpUser(String email, String password, String givenName, String middleName, String familyName) {
        AnonymousAWSCredentials awsCreds = new AnonymousAWSCredentials();
        AWSCognitoIdentityProvider cognitoIdentityProvider = AWSCognitoIdentityProviderClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .withRegion(Regions.fromName(REGION))
                .build();


        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setClientId(CLIENTAPP_ID);
        signUpRequest.setUsername(email);
        signUpRequest.setPassword(password);
        List<AttributeType> list = new ArrayList<>();

        AttributeType attributeTypeEmail = new AttributeType();
        attributeTypeEmail.setName("email");
        attributeTypeEmail.setValue(email);
        list.add(attributeTypeEmail);

        AttributeType attributeTypeFamilyName = new AttributeType();
        attributeTypeFamilyName.setName("family_name");
        attributeTypeFamilyName.setValue(familyName);
        list.add(attributeTypeFamilyName);

        AttributeType attributeTypeGivenName = new AttributeType();
        attributeTypeGivenName.setName("given_name");
        attributeTypeGivenName.setValue(givenName);
        list.add(attributeTypeGivenName);

        AttributeType attributeTypeMiddleName = new AttributeType();
        attributeTypeMiddleName.setName("middle_name");
        attributeTypeMiddleName.setValue(middleName);
        list.add(attributeTypeMiddleName);

        signUpRequest.setUserAttributes(list);

        try {
            SignUpResult result = cognitoIdentityProvider.signUp(signUpRequest);
            System.out.println(result);
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
        return true;
    }



    public boolean verifyAccessCode(String username, String code) {
        AnonymousAWSCredentials awsCreds = new AnonymousAWSCredentials();
        AWSCognitoIdentityProvider cognitoIdentityProvider = AWSCognitoIdentityProviderClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .withRegion(Regions.fromName(REGION))
                .build();

        ConfirmSignUpRequest confirmSignUpRequest = new ConfirmSignUpRequest();
        confirmSignUpRequest.setUsername(username);
        confirmSignUpRequest.setConfirmationCode(code);
        confirmSignUpRequest.setClientId(CLIENTAPP_ID);


        try {
            ConfirmSignUpResult confirmSignUpResult = cognitoIdentityProvider.confirmSignUp(confirmSignUpRequest);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    public String validateUser(String username, String password) {
        SRPAuthenticationHelper helper = new SRPAuthenticationHelper(POOL_ID, CLIENTAPP_ID, CLIENT_SECRET, REGION);
        return helper.PerformSRPAuthentication("davecverano@gmail.com", "Asdf123!");
    }

    /**
     * Returns the AWS credentials
     *
     * @param idprovider the IDP provider for the login map
     * @param id         the username for the login map.
     * @return returns the credentials based on the access token returned from the user pool.
     */
    public Credentials getCredentials(String idprovider, String id) {
        AnonymousAWSCredentials awsCreds = new AnonymousAWSCredentials();
        AmazonCognitoIdentity provider = AmazonCognitoIdentityClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .withRegion(Regions.fromName(REGION))
                .build();

        GetIdRequest idrequest = new GetIdRequest();
        idrequest.setIdentityPoolId(FED_POOL_ID);
        idrequest.addLoginsEntry(idprovider, id);
        GetIdResult idResult = provider.getId(idrequest);

        GetCredentialsForIdentityRequest request = new GetCredentialsForIdentityRequest();
        request.setIdentityId(idResult.getIdentityId());
        request.addLoginsEntry(idprovider, id);

        GetCredentialsForIdentityResult result = provider.getCredentialsForIdentity(request);
        return result.getCredentials();
    }

}