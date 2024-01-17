package com.droidev.postgresqlchat;

import java.io.Serializable;

public class DatabaseDetails implements Serializable {
    private String identifyName;
    private String username;
    private String dbName;
    private String dbUser;
    private String dbPass;
    private String dbHost;
    private String dbPort;

    public DatabaseDetails(String identifyName, String username, String dbName, String dbUser, String dbPass, String dbHost, String dbPort) {
        this.identifyName = identifyName;
        this.username = username;
        this.dbName = dbName;
        this.dbUser = dbUser;
        this.dbPass = dbPass;
        this.dbHost = dbHost;
        this.dbPort = dbPort;
    }

    public String getIdentifyName() {
        return identifyName;
    }

    public String getUsername() {
        return username;
    }

    public String getDbName() {
        return dbName;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getDbPass() {
        return dbPass;
    }

    public String getDbHost() {
        return dbHost;
    }

    public String getDbPort() {
        return dbPort;
    }
}

