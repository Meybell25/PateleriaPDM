package com.example.pasteleriapdm.Models;

import com.google.firebase.database.PropertyName;
import java.util.HashMap;
import java.util.Map;

/**
 * Modelo para representar una Reserva/Pedido
 * Corresponde al nodo reservations/ en Firebase Realtime Database
 */
public class Reservation {

    // Constantes para estados de reserva
    public static final String STATUS_PENDING = "pendiente";
    public static final String STATUS_CONFIRMED = "confirmada";
    public static final String STATUS_IN_PRODUCTION = "en_produccion";
    public static final String STATUS_READY = "lista_para_entrega";
    public static final String STATUS_DELIVERED = "entregada";
    public static final String STATUS_CANCELLED = "cancelada";

    // Constantes para prioridad
    public static final String PRIORITY_LOW = "baja";
    public static final String PRIORITY_NORMAL = "normal";
    public static final String PRIORITY_HIGH = "alta";
    public static final String PRIORITY_URGENT = "urgente";

    private String id;                    // ID unico de la reserva
    private String clientId;              // ID del cliente
    private String createdBy;             // UID del seller que la creo
    private long createdAt;               // Timestamp de creacion
    private long deliveryAt;              // Timestamp de entrega programada
    private long updatedAt;               // Timestamp de ultima actualizacion
    private String status;                // Estado actual de la reserva
    private Map<String, Integer> items;   // cakeId -> cantidad
    private Payment payment;              // Información del pago
    private String notes;                 // Notas especiales del pedido
    private String deliveryAddress;       // Direccion de entrega
    private String priority;              // Prioridad del pedido
    private double totalAmount;           // Monto total calculado
    private String lastUpdatedBy;         // UID del último usuario que la modifico


    // Constructor vacio requerido por Firebase
    public Reservation() {}

    // Constructor básico
    public Reservation(String clientId, String createdBy, long deliveryAt,
                       Map<String, Integer> items) {
        this.clientId = clientId;
        this.createdBy = createdBy;
        this.deliveryAt = deliveryAt;
        this.items = items;
        this.status = STATUS_PENDING;
        this.priority = PRIORITY_NORMAL;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
        this.totalAmount = 0.0;
    }

    // Constructor completo
    public Reservation(String clientId, String createdBy, long deliveryAt,
                       Map<String, Integer> items, String notes, String priority) {
        this(clientId, createdBy, deliveryAt, items);
        this.notes = notes;
        this.priority = priority;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @PropertyName("clientId")
    public String getClientId() { return clientId; }

    @PropertyName("clientId")
    public void setClientId(String clientId) { this.clientId = clientId; }

    @PropertyName("createdBy")
    public String getCreatedBy() { return createdBy; }
    @PropertyName("createdBy")
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    @PropertyName("createdAt")
    public long getCreatedAt() { return createdAt; }

    @PropertyName("createdAt")
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    @PropertyName("deliveryAt")
    public long getDeliveryAt() { return deliveryAt; }
    @PropertyName("deliveryAt")
    public void setDeliveryAt(long deliveryAt) {
        this.deliveryAt = deliveryAt;
        updateTimestamp();
    }

    @PropertyName("updatedAt")
    public long getUpdatedAt() { return updatedAt; }
    @PropertyName("updatedAt")
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
        updateTimestamp();
    }

