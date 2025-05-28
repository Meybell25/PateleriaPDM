package com.example.pasteleriapdm.Models;

import com.google.firebase.database.PropertyName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Modelo para representar un Pastel del catálogo
 * Corresponde al nodo cakes/ en Firebase Realtime Database
 */
public class Cake implements Serializable {

    // Constantes para categorías
    public static final String CATEGORY_CHOCOLATE = "chocolate";
    public static final String CATEGORY_VANILLA = "vanilla";
    public static final String CATEGORY_FRUIT = "fruit";
    public static final String CATEGORY_CHEESECAKE = "cheesecake";
    public static final String CATEGORY_SPECIAL = "special";
    public static final String CATEGORY_BIRTHDAY = "birthday";
    public static final String CATEGORY_WEDDING = "wedding";

    // Constantes para estados
    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_INACTIVE = "inactive";

    // Constantes para tamaños
    public static final String SIZE_SMALL = "small";     // 6-8 personas
    public static final String SIZE_MEDIUM = "medium";   // 10-12 personas
    public static final String SIZE_LARGE = "large";     // 15-20 personas
    public static final String SIZE_XLARGE = "xlarge";   // 25+ personas

    private String id;               // ID único del pastel
    private String name;             // Nombre del pastel
    private String description;      // Descripción detallada
    private double price;            // Precio en COP
    private String category;         // Categoría del pastel
    private String imageUrl;
    private String status;           // Estado: active, inactive
    private String size;             // Tamaño del pastel
    private long createdAt;          // Timestamp de creación
    private long updatedAt;          // Timestamp de última actualización
    private String createdBy;        // UID del admin que lo creó
    private int servings;            // Número de porciones

    // Constructor vacío requerido por Firebase
    public Cake() {}

    // Constructor básico
    public Cake(String name, String description, double price, String category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.status = STATUS_ACTIVE;
        this.size = SIZE_MEDIUM;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
        this.servings = 10; // Por defecto 10 porciones
    }

    // Constructor completo
    public Cake(String name, String description, double price, String category,
                String size, String createdBy) {
        this(name, description, price, category);
        this.size = size;
        this.createdBy = createdBy;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) {
        this.name = name;
        updateTimestamp();
    }

    public String getDescription() { return description; }
    public void setDescription(String description) {
        this.description = description;
        updateTimestamp();
    }

    public double getPrice() { return price; }
    public void setPrice(double price) {
        this.price = price;
        updateTimestamp();
    }

    public String getCategory() { return category; }
    public void setCategory(String category) {
        this.category = category;
        updateTimestamp();
    }

    @PropertyName("imageUrl")
    public String getImageUrl() { return imageUrl; }
    @PropertyName("imageUrl")
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        updateTimestamp();
    }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
        updateTimestamp();
    }

    public String getSize() { return size; }
    public void setSize(String size) {
        this.size = size;
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

    public int getServings() { return servings; }
    public void setServings(int servings) {
        this.servings = servings;
        updateTimestamp();
    }

    // Métodos de utilidad
    public boolean isActive() {
        return STATUS_ACTIVE.equals(status);
    }

    public boolean isInactive() {
        return STATUS_INACTIVE.equals(status);
    }

    public void activate() {
        setStatus(STATUS_ACTIVE);
    }

    public void deactivate() {
        setStatus(STATUS_INACTIVE);
    }

    // Actualizar timestamp de modificación
    private void updateTimestamp() {
        this.updatedAt = System.currentTimeMillis();
    }

    // Calcular precio formateado
    public String getFormattedPrice() {
        return String.format("$%,.0f COP", price);
    }

    // Verificar si tiene imagen
    public boolean hasImage() {
        return imageUrl != null && !imageUrl.trim().isEmpty();
    }

    // Convertir a Map para Firebase
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("name", name);
        result.put("description", description);
        result.put("price", price);
        result.put("category", category);
        result.put("imageUrl", imageUrl);
        result.put("status", status);
        result.put("size", size);
        result.put("createdAt", createdAt);
        result.put("updatedAt", updatedAt);
        result.put("createdBy", createdBy);
        result.put("servings", servings);
        return result;
    }

    // Validar categoría válida
    public static boolean isValidCategory(String category) {
        return CATEGORY_CHOCOLATE.equals(category) ||
                CATEGORY_VANILLA.equals(category) ||
                CATEGORY_FRUIT.equals(category) ||
                CATEGORY_CHEESECAKE.equals(category) ||
                CATEGORY_SPECIAL.equals(category) ||
                CATEGORY_BIRTHDAY.equals(category) ||
                CATEGORY_WEDDING.equals(category);
    }

    // Validar tamaño válido
    public static boolean isValidSize(String size) {
        return SIZE_SMALL.equals(size) ||
                SIZE_MEDIUM.equals(size) ||
                SIZE_LARGE.equals(size) ||
                SIZE_XLARGE.equals(size);
    }

    // Obtener descripción del tamaño
    public String getSizeDescription() {
        switch (size) {
            case SIZE_SMALL: return "Pequeño";
            case SIZE_MEDIUM: return "Mediano";
            case SIZE_LARGE: return "Grande";
            case SIZE_XLARGE: return "Extra Grande";
            default: return "Tamaño no especificado";
        }
    }

    @Override
    public String toString() {
        return "Cake{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", category='" + category + '\'' +
                ", status='" + status + '\'' +
                ", size='" + size + '\'' +
                '}';
    }
}