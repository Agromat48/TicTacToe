package org.example.datasource.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "users")
public class DataUser {
    @Id
    private UUID uuid;

    @Column(name = "user_login", unique = true)
    private String login;

    @Column(name = "user_password")
    private String password;

    public DataUser() {
    }

    public DataUser(UUID uuid, String login, String password) {
        this.uuid = uuid;
        this.login = login;
        this.password = password;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
