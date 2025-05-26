package com.example.pasteleriapdm.Models;

import com.google.firebase.database.PropertyName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Modelo para representar un Usuario del sistema
 * Corresponde al nodo users/ en Firebase Realtime Database
 */
public class User implements Serializable {

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
    private String password;      // Contraseña del usuario
    private String role;          // Rol: admin, seller, production
    private String status;        // Estado: active, inactive, blocked
    private long createdAt;       // Timestamp de creación
    private long lastLogin;       // Último login
    private long updatedAt;

    // Constructor vacío requerido por Firebase
    public User() {}

    // Constructor completo
    public User(String uid, String name, String email, String role, String password) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.role = role;
        this.password = password;
        this.status = STATUS_ACTIVE;
        this.createdAt = System.currentTimeMillis();
        this.lastLogin = 0;
        this.updatedAt = System.currentTimeMillis();
    }

    // Constructor para admin inicial (creado por el sistema)
    public static User createInitialAdmin(String uid, String name, String email, String password) {
        User admin = new User(uid, name, email, password, ROLE_ADMIN);
        return admin;
    }

    // Getters y Setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    @PropertyName("createdAt")
    public long getCreatedAt() { return createdAt; }
    @PropertyName("createdAt")
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    @PropertyName("lastLogin")
    public long getLastLogin() { return lastLogin; }
    @PropertyName("lastLogin")
    public void setLastLogin(long lastLogin) { this.lastLogin = lastLogin; }





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

    // Validar si el usuario puede crear otros usuarios
    public boolean canCreateUsers() {
        return isAdmin() && isActive();
    }

    // Validar si el usuario puede crear un rol específico
    public boolean canCreateRole(String targetRole) {
        if (!canCreateUsers()) return false;

        // Admin puede crear cualquier rol
        return isAdmin();
    }

    // Método toMap mejorado para Firebase
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("name", name);
        result.put("email", email);
        result.put("role", role);
        result.put("status", status);
        result.put("createdAt", createdAt);
        result.put("lastLogin", lastLogin);
        result.put("updatedAt", updatedAt);

        // Solo incluir contraseña si no está vacía
        if (password != null && !password.trim().isEmpty()) {
            result.put("password", password);
        }

        return result;
    }

    // Método para crear mapa de actualización (sin campos sensibles)
    public Map<String, Object> toUpdateMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("role", role);
        result.put("status", status);
        result.put("updatedAt", System.currentTimeMillis());

        // Solo incluir contraseña si se va a actualizar
        if (password != null && !password.trim().isEmpty()) {
            result.put("password", password);
        }

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

    // Método para validar contraseña
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    // Método para validar datos antes de guardar
    public boolean isValid() {
        return uid != null && !uid.isEmpty() &&
                name != null && !name.isEmpty() &&
                email != null && !email.isEmpty() &&
                password != null && !password.isEmpty() &&
                isValidPassword(password) &&
                isValidRole(role) &&
                isValidStatus(status);
    }

    // Método para obtener contraseña censurada (para mostrar en UI)
    public String getCensoredPassword() {
        if (password == null || password.isEmpty()) {
            return "";
        }
        return "*".repeat(password.length());
    }

    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", password='[PROTECTED]'" +
                ", role='" + role + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}