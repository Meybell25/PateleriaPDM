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

    private String id;                            // ID unico de la reserva
    private String clientId;                      // ID del cliente
    private String createdBy;                     // UID del seller que la creo
    private long createdAt;                       // Timestamp de creacion
    private long deliveryAt;                      // Timestamp de entrega programada
    private long updatedAt;                       // Timestamp de ultima actualizacion
    private String status;                        // Estado actual de la reserva
    private Map<String, Object> items;            // CAMBIADO: cakeId -> {quantity, unitPrice, cakeName}
    private Payment payment;                      // Información del pago
    private String notes;                         // Notas especiales del pedido
    private String deliveryAddress;               // Direccion de entrega
    private double totalAmount;                   // Monto total calculado
    private String lastUpdatedBy;                 // UID del último usuario que la modifico

    // Constructor vacio requerido por Firebase
    public Reservation() {}

    // Constructor básico actualizado
    public Reservation(String clientId, String createdBy, long deliveryAt,
                       Map<String, Object> items) {
        this.clientId = clientId;
        this.createdBy = createdBy;
        this.deliveryAt = deliveryAt;
        this.items = items;
        this.status = STATUS_PENDING;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
        this.totalAmount = 0.0;
    }

    // Constructor completo
    public Reservation(String clientId, String createdBy, long deliveryAt,
                       Map<String, Object> items, String notes) {
        this(clientId, createdBy, deliveryAt, items);
        this.notes = notes;
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

    // CAMBIADO: Ahora usa Map<String, Object>
    public Map<String, Object> getItems() { return items; }
    public void setItems(Map<String, Object> items) {
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

    // ACTUALIZADO: Calcular total de items con el nuevo formato
    public int getTotalItems() {
        if (items == null) return 0;
        int total = 0;
        for (Object itemObj : items.values()) {
            if (itemObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> itemData = (Map<String, Object>) itemObj;
                Object quantityObj = itemData.get("quantity");
                if (quantityObj instanceof Number) {
                    total += ((Number) quantityObj).intValue();
                }
            }
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
    // Agregar este método a tu clase Reservation
    @SuppressWarnings("unchecked")
    public void loadFromMap(Map<String, Object> data) {
        if (data.containsKey("id")) {
            this.id = (String) data.get("id");
        }

        if (data.containsKey("clientId")) {
            this.clientId = (String) data.get("clientId");
        }

        if (data.containsKey("createdBy")) {
            this.createdBy = (String) data.get("createdBy");
        }

        if (data.containsKey("createdAt")) {
            Object createdAtObj = data.get("createdAt");
            if (createdAtObj instanceof Long) {
                this.createdAt = (Long) createdAtObj;
            }
        }

        if (data.containsKey("deliveryAt")) {
            Object deliveryAtObj = data.get("deliveryAt");
            if (deliveryAtObj instanceof Long) {
                this.deliveryAt = (Long) deliveryAtObj;
            }
        }

        if (data.containsKey("updatedAt")) {
            Object updatedAtObj = data.get("updatedAt");
            if (updatedAtObj instanceof Long) {
                this.updatedAt = (Long) updatedAtObj;
            }
        }

        if (data.containsKey("status")) {
            this.status = (String) data.get("status");
        }

        if (data.containsKey("items")) {
            this.items = (Map<String, Object>) data.get("items");
        }

        if (data.containsKey("notes")) {
            this.notes = (String) data.get("notes");
        }

        if (data.containsKey("deliveryAddress")) {
            this.deliveryAddress = (String) data.get("deliveryAddress");
        }

        if (data.containsKey("totalAmount")) {
            Object totalAmountObj = data.get("totalAmount");
            if (totalAmountObj instanceof Double) {
                this.totalAmount = (Double) totalAmountObj;
            } else if (totalAmountObj instanceof Long) {
                this.totalAmount = ((Long) totalAmountObj).doubleValue();
            }
        }

        if (data.containsKey("lastUpdatedBy")) {
            this.lastUpdatedBy = (String) data.get("lastUpdatedBy");
        }

        // Cargar información del pago
        if (data.containsKey("payment") && data.get("payment") != null) {
            Map<String, Object> paymentData = (Map<String, Object>) data.get("payment");
            Payment payment = new Payment();

            if (paymentData.containsKey("amount")) {
                Object amountObj = paymentData.get("amount");
                if (amountObj instanceof Double) {
                    payment.setAmount((Double) amountObj);
                } else if (amountObj instanceof Long) {
                    payment.setAmount(((Long) amountObj).doubleValue());
                }
            }

            if (paymentData.containsKey("method")) {
                payment.setMethod((String) paymentData.get("method"));
            }

            if (paymentData.containsKey("status")) {
                payment.setStatus((String) paymentData.get("status"));
            }

            if (paymentData.containsKey("timestamp")) {
                Object timestampObj = paymentData.get("timestamp");
                if (timestampObj instanceof Long) {
                    payment.setTimestamp((Long) timestampObj);
                }
            }

            if (paymentData.containsKey("processedBy")) {
                payment.setProcessedBy((String) paymentData.get("processedBy"));
            }

            if (paymentData.containsKey("discount")) {
                Object discountObj = paymentData.get("discount");
                if (discountObj instanceof Double) {
                    payment.setDiscount((Double) discountObj);
                } else if (discountObj instanceof Long) {
                    payment.setDiscount(((Long) discountObj).doubleValue());
                }
            }

            if (paymentData.containsKey("tax")) {
                Object taxObj = paymentData.get("tax");
                if (taxObj instanceof Double) {
                    payment.setTax((Double) taxObj);
                } else if (taxObj instanceof Long) {
                    payment.setTax(((Long) taxObj).doubleValue());
                }
            }

            if (paymentData.containsKey("finalAmount")) {
                Object finalAmountObj = paymentData.get("finalAmount");
                if (finalAmountObj instanceof Long) {
                    payment.setFinalAmount(((Long) finalAmountObj).doubleValue());
                } else if (finalAmountObj instanceof Double) {
                    payment.setFinalAmount((Double) finalAmountObj);
                }
            }

            this.payment = payment;
        }
    }

    public Reservation(Map<String, Object> data) {
        loadFromMap(data);
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