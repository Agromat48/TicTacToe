package org.example.datasource.model;

import jakarta.persistence.*;
import org.example.domain.model.Role;

import java.util.List;
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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_uuid"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private List<Role> roles;

    public DataUser() {
    }

    public DataUser(UUID uuid, String login, String password, List<Role> roles) {
        this.uuid = uuid;
        this.login = login;
        this.password = password;
        this.roles = roles;
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

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }
}
