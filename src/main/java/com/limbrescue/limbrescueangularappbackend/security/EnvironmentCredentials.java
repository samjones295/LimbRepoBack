package com.limbrescue.limbrescueangularappbackend.security;
import java.io.FileInputStream;  


import java.io.IOException;
import java.sql.*;
import java.util.*;

public class EnvironmentCredentials {
private String fileName;

    public EnvironmentCredentials(String file){
        this.fileName=file;
    }   
    public String getProp(String title){

        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream(fileName));
            return prop.getProperty(title);
        } catch (IOException e) {
            e.printStackTrace();
                    return null;
        }
    }
}