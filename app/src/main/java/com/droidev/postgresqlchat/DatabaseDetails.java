package com.droidev.postgresqlchat;

import java.io.Serializable;

public class DatabaseDetails implements Serializable {
    private final String identifyName;
    private final String username;
    private final String dbName;
    private final String dbUser;
    private final String dbPass;
    private final String dbHost;
    private final String dbPort;

    private final String dbEncryptKey;

    public DatabaseDetails(String identifyName, String username, String dbName, String dbUser, String dbPass, String dbHost, String dbPort, String dbEncryptKey) {
        this.identifyName = identifyName;
        this.username = username;
        this.dbName = dbName;
        this.dbUser = dbUser;
        this.dbPass = dbPass;
        this.dbHost = dbHost;
        this.dbPort = dbPort;
        this.dbEncryptKey = dbEncryptKey;
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

    public String getDbEcryptKey() {
        return dbEncryptKey;
    }
}

