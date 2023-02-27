package com.limbrescue.limbrescueangularappbackend.payload.response;

public class AuthenticationResponse {
    private String uuid; 
    private String publicKey; //The authentication publicKey
    public AuthenticationResponse(String uuid, String publicKey) {
        this.uuid = uuid;
        this.publicKey = publicKey;
    }

    public String getId() {
        return uuid;
    }

    public String getPublicKey() {
        return publicKey;
    }

    
    @Override
    public String toString() {
        return String.format("{\"uuid\":\"%s\", \"publicKey\":\"%s\"}", uuid, publicKey);
    }
}