    public Map<String, Integer> getItems() { return items; }
    public void setItems(Map<String, Integer> items) {
        this.items = items;
        updateTimestamp();
    }

    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) {
        this.payment = payment;
        updateTimestamp();
    }

    public String getNotes() { return notes; }
    public void setNotes(String notes) {
        this.notes = notes;
        updateTimestamp();
    }

    @PropertyName("deliveryAddress")
    public String getDeliveryAddress() { return deliveryAddress; }
    @PropertyName("deliveryAddress")
    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
        updateTimestamp();
    }

    public String getPriority() { return priority; }
    public void setPriority(String priority) {
        this.priority = priority;
        updateTimestamp();
    }

    @PropertyName("totalAmount")
    public double getTotalAmount() { return totalAmount; }
    @PropertyName("totalAmount")
    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
        updateTimestamp();
    }

    @PropertyName("lastUpdatedBy")
    public String getLastUpdatedBy() { return lastUpdatedBy; }
    @PropertyName("lastUpdatedBy")
    public void setLastUpdatedBy(String lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
        updateTimestamp();
    }


    // Metodos de utilidad para estados
    public boolean isPending() {
        return STATUS_PENDING.equals(status);
    }

    public boolean isConfirmed() {
        return STATUS_CONFIRMED.equals(status);
    }

    public boolean isInProduction() {
        return STATUS_IN_PRODUCTION.equals(status);
    }

    public boolean isReady() {
        return STATUS_READY.equals(status);
    }

    public boolean isDelivered() {
        return STATUS_DELIVERED.equals(status);
    }

    public boolean isCancelled() {
        return STATUS_CANCELLED.equals(status);
    }

    // Metodos para cambiar estados
    public void confirm(String updatedBy) {
        setStatus(STATUS_CONFIRMED);
        setLastUpdatedBy(updatedBy);
    }

    public void startProduction(String updatedBy) {
        setStatus(STATUS_IN_PRODUCTION);
        setLastUpdatedBy(updatedBy);
    }

    public void markReady(String updatedBy) {
        setStatus(STATUS_READY);
        setLastUpdatedBy(updatedBy);
    }

    public void deliver(String updatedBy) {
        setStatus(STATUS_DELIVERED);
        setLastUpdatedBy(updatedBy);
    }

    public void cancel(String updatedBy) {
        setStatus(STATUS_CANCELLED);
        setLastUpdatedBy(updatedBy);
    }

    // Actualizar timestamp
    private void updateTimestamp() {
        this.updatedAt = System.currentTimeMillis();
    }

    // Calcular total de items
    public int getTotalItems() {
        if (items == null) return 0;
        int total = 0;
        for (Integer quantity : items.values()) {
            total += quantity;
        }
        return total;
    }

    // Obtener monto formateado
    public String getFormattedTotalAmount() {
        return String.format("$%,.0f COP", totalAmount);
    }

    // Verificar si esta vencida
    public boolean isOverdue() {
        return deliveryAt < System.currentTimeMillis() && !isDelivered() && !isCancelled();
    }

    // Validar estado valido
    public static boolean isValidStatus(String status) {
        return STATUS_PENDING.equals(status) ||
                STATUS_CONFIRMED.equals(status) ||
                STATUS_IN_PRODUCTION.equals(status) ||
                STATUS_READY.equals(status) ||
                STATUS_DELIVERED.equals(status) ||
                STATUS_CANCELLED.equals(status);
    }

    // Validar prioridad valida
    public static boolean isValidPriority(String priority) {
        return PRIORITY_LOW.equals(priority) ||
                PRIORITY_NORMAL.equals(priority) ||
                PRIORITY_HIGH.equals(priority) ||
                PRIORITY_URGENT.equals(priority);
    }

    // Convertir a Map para Firebase
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("clientId", clientId);
        result.put("createdBy", createdBy);
        result.put("createdAt", createdAt);
        result.put("deliveryAt", deliveryAt);
        result.put("updatedAt", updatedAt);
        result.put("status", status);
        result.put("items", items);
        result.put("payment", payment != null ? payment.toMap() : null);
        result.put("notes", notes);
        result.put("deliveryAddress", deliveryAddress);
        result.put("priority", priority);
        result.put("totalAmount", totalAmount);
        result.put("lastUpdatedBy", lastUpdatedBy);
        return result;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id='" + id + '\'' +
                ", clientId='" + clientId + '\'' +
                ", status='" + status + '\'' +
                ", totalAmount=" + totalAmount +
                ", deliveryAt=" + deliveryAt +
                '}';
    }
}