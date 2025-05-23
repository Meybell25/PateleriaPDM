package com.example.pasteleriapdm.Models;

import com.google.firebase.database.PropertyName;
import java.util.HashMap;
import java.util.Map;

/**
 * Modelo para representar un Usuario del sistema
 * Corresponde al nodo users/ en Firebase Realtime Database
 */
public class User {

    // Constantes para roles
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_SELLER = "seller";
    public static final String ROLE_PRODUCTION = "production";

    // Constantes para estados
    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_INACTIVE = "inactive";
    public static final String STATUS_BLOCKED = "blocked";

    private String uid;           // ID de Firebase Auth
    private String name;          // Nombre completo
    private String email;         // Email del usuario
    private String role;          // Rol: admin, seller, production
    private String status;        // Estado: active, inactive, blocked
    private long createdAt;       // Timestamp de creación
    private long lastLogin;       // Último login
    private String createdBy;     // UID del admin que lo creó

    // Constructor vacío requerido por Firebase
    public User() {}

    // Constructor completo
    public User(String uid, String name, String email, String role) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.role = role;
        this.status = STATUS_ACTIVE;
        this.createdAt = System.currentTimeMillis();
        this.lastLogin = 0;
    }

    // Constructor para registro inicial
    public User(String uid, String name, String email, String role, String createdBy) {
        this(uid, name, email, role);
        this.createdBy = createdBy;
    }

    // Getters y Setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @PropertyName("createdAt")
    public long getCreatedAt() { return createdAt; }
    @PropertyName("createdAt")
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    @PropertyName("lastLogin")
    public long getLastLogin() { return lastLogin; }
    @PropertyName("lastLogin")
    public void setLastLogin(long lastLogin) { this.lastLogin = lastLogin; }

    @PropertyName("createdBy")
    public String getCreatedBy() { return createdBy; }
    @PropertyName("createdBy")
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }


    // Métodos de utilidad
    public boolean isAdmin() {
        return ROLE_ADMIN.equals(role);
    }

    public boolean isSeller() {
        return ROLE_SELLER.equals(role);
    }

    public boolean isProduction() {
        return ROLE_PRODUCTION.equals(role);
    }

    public boolean isActive() {
        return STATUS_ACTIVE.equals(status);
    }

    public boolean isBlocked() {
        return STATUS_BLOCKED.equals(status);
    }

    // Método para actualizar último login
    public void updateLastLogin() {
        this.lastLogin = System.currentTimeMillis();
    }

    // Convertir a Map para Firebase
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("name", name);
        result.put("email", email);
        result.put("role", role);
        result.put("status", status);
        result.put("createdAt", createdAt);
        result.put("lastLogin", lastLogin);
        result.put("createdBy", createdBy);
        return result;
    }

    // Validar rol válido
    public static boolean isValidRole(String role) {
        return ROLE_ADMIN.equals(role) ||
                ROLE_SELLER.equals(role) ||
                ROLE_PRODUCTION.equals(role);
    }

    // Validar estado válido
    public static boolean isValidStatus(String status) {
        return STATUS_ACTIVE.equals(status) ||
                STATUS_INACTIVE.equals(status) ||
                STATUS_BLOCKED.equals(status);
    }

    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}