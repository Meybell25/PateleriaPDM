package com.example.pasteleriapdm.Utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.pasteleriapdm.Models.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Clase helper para operaciones con Firebase Realtime Database
 * Centraliza todas las operaciones CRUD y manejo de datos
 */
public class DatabaseHelper {

    private static final String TAG = "DatabaseHelper";

    // Referencias a nodos principales
    private static final String USERS_NODE = "users";
    private static final String CAKES_NODE = "cakes";
    private static final String CLIENTS_NODE = "clients";
    private static final String RESERVATIONS_NODE = "reservations";

    private DatabaseReference databaseRef;
    private FirebaseAuth firebaseAuth;
    private static DatabaseHelper instance;

    // Constructor privado para Singleton
    private DatabaseHelper() {
        databaseRef = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
    }

    // Método para obtener instancia única
    public static synchronized DatabaseHelper getInstance() {
        if (instance == null) {
            instance = new DatabaseHelper();
        }
        return instance;
    }

    // ==================== OPERACIONES DE USUARIOS ====================

    /**
     * Crear un nuevo usuario en la base de datos
     */
    public void createUser(User user, DatabaseCallback<User> callback) {
        if (user.getUid() == null) {
            callback.onError("UID del usuario no puede ser null");
            return;
        }

        databaseRef.child(USERS_NODE).child(user.getUid())
                .setValue(user.toMap())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Usuario creado exitosamente: " + user.getUid());
                    callback.onSuccess(user);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creando usuario", e);
                    callback.onError("Error creando usuario: " + e.getMessage());
                });
    }

    /**
     * Obtener usuario por UID
     */
    public void getUser(String uid, DatabaseCallback<User> callback) {
        databaseRef.child(USERS_NODE).child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            User user = snapshot.getValue(User.class);
                            if (user != null) {
                                user.setUid(snapshot.getKey());
                                callback.onSuccess(user);
                            } else {
                                callback.onError("Error deserializando usuario");
                            }
                        } else {
                            callback.onError("Usuario no encontrado");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e(TAG, "Error obteniendo usuario", error.toException());
                        callback.onError("Error obteniendo usuario: " + error.getMessage());
                    }
                });
    }

    /**
     * Obtener todos los usuarios (solo admin)
     */
    public void getAllUsers(DatabaseCallback<List<User>> callback) {
        databaseRef.child(USERS_NODE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<User> users = new ArrayList<>();
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            User user = userSnapshot.getValue(User.class);
                            if (user != null) {
                                user.setUid(userSnapshot.getKey());
                                users.add(user);
                            }
                        }
                        callback.onSuccess(users);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e(TAG, "Error obteniendo usuarios", error.toException());
                        callback.onError("Error obteniendo usuarios: " + error.getMessage());
                    }
                });
    }

    /**
     * Actualizar usuario
     */
    public void updateUser(User user, DatabaseCallback<User> callback) {
        // Crear mapa de actualizaciones sin incluir campos null o vacíos
        Map<String, Object> updates = new HashMap<>();

        // Campos obligatorios que siempre se actualizan
        updates.put("name", user.getName());
        updates.put("role", user.getRole());
        updates.put("status", user.getStatus());

        // Solo actualizar contraseña si no está vacía
        if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
            updates.put("password", user.getPassword());
            Log.d(TAG, "Actualizando contraseña del usuario");
        } else {
            Log.d(TAG, "No se actualiza la contraseña (está vacía)");
        }

        // Agregar: Actualizar lastLogin si tiene un valor valido
        if (user.getLastLogin() > 0) {
            updates.put("lastLogin", user.getLastLogin());
            Log.d(TAG, "Actualizando lastLogin del usuario: " + user.getLastLogin());
        }

        // Timestamp de actualización
        updates.put("updatedAt", System.currentTimeMillis());

        Log.d(TAG, "Actualizando usuario con UID: " + user.getUid());
        Log.d(TAG, "Campos a actualizar: " + updates.keySet().toString());

        databaseRef.child(USERS_NODE).child(user.getUid())
                .updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Usuario actualizado exitosamente");
                    callback.onSuccess(user);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error actualizando usuario", e);
                    callback.onError("Error actualizando usuario: " + e.getMessage());
                });
    }

    /**
     * Crear el primer usuario administrador del sistema
     * Este metodo incluye validaciones especiales para el primer usuario
     */
    public void createFirstAdmin(User user, DatabaseCallback<User> callback) {
        if (user.getUid() == null) {
            callback.onError("UID del usuario no puede ser null");
            return;
        }

        Log.d(TAG, "Iniciando creación del primer administrador con UID: " + user.getUid());

        // Verificar primero si ya existen usuarios
        databaseRef.child(USERS_NODE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                            Log.d(TAG, "Ya existen usuarios en el sistema");
                            // Ya existen usuarios, usar metodo normal
                            createUser(user, callback);
                        } else {
                            // No hay usuarios, crear el primer admin
                            Log.d(TAG, "No hay usuarios existentes, creando primer administrador");

                            // Asegurar que sea admin y activo
                            user.setRole(User.ROLE_ADMIN);
                            user.setStatus(User.STATUS_ACTIVE);
                            user.setCreatedAt(System.currentTimeMillis());
                            user.setLastLogin(0);

                            // Crear el Map COMPLETO incluyendo la contraseña (necesario para primer admin)
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("uid", user.getUid());
                            userData.put("name", user.getName());
                            userData.put("email", user.getEmail());
                            userData.put("password", user.getPassword());
                            userData.put("role", user.getRole());
                            userData.put("status", user.getStatus());
                            userData.put("createdAt", user.getCreatedAt());
                            userData.put("lastLogin", user.getLastLogin());

                            Log.d(TAG, "Datos del primer admin a guardar: " + userData.toString());

                            // Crear directamente sin las restricciones normales
                            databaseRef.child(USERS_NODE).child(user.getUid())
                                    .setValue(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Primer administrador creado exitosamente en BD: " + user.getUid());
                                        callback.onSuccess(user);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error creando primer administrador en BD", e);
                                        Log.e(TAG, "Detalles del error: " + e.getMessage());
                                        callback.onError("Error creando primer administrador: " + e.getMessage());
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e(TAG, "Error verificando existencia de usuarios", error.toException());
                        callback.onError("Error verificando usuarios existentes: " + error.getMessage());
                    }
                });
    }

    /**
     * Verificar si existen usuarios en el sistema
     */
    public void checkIfUsersExist(DatabaseCallback<Boolean> callback) {
        databaseRef.child(USERS_NODE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        boolean usersExist = snapshot.exists() && snapshot.getChildrenCount() > 0;
                        callback.onSuccess(usersExist);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e(TAG, "Error verificando existencia de usuarios", error.toException());
                        callback.onError("Error verificando usuarios: " + error.getMessage());
                    }
                });
    }
    // ==================== MÉTODOS PARA OBTENER INFORMACIÓN DEL USUARIO ====================

    /**
     * Obtener usuario por ID
     */
    public void getUserById(String userId, DatabaseCallback<User> callback) {
        databaseRef.child(USERS_NODE).child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            user.setUid(snapshot.getKey());
                            callback.onSuccess(user);
                        } else {
                            callback.onError("Usuario no encontrado");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e(TAG, "Error obteniendo usuario", error.toException());
                        callback.onError("Error obteniendo usuario: " + error.getMessage());
                    }
                });
    }



    // ==================== OPERACIONES DE PASTELES ====================

    /**
     * Crear un nuevo pastel
     */
    public void createCake(Cake cake, DatabaseCallback<Cake> callback) {
        String cakeId = databaseRef.child(CAKES_NODE).push().getKey();
        if (cakeId == null) {
            callback.onError("Error generando ID del pastel");
            return;
        }

        cake.setId(cakeId);
        databaseRef.child(CAKES_NODE).child(cakeId)
                .setValue(cake.toMap())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Pastel creado exitosamente: " + cakeId);
                    callback.onSuccess(cake);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creando pastel", e);
                    callback.onError("Error creando pastel: " + e.getMessage());
                });
    }

    /**
     * Obtener solo pasteles activos (para usar en reservas)
     */
    public void getActiveCakes(DatabaseCallback<List<Cake>> callback) {
        databaseRef.child(CAKES_NODE)
                .orderByChild("status")
                .equalTo(Cake.STATUS_ACTIVE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Cake> activeCakes = new ArrayList<>();
                        for (DataSnapshot cakeSnapshot : snapshot.getChildren()) {
                            try {
                                Cake cake = cakeSnapshot.getValue(Cake.class);
                                if (cake != null) {
                                    cake.setId(cakeSnapshot.getKey());
                                    activeCakes.add(cake);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing cake data", e);
                            }
                        }

                        Log.d(TAG, "Pasteles activos obtenidos: " + activeCakes.size());
                        callback.onSuccess(activeCakes);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error obteniendo pasteles activos", error.toException());
                        callback.onError("Error obteniendo pasteles activos: " + error.getMessage());
                    }
                });
    }

    /**
     * Obtener todos los pasteles (admin)
     */
    public void getAllCakes(DatabaseCallback<List<Cake>> callback) {
        databaseRef.child(CAKES_NODE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<Cake> cakes = new ArrayList<>();
                        for (DataSnapshot cakeSnapshot : snapshot.getChildren()) {
                            Cake cake = cakeSnapshot.getValue(Cake.class);
                            if (cake != null) {
                                cake.setId(cakeSnapshot.getKey());
                                cakes.add(cake);
                            }
                        }
                        callback.onSuccess(cakes);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e(TAG, "Error obteniendo todos los pasteles", error.toException());
                        callback.onError("Error obteniendo pasteles: " + error.getMessage());
                    }
                });
    }

    /**
     * Actualizar pastel
     */
    public void updateCake(Cake cake, DatabaseCallback<Cake> callback) {
        databaseRef.child(CAKES_NODE).child(cake.getId())
                .updateChildren(cake.toMap())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Pastel actualizado exitosamente");
                    callback.onSuccess(cake);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error actualizando pastel", e);
                    callback.onError("Error actualizando pastel: " + e.getMessage());
                });
    }

    /**
     * Eliminar pastel permanentemente (solo para casos extremos)
     */
    public void permanentDeleteCake(String cakeId, DatabaseCallback<Boolean> callback) {
        databaseRef.child(CAKES_NODE).child(cakeId)
                .removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Pastel eliminado permanentemente: " + cakeId);
                    callback.onSuccess(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error eliminando pastel permanentemente", e);
                    callback.onError("Error eliminando pastel: " + e.getMessage());
                });
    }

    // ==================== OPERACIONES DE CLIENTES ====================

    /**
     * Crear un nuevo cliente
     */
    public void createClient(Client client, DatabaseCallback<Client> callback) {
        String clientId = databaseRef.child(CLIENTS_NODE).push().getKey();
        if (clientId == null) {
            callback.onError("Error generando ID del cliente");
            return;
        }

        client.setId(clientId);
        databaseRef.child(CLIENTS_NODE).child(clientId)
                .setValue(client.toMap())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Cliente creado exitosamente: " + clientId);
                    callback.onSuccess(client);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creando cliente", e);
                    callback.onError("Error creando cliente: " + e.getMessage());
                });
    }

    /**
     * Obtener clientes por vendedor
     */
    public void getClientsBySeller(String sellerUid, DatabaseCallback<List<Client>> callback) {
        databaseRef.child(CLIENTS_NODE)
                .orderByChild("createdBy")
                .equalTo(sellerUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<Client> clients = new ArrayList<>();
                        for (DataSnapshot clientSnapshot : snapshot.getChildren()) {
                            Client client = clientSnapshot.getValue(Client.class);
                            if (client != null) {
                                client.setId(clientSnapshot.getKey());
                                clients.add(client);
                            }
                        }
                        callback.onSuccess(clients);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e(TAG, "Error obteniendo clientes", error.toException());
                        callback.onError("Error obteniendo clientes: " + error.getMessage());
                    }
                });
    }

    /**
     * Obtener todos los clientes (admin)
     */
    public void getAllClients(DatabaseCallback<List<Client>> callback) {
        databaseRef.child(CLIENTS_NODE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<Client> clients = new ArrayList<>();
                        for (DataSnapshot clientSnapshot : snapshot.getChildren()) {
                            Client client = clientSnapshot.getValue(Client.class);
                            if (client != null) {
                                client.setId(clientSnapshot.getKey());
                                clients.add(client);
                            }
                        }
                        callback.onSuccess(clients);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e(TAG, "Error obteniendo todos los clientes", error.toException());
                        callback.onError("Error obteniendo clientes: " + error.getMessage());
                    }
                });
    }

    /**
     * Actualizar cliente
     */
    public void updateClient(Client client, DatabaseCallback<Client> callback) {
        databaseRef.child(CLIENTS_NODE).child(client.getId())
                .updateChildren(client.toMap())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Cliente actualizado exitosamente");
                    callback.onSuccess(client);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error actualizando cliente", e);
                    callback.onError("Error actualizando cliente: " + e.getMessage());
                });
    }

    // ==================== OPERACIONES DE RESERVAS ====================

    /**
     * Crear una nueva reserva
     */
    public void createReservation(Reservation reservation, DatabaseCallback<Reservation> callback) {
        String reservationId = databaseRef.child(RESERVATIONS_NODE).push().getKey();
        if (reservationId == null) {
            callback.onError("Error generando ID de la reserva");
            return;
        }

        reservation.setId(reservationId);
        databaseRef.child(RESERVATIONS_NODE).child(reservationId)
                .setValue(reservation.toMap())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Reserva creada exitosamente: " + reservationId);
                    callback.onSuccess(reservation);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creando reserva", e);
                    callback.onError("Error creando reserva: " + e.getMessage());
                });
    }

    /**
     * Obtener reservas por vendedor
     */
    public void getReservationsBySeller(String sellerUid, DatabaseCallback<List<Reservation>> callback) {
        databaseRef.child(RESERVATIONS_NODE)
                .orderByChild("createdBy")
                .equalTo(sellerUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<Reservation> reservations = new ArrayList<>();
                        for (DataSnapshot reservationSnapshot : snapshot.getChildren()) {
                            Reservation reservation = reservationSnapshot.getValue(Reservation.class);
                            if (reservation != null) {
                                reservation.setId(reservationSnapshot.getKey());
                                reservations.add(reservation);
                            }
                        }
                        callback.onSuccess(reservations);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e(TAG, "Error obteniendo reservas", error.toException());
                        callback.onError("Error obteniendo reservas: " + error.getMessage());
                    }
                });
    }

    /**
     * Obtener reservas para producción
     */
    public void getProductionReservations(DatabaseCallback<List<Reservation>> callback) {
        databaseRef.child(RESERVATIONS_NODE)
                .orderByChild("status")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<Reservation> reservations = new ArrayList<>();
                        for (DataSnapshot reservationSnapshot : snapshot.getChildren()) {
                            Reservation reservation = reservationSnapshot.getValue(Reservation.class);
                            if (reservation != null) {
                                // Solo reservas confirmadas o en producción
                                if (reservation.isConfirmed() || reservation.isInProduction() ||
                                        reservation.isReady() || reservation.isDelivered()) {
                                    reservation.setId(reservationSnapshot.getKey());
                                    reservations.add(reservation);
                                }
                            }
                        }
                        callback.onSuccess(reservations);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e(TAG, "Error obteniendo reservas de producción", error.toException());
                        callback.onError("Error obteniendo reservas: " + error.getMessage());
                    }
                });
    }

    /**
     * Obtener todas las reservas (admin)
     */
    public void getAllReservations(DatabaseCallback<List<Reservation>> callback) {
        databaseRef.child(RESERVATIONS_NODE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<Reservation> reservations = new ArrayList<>();
                        for (DataSnapshot reservationSnapshot : snapshot.getChildren()) {
                            Reservation reservation = reservationSnapshot.getValue(Reservation.class);
                            if (reservation != null) {
                                reservation.setId(reservationSnapshot.getKey());
                                reservations.add(reservation);
                            }
                        }
                        callback.onSuccess(reservations);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e(TAG, "Error obteniendo todas las reservas", error.toException());
                        callback.onError("Error obteniendo reservas: " + error.getMessage());
                    }
                });
    }

    /**
     * Actualizar reserva
     */
    public void updateReservation(Reservation reservation, DatabaseCallback<Reservation> callback) {
        databaseRef.child(RESERVATIONS_NODE).child(reservation.getId())
                .updateChildren(reservation.toMap())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Reserva actualizada exitosamente");
                    callback.onSuccess(reservation);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error actualizando reserva", e);
                    callback.onError("Error actualizando reserva: " + e.getMessage());
                });
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Obtener usuario actual
     */
    public String getCurrentUserId() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        return currentUser != null ? currentUser.getUid() : null;
    }

    /**
     * Verificar si hay usuario autenticado
     */
    public boolean isUserAuthenticated() {
        return firebaseAuth.getCurrentUser() != null;
    }

    /**
     * Interface para callbacks de base de datos
     */
    public interface DatabaseCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    // ==================== LISTENERS EN TIEMPO REAL ====================

    /**
     * Escuchar cambios en reservas por vendedor
     */
    public void listenToSellerReservations(String sellerUid, DatabaseCallback<List<Reservation>> callback) {
        databaseRef.child(RESERVATIONS_NODE)
                .orderByChild("createdBy")
                .equalTo(sellerUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<Reservation> reservations = new ArrayList<>();
                        for (DataSnapshot reservationSnapshot : snapshot.getChildren()) {
                            Reservation reservation = reservationSnapshot.getValue(Reservation.class);
                            if (reservation != null) {
                                reservation.setId(reservationSnapshot.getKey());
                                reservations.add(reservation);
                            }
                        }
                        callback.onSuccess(reservations);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        callback.onError("Error escuchando reservas: " + error.getMessage());
                    }
                });
    }

    /**
     * Escuchar cambios en reservas de producción
     */
    public void listenToProductionReservations(DatabaseCallback<List<Reservation>> callback) {
        databaseRef.child(RESERVATIONS_NODE)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<Reservation> reservations = new ArrayList<>();
                        for (DataSnapshot reservationSnapshot : snapshot.getChildren()) {
                            Reservation reservation = reservationSnapshot.getValue(Reservation.class);
                            if (reservation != null &&
                                    (reservation.isConfirmed() || reservation.isInProduction() ||
                                            reservation.isReady() || reservation.isDelivered())) {
                                reservation.setId(reservationSnapshot.getKey());
                                reservations.add(reservation);
                            }
                        }
                        callback.onSuccess(reservations);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        callback.onError("Error escuchando reservas de producción: " + error.getMessage());
                    }
                });
    }

    /**
     * Remover todos los listeners
     */
    public void removeAllListeners() {
        databaseRef.removeEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {}
            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }
}