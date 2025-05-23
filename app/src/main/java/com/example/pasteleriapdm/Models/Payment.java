package com.example.pasteleriapdm.Models;

import com.google.firebase.database.PropertyName;
import java.util.HashMap;
import java.util.Map;

/**
 * Modelo para representar un Pago
 * Corresponde al subnodo payment/ dentro de reservations/ en Firebase
 */
public class Payment {

    // Constantes para métodos de pago
    public static final String METHOD_CASH = "efectivo";
    public static final String METHOD_CARD = "tarjeta";
    public static final String METHOD_TRANSFER = "transferencia";
    public static final String METHOD_DEPOSIT = "deposito";

    // Constantes para estados de pago
    public static final String STATUS_PENDING = "pendiente";
    public static final String STATUS_PAID = "pagado";
    public static final String STATUS_CANCELLED = "cancelado";
    public static final String STATUS_REFUNDED = "reembolsado";

    private double amount;          // Monto total del pago
    private String method;          // Metodo de pago
    private String status;          // Estado del pago
    private long timestamp;         // Timestamp del pago
    private String reference;       // Referencia del pago (número de transacción)
    private String notes;           // Notas del pago
    private String processedBy;     // UID del usuario que procesó el pago
    private double discount;        // Descuento aplicado
    private double tax;             // Impuesto aplicado
    private double finalAmount;     // Monto final después de descuentos e impuestos

    // Constructor vacío requerido por Firebase
    public Payment() {}

    // Constructor básico
    public Payment(double amount, String method) {
        this.amount = amount;
        this.method = method;
        this.status = STATUS_PENDING;
        this.timestamp = System.currentTimeMillis();
        this.discount = 0.0;
        this.tax = 0.0;
        this.finalAmount = amount;
    }

    // Constructor completo
    public Payment(double amount, String method, String status, String processedBy) {
        this(amount, method);
        this.status = status;
        this.processedBy = processedBy;
    }

    // Getters y Setters
    public double getAmount() { return amount; }
    public void setAmount(double amount) {
        this.amount = amount;
        calculateFinalAmount();
    }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    @PropertyName("processedBy")
    public String getProcessedBy() { return processedBy; }
    @PropertyName("processedBy")
    public void setProcessedBy(String processedBy) { this.processedBy = processedBy; }

    public double getDiscount() { return discount; }
    public void setDiscount(double discount) {
        this.discount = discount;
        calculateFinalAmount();
    }

    public double getTax() { return tax; }
    public void setTax(double tax) {
        this.tax = tax;
        calculateFinalAmount();
    }

    @PropertyName("finalAmount")
    public double getFinalAmount() { return finalAmount; }
    @PropertyName("finalAmount")
    public void setFinalAmount(double finalAmount) { this.finalAmount = finalAmount; }

    // Métodos de utilidad
    public boolean isPending() {
        return STATUS_PENDING.equals(status);
    }

    public boolean isPaid() {
        return STATUS_PAID.equals(status);
    }

    public boolean isCancelled() {
        return STATUS_CANCELLED.equals(status);
    }

    public void markAsPaid(String processedBy) {
        this.status = STATUS_PAID;
        this.processedBy = processedBy;
        this.timestamp = System.currentTimeMillis();
    }

    public void cancel() {
        this.status = STATUS_CANCELLED;
        this.timestamp = System.currentTimeMillis();
    }

    // Calcular monto final
    private void calculateFinalAmount() {
        this.finalAmount = this.amount - this.discount + this.tax;
    }

    // Obtener monto formateado
    public String getFormattedAmount() {
        return String.format("$%,.0f COP", amount);
    }

    public String getFormattedFinalAmount() {
        return String.format("$%,.0f COP", finalAmount);
    }

    // Validar método de pago válido
    public static boolean isValidMethod(String method) {
        return METHOD_CASH.equals(method) ||
                METHOD_CARD.equals(method) ||
                METHOD_TRANSFER.equals(method) ||
                METHOD_DEPOSIT.equals(method);
    }

    // Validar estado válido
    public static boolean isValidStatus(String status) {
        return STATUS_PENDING.equals(status) ||
                STATUS_PAID.equals(status) ||
                STATUS_CANCELLED.equals(status) ||
                STATUS_REFUNDED.equals(status);
    }

    // Convertir a Map para Firebase
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("amount", amount);
        result.put("method", method);
        result.put("status", status);
        result.put("timestamp", timestamp);
        result.put("reference", reference);
        result.put("notes", notes);
        result.put("processedBy", processedBy);
        result.put("discount", discount);
        result.put("tax", tax);
        result.put("finalAmount", finalAmount);
        return result;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "amount=" + amount +
                ", method='" + method + '\'' +
                ", status='" + status + '\'' +
                ", finalAmount=" + finalAmount +
                '}';
    }
}