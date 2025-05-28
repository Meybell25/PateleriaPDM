package com.example.pasteleriapdm.Models;

import com.google.firebase.database.PropertyName;
import java.util.HashMap;
import java.util.Map;

/**
 * Modelo para representar un Cliente
 * Corresponde al nodo clients/ en Firebase Realtime Database
 */
public class Client {

    // Constantes para estados
    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_INACTIVE = "inactive";
    public static final String STATUS_BLOCKED = "blocked";

    private String id;                  // ID único del cliente
    private String name;                // Nombre completo
    private String phone;               // Teléfono principal
    private String address;             // Dirección completa

    private String status;              // Estado: active, inactive, blocked
    private long createdAt;             // Timestamp de creación
    private long updatedAt;             // Timestamp de última actualización
    private String createdBy;           // UID del seller que lo creó
    private int totalOrders;            // Total de pedidos realizados
    private double totalSpent;          // Total gastado histórico
    private long lastOrderDate;         // Fecha del último pedido
    private boolean preferredClient;    // Cliente preferente

    // Constructor vacío requerido por Firebase
    public Client() {}

    // Constructor básico
    public Client(String name, String phone, String createdBy) {
        this.name = name;
        this.phone = phone;
        this.createdBy = createdBy;
        this.status = STATUS_ACTIVE;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
        this.totalOrders = 0;
        this.totalSpent = 0.0;
        this.lastOrderDate = 0;
        this.preferredClient = false;
    }

    // Constructor completo
    public Client(String name, String phone, String address,
                  String createdBy) {
        this(name, phone, createdBy);
        this.address = address;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) {
        this.name = name;
        updateTimestamp();
    }


    public String getPhone() { return phone; }
    public void setPhone(String phone) {
        this.phone = phone;
        updateTimestamp();
    }


    public String getAddress() { return address; }
    public void setAddress(String address) {
        this.address = address;
        updateTimestamp();
    }


    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
        updateTimestamp();
    }

    @PropertyName("createdAt")
    public long getCreatedAt() { return createdAt; }
    @PropertyName("createdAt")
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    @PropertyName("updatedAt")
    public long getUpdatedAt() { return updatedAt; }
    @PropertyName("updatedAt")
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    @PropertyName("createdBy")
    public String getCreatedBy() { return createdBy; }
    @PropertyName("createdBy")
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }


    @PropertyName("totalOrders")
    public int getTotalOrders() { return totalOrders; }
    @PropertyName("totalOrders")
    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
        updateTimestamp();
    }

    @PropertyName("totalSpent")
    public double getTotalSpent() { return totalSpent; }
    @PropertyName("totalSpent")
    public void setTotalSpent(double totalSpent) {
        this.totalSpent = totalSpent;
        updateTimestamp();
    }

    @PropertyName("lastOrderDate")
    public long getLastOrderDate() { return lastOrderDate; }
    @PropertyName("lastOrderDate")
    public void setLastOrderDate(long lastOrderDate) {
        this.lastOrderDate = lastOrderDate;
        updateTimestamp();
    }

    @PropertyName("preferredClient")
    public boolean isPreferredClient() { return preferredClient; }
    @PropertyName("preferredClient")
    public void setPreferredClient(boolean preferredClient) {
        this.preferredClient = preferredClient;
        updateTimestamp();
    }

    // Métodos de utilidad
    public boolean isActive() {
        return STATUS_ACTIVE.equals(status);
    }

    public boolean isBlocked() {
        return STATUS_BLOCKED.equals(status);
    }

    public void activate() {
        setStatus(STATUS_ACTIVE);
    }

    public void deactivate() {
        setStatus(STATUS_INACTIVE);
    }

    public void block() {
        setStatus(STATUS_BLOCKED);
    }

    // Actualizar timestamp de modificación
    private void updateTimestamp() {
        this.updatedAt = System.currentTimeMillis();
    }

    // Incrementar contador de pedidos
    public void incrementOrders(double orderAmount) {
        this.totalOrders++;
        this.totalSpent += orderAmount;
        this.lastOrderDate = System.currentTimeMillis();
        updateTimestamp();

        // Marcar como cliente preferente si ha gastado más de 100
        if (this.totalSpent >= 100) {
            this.preferredClient = true;
        }
    }


    // Verificar si tiene información completa
    public boolean hasCompleteInfo() {
        return name != null && !name.trim().isEmpty() &&
                phone != null && !phone.trim().isEmpty() &&
                address != null && !address.trim().isEmpty();
    }

    // Obtener total gastado formateado
    public String getFormattedTotalSpent() {
        return String.format("$%,.0f COP", totalSpent);
    }

    /**
     * Método toMap() completo - solo para creación
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        // Todos los campos para creación
        if (this.id != null) map.put("id", this.id);
        map.put("name", this.name);
        map.put("phone", this.phone);
        map.put("address", this.address != null ? this.address : "");
        map.put("status", this.status);
        map.put("createdBy", this.createdBy);
        map.put("createdAt", this.createdAt);
        map.put("updatedAt", this.updatedAt);
        map.put("totalOrders", this.totalOrders);
        map.put("totalSpent", this.totalSpent);
        map.put("lastOrderDate", this.lastOrderDate);
        map.put("isPreferredClient", this.isPreferredClient());

        return map;
    }

    /**
     * Metodo toMap() para actualizaciones
     * Evita incluir campos inmutables que pueden causar problemas con Firebase
     */
    public Map<String, Object> toMapForUpdate() {
        Map<String, Object> map = new HashMap<>();

        // Solo campos que pueden ser actualizados
        map.put("name", this.name);
        map.put("phone", this.phone);
        map.put("address", this.address != null ? this.address : "");
        map.put("status", this.status);
        map.put("updatedAt", System.currentTimeMillis());

        // Campos opcionales que pueden cambiar
        if (this.totalOrders > 0) {
            map.put("totalOrders", this.totalOrders);
        }
        if (this.totalSpent > 0) {
            map.put("totalSpent", this.totalSpent);
        }
        if (this.lastOrderDate > 0) {
            map.put("lastOrderDate", this.lastOrderDate);
        }
        map.put("isPreferredClient", this.preferredClient);

        return map;
    }


    @Override
    public String toString() {
        return "Client{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", status='" + status + '\'' +
                ", totalOrders=" + totalOrders +
                ", totalSpent=" + totalSpent +
                '}';
    }
}