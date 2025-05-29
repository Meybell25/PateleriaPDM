package com.example.pasteleriapdm.Utils;

import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

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

    // Metodo para obtener instancia unica
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
            callback.onError("El UID del usuario no puede ser nulo.");
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

                    String mensajeError;
                    String error = e.getMessage();

                    if (error != null && error.contains("correo electronico ya en uso")) {
                        mensajeError = "Este email ya esta registrado.";
                    } else if (error != null && error.contains("contrasena debil")) {
                        mensajeError = "La contrasena es muy debil.";
                    } else if (error != null && error.contains("correo invalido")) {
                        mensajeError = "Correo invalido.";
                    } else {
                        mensajeError = "Error al crear el usuario: " + error;
                    }

                    callback.onError(mensajeError);
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
        // Crear mapa de actualizaciones sin incluir campos null o vacios
        Map<String, Object> updates = new HashMap<>();

        // Campos obligatorios que siempre se actualizan
        updates.put("name", user.getName());
        updates.put("role", user.getRole());
        updates.put("status", user.getStatus());

        // Solo actualizar contrasena si no esta vacia
        if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
            updates.put("password", user.getPassword());
            Log.d(TAG, "Actualizando contrasena del usuario");
        } else {
            Log.d(TAG, "No se actualiza la contrasena (esta vacia)");
        }

        // Agregar: Actualizar lastLogin si tiene un valor valido
        if (user.getLastLogin() > 0) {
            updates.put("lastLogin", user.getLastLogin());
            Log.d(TAG, "Actualizando lastLogin del usuario: " + user.getLastLogin());
        }

        // Timestamp de actualizacion
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

        Log.d(TAG, "Iniciando creacion del primer administrador con UID: " + user.getUid());

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

                            // Asegurarse que sea admin y activo
                            user.setRole(User.ROLE_ADMIN);
                            user.setStatus(User.STATUS_ACTIVE);
                            user.setCreatedAt(System.currentTimeMillis());
                            user.setLastLogin(0);
                            // Crear el Map COMPLETO  (necesario para primer admin)
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

    /**
     * Eliminar usuario permanentemente (solo usuarios inactivos)
     */
    public void deleteUser(String uid, DatabaseCallback<Boolean> callback) {
        if (uid == null || uid.trim().isEmpty()) {
            callback.onError("UID del usuario no puede ser null o vacio");
            return;
        }

        Log.d(TAG, "Iniciando eliminacion del usuario: " + uid);

        // Primero verificar que el usuario existe y esta inactivo
        databaseRef.child(USERS_NODE).child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            callback.onError("Usuario no encontrado");
                            return;
                        }

                        User user = snapshot.getValue(User.class);
                        if (user == null) {
                            callback.onError("Error obteniendo datos del usuario");
                            return;
                        }

                        // Verificar que el usuario este inactivo
                        if (!User.STATUS_INACTIVE.equals(user.getStatus())) {
                            String estadoActual = user.getStatus();
                            String mensajeError;

                            if (User.STATUS_ACTIVE.equals(estadoActual)) {
                                mensajeError = "No se puede eliminar un usuario ACTIVO. Desactivalo primero.";
                            } else if (User.STATUS_BLOCKED.equals(estadoActual)) {
                                mensajeError = "No se puede eliminar un usuario BLOQUEADO. Solo usuarios INACTIVOS.";
                            } else {
                                mensajeError = "Solo se pueden eliminar usuarios con estado INACTIVO.";
                            }

                            Log.w(TAG, "Intento de eliminar usuario con estado: " + estadoActual);
                            callback.onError(mensajeError);
                            return;
                        }

                        // Si llegamos aqui, el usuario esta inactivo y puede ser eliminado
                        Log.d(TAG, "Usuario verificado como INACTIVO, procediendo con eliminacion");

                        // Proceder con la eliminacion
                        databaseRef.child(USERS_NODE).child(uid)
                                .removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Usuario eliminado exitosamente de la base de datos: " + uid);
                                    callback.onSuccess(true);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error eliminando usuario de la base de datos", e);
                                    callback.onError("Error eliminando usuario: " + e.getMessage());
                                });
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e(TAG, "Error verificando usuario para eliminacion", error.toException());
                        callback.onError("Error verificando usuario: " + error.getMessage());
                    }
                });
    }

    // ==================== METODOS PARA OBTENER INFORMACION DEL USUARIO ====================

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


    /**
     * Crear un nuevo pastel con imagen en Firebase Storage
     */
    public void createCakeWithImage(Cake cake, Uri imageUri, DatabaseCallback<Cake> callback) {
        // Generar ID del pastel primero
        String cakeId = databaseRef.child(CAKES_NODE).push().getKey();
        if (cakeId == null) {
            callback.onError("Error generando ID del pastel");
            return;
        }

        cake.setId(cakeId);

        // Si no hay imagen, crear pastel sin imagen
        if (imageUri == null) {
            createCake(cake, callback);
            return;
        }

        // Subir imagen primero
        StorageHelper.getInstance().uploadCakeImage(imageUri, cakeId, new StorageHelper.StorageCallback() {
            @Override
            public void onSuccess(String downloadUrl, String storagePath) {
                // Configurar datos de imagen en el pastel
                cake.setImageUrl(downloadUrl);
                cake.setImagePath(storagePath);
                cake.setImageFileName(getFileNameFromPath(storagePath));

                // Crear pastel en la base de datos
                createCake(cake, callback);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error subiendo imagen del pastel", new Exception(error));
                callback.onError("Error subiendo imagen: " + error);
            }

            @Override
            public void onProgress(int progress) {
                Log.d(TAG, "Progreso subida imagen: " + progress + "%");
            }
        });
    }

    /**
     * Actualizar pastel con nueva imagen
     */
    public void updateCakeWithImage(Cake cake, Uri newImageUri, DatabaseCallback<Cake> callback) {
        if (newImageUri == null) {
            // No hay nueva imagen, solo actualizar datos
            updateCake(cake, callback);
            return;
        }

        // Obtener ruta de imagen anterior para eliminarla
        String oldImagePath = cake.getImagePath();

        // Subir nueva imagen
        StorageHelper.getInstance().updateCakeImage(newImageUri, cake.getId(), oldImagePath,
                new StorageHelper.StorageCallback() {
                    @Override
                    public void onSuccess(String downloadUrl, String storagePath) {
                        // Actualizar datos de imagen en el pastel
                        cake.setImageUrl(downloadUrl);
                        cake.setImagePath(storagePath);
                        cake.setImageFileName(getFileNameFromPath(storagePath));

                        // Actualizar pastel en la base de datos
                        updateCake(cake, callback);
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Error actualizando imagen del pastel", new Exception(error));
                        callback.onError("Error actualizando imagen: " + error);
                    }

                    @Override
                    public void onProgress(int progress) {
                        Log.d(TAG, "Progreso actualización imagen: " + progress + "%");
                    }
                });
    }

    /**
     * Eliminar pastel y su imagen de Storage
     */
    public void deleteCakeWithImage(String cakeId, DatabaseCallback<Boolean> callback) {
        // Primero obtener datos del pastel para conseguir la ruta de la imagen
        databaseRef.child(CAKES_NODE).child(cakeId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            callback.onError("El pastel no existe");
                            return;
                        }

                        try {
                            Cake cake = snapshot.getValue(Cake.class);
                            if (cake != null && cake.hasStorageImage()) {
                                // Eliminar imagen primero
                                StorageHelper.getInstance().deleteCakeImage(cake.getImagePath(),
                                        new StorageHelper.DeleteCallback() {
                                            @Override
                                            public void onSuccess() {
                                                // Imagen eliminada, ahora eliminar pastel de BD
                                                permanentDeleteCake(cakeId, callback);
                                            }

                                            @Override
                                            public void onError(String error) {
                                                Log.w(TAG, "No se pudo eliminar imagen: " + error);
                                                // Continuar eliminando el pastel aunque falle la imagen
                                                permanentDeleteCake(cakeId, callback);
                                            }
                                        });
                            } else {
                                // No hay imagen, solo eliminar pastel
                                permanentDeleteCake(cakeId, callback);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error procesando datos del pastel para eliminar", e);
                            callback.onError("Error procesando pastel: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error obteniendo pastel para eliminar", error.toException());
                        callback.onError("Error obteniendo pastel: " + error.getMessage());
                    }
                });
    }

    /**
     * Eliminar solo la imagen de un pastel (mantener el pastel sin imagen)
     */
    public void removeCakeImage(String cakeId, DatabaseCallback<Cake> callback) {
        databaseRef.child(CAKES_NODE).child(cakeId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            callback.onError("El pastel no existe");
                            return;
                        }

                        try {
                            Cake cake = snapshot.getValue(Cake.class);
                            if (cake != null && cake.hasStorageImage()) {
                                // Eliminar imagen de Storage
                                StorageHelper.getInstance().deleteCakeImage(cake.getImagePath(),
                                        new StorageHelper.DeleteCallback() {
                                            @Override
                                            public void onSuccess() {
                                                // Limpiar datos de imagen del pastel
                                                cake.setImageUrl(null);
                                                cake.setImagePath(null);
                                                cake.setImageFileName(null);

                                                // Actualizar pastel en BD
                                                updateCake(cake, callback);
                                            }

                                            @Override
                                            public void onError(String error) {
                                                Log.e(TAG, "Error eliminando imagen", new Exception(error));
                                                callback.onError("Error eliminando imagen: " + error);
                                            }
                                        });
                            } else {
                                callback.onError("El pastel no tiene imagen para eliminar");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error procesando pastel para eliminar imagen", e);
                            callback.onError("Error procesando pastel: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error obteniendo pastel", error.toException());
                        callback.onError("Error obteniendo pastel: " + error.getMessage());
                    }
                });
    }

    /**
     * Método helper para extraer nombre de archivo de la ruta de Storage
     */
    private String getFileNameFromPath(String storagePath) {
        if (storagePath == null || storagePath.isEmpty()) return null;

        int lastSlash = storagePath.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < storagePath.length() - 1) {
            return storagePath.substring(lastSlash + 1);
        }
        return storagePath;
    }

    /**
     * Validar y regenerar URLs de descarga para pasteles existentes
     *
     */
    public void refreshCakeImageUrls(DatabaseCallback<Integer> callback) {
        getAllCakes(new DatabaseCallback<List<Cake>>() {
            @Override
            public void onSuccess(List<Cake> cakes) {
                int[] refreshCount = {0};
                int[] totalToRefresh = {0};

                // Contar cuántos pasteles tienen imágenes para refrescar
                for (Cake cake : cakes) {
                    if (cake.hasStorageImage()) {
                        totalToRefresh[0]++;
                    }
                }

                if (totalToRefresh[0] == 0) {
                    callback.onSuccess(0);
                    return;
                }

                // Refrescar URLs
                for (Cake cake : cakes) {
                    if (cake.hasStorageImage()) {
                        StorageHelper.getInstance().getImageDownloadUrl(cake.getImagePath(),
                                new StorageHelper.StorageCallback() {
                                    @Override
                                    public void onSuccess(String downloadUrl, String storagePath) {
                                        cake.setImageUrl(downloadUrl);
                                        updateCake(cake, new DatabaseCallback<Cake>() {
                                            @Override
                                            public void onSuccess(Cake result) {
                                                refreshCount[0]++;
                                                if (refreshCount[0] == totalToRefresh[0]) {
                                                    callback.onSuccess(refreshCount[0]);
                                                }
                                            }

                                            @Override
                                            public void onError(String error) {
                                                Log.w(TAG, "Error actualizando URL de " + cake.getName() + ": " + error);
                                                refreshCount[0]++;
                                                if (refreshCount[0] == totalToRefresh[0]) {
                                                    callback.onSuccess(refreshCount[0]);
                                                }
                                            }
                                        });
                                    }

                                    @Override
                                    public void onError(String error) {
                                        Log.w(TAG, "Error obteniendo URL para " + cake.getName() + ": " + error);
                                        refreshCount[0]++;
                                        if (refreshCount[0] == totalToRefresh[0]) {
                                            callback.onSuccess(refreshCount[0]);
                                        }
                                    }

                                    @Override
                                    public void onProgress(int progress) {
                                        // No necesario para esta operación
                                    }
                                });
                    }
                }
            }

            @Override
            public void onError(String error) {
                callback.onError("Error obteniendo pasteles: " + error);
            }
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
    public void getClientsBySeller(String sellerId, DatabaseCallback<List<Client>> callback) {
        databaseRef.child(CLIENTS_NODE)
                .orderByChild("createdBy")
                .equalTo(sellerId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<Client> clients = new ArrayList<>();
                        for (DataSnapshot clientSnapshot : snapshot.getChildren()) {
                            Client client = clientSnapshot.getValue(Client.class);
                            if (client != null) {
                                client.setId(clientSnapshot.getKey());
                                // Asegurarse que createdBy no sea null
                                if (client.getCreatedBy() == null) {
                                    client.setCreatedBy(sellerId); // Forzar el valor si es null
                                }
                                clients.add(client);
                            }
                        }
                        callback.onSuccess(clients);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        callback.onError(error.getMessage());
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
     * Actualizar cliente - Version CORREGIDA para permisos
     */
    public void updateClient(Client client, DatabaseCallback<Client> callback) {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        getUser(currentUserId, new DatabaseCallback<User>() {
            @Override
            public void onSuccess(User currentUser) {
                if (currentUser == null) {
                    callback.onError("Usuario no encontrado");
                    return;
                }

                Log.d(TAG, "Usuario actual: " + currentUser.getName() +
                        " - Rol: " + currentUser.getRole() +
                        " - Es Admin: " + currentUser.isAdmin());
                Log.d(TAG, "Cliente a editar - ID: " + client.getId() +
                        " - Nombre: " + client.getName() +
                        " - Creado por: " + client.getCreatedBy());

                // Admin puede editar cualquier cliente
                if (currentUser.isAdmin()) {
                    Log.d(TAG, "Admin detectado, procediendo con actualizacion");
                    procederConActualizacion(client, callback);
                    return;
                }

                // Seller solo puede editar sus clientes
                if ("seller".equals(currentUser.getRole())) {
                    if (client.getCreatedBy() != null && client.getCreatedBy().equals(currentUserId)) {
                        Log.d(TAG, "Seller autorizado (es el creador), procediendo con actualizacion");
                        procederConActualizacion(client, callback);
                    } else {
                        String errorMsg = "Seller NO autorizado - Cliente creado por: " +
                                client.getCreatedBy() + " - Usuario actual: " + currentUserId;
                        Log.e(TAG, errorMsg);
                        callback.onError("No tienes permisos para editar este cliente");
                    }
                } else {
                    callback.onError("No tienes permisos para editar clientes");
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error verificando usuario: " + error);
                callback.onError("Error verificando usuario: " + error);
            }
        });
    }

    private void procederConActualizacion(Client client, DatabaseCallback<Client> callback) {
        // CRUCIAL: Preservar el createdBy original y otros campos inmutables
        Map<String, Object> updates = new HashMap<>();

        // Solo actualizar campos que pueden cambiar
        updates.put("name", client.getName());
        updates.put("phone", client.getPhone());
        updates.put("address", client.getAddress());
        updates.put("status", client.getStatus());
        updates.put("updatedAt", System.currentTimeMillis());

        Log.d(TAG, "Actualizando cliente: " + client.getId() + " con datos: " + updates.toString());

        databaseRef.child(CLIENTS_NODE).child(client.getId())
                .updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Cliente actualizado exitosamente: " + client.getId());
                    callback.onSuccess(client);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error actualizando cliente: " + e.getMessage(), e);
                    callback.onError("Error actualizando cliente: " + e.getMessage());
                });
    }

    /**
     * Eliminar cliente (solo clientes inactivos)
     */
    public void deleteClient(String clientId, DatabaseCallback<Void> callback) {
        // Primero verificar que el cliente este inactivo
        databaseRef.child(CLIENTS_NODE).child(clientId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        Client client = snapshot.getValue(Client.class);
                        if (client == null) {
                            callback.onError("Cliente no encontrado");
                            return;
                        }

                        if (!Client.STATUS_INACTIVE.equals(client.getStatus())) {
                            callback.onError("Solo se pueden eliminar clientes inactivos");
                            return;
                        }

                        // Proceder con la eliminacion
                        databaseRef.child(CLIENTS_NODE).child(clientId)
                                .removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Cliente eliminado exitosamente: " + clientId);
                                    callback.onSuccess(null);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error eliminando cliente", e);
                                    callback.onError("Error eliminando cliente: " + e.getMessage());
                                });
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e(TAG, "Error verificando cliente", error.toException());
                        callback.onError("Error verificando cliente: " + error.getMessage());
                    }
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
     * Obtener reservas para produccion
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
                                // Solo reservas confirmadas o en produccion
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
                        Log.e(TAG, "Error obteniendo reservas de produccion", error.toException());
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

    // ==================== METODOS AUXILIARES ====================

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
     * Escuchar cambios en reservas de produccion
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
                        callback.onError("Error escuchando reservas de produccion: " + error.getMessage());
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

    // ==================== METODO PARA OBTENER EL TOTAL DE USUARIOS ====================
    /**
     * Obtiene el total de usuarios en la base de datos
     * @param callback Callback para recibir el resultado
     */
    public void obtenerTotalUsuarios(DatabaseCallback<Integer> callback) {
        Log.d(TAG, "Obteniendo total de usuarios...");

        databaseRef.child(USERS_NODE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        int total = (int) snapshot.getChildrenCount();
                        Log.d(TAG, "Total de usuarios: " + total);
                        callback.onSuccess(total);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e(TAG, "Error obteniendo total de usuarios", error.toException());
                        callback.onError("Error: " + error.getMessage());
                    }
                });
    }

    // ==================== METODO PARA OBTENER EL TOTAL DE CLIENTES ====================
    /**
     * Obtiene el total de clientes en la base de datos
     * @param callback Callback para recibir el resultado
     */
    public void obtenerTotalClientes(DatabaseCallback<Integer> callback) {
        Log.d(TAG, "Obteniendo total de clientes...");

        databaseRef.child(CLIENTS_NODE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        int total = (int) snapshot.getChildrenCount();
                        Log.d(TAG, "Total de clientes: " + total);
                        callback.onSuccess(total);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e(TAG, "Error obteniendo total de clientes", error.toException());
                        callback.onError("Error: " + error.getMessage());
                    }
                });
    }


// ==================== METODO PARA OBTENER EL TOTAL DE PASTELES ====================
    /**
     * Obtiene el total de pasteles en la base de datos
     * @param callback Callback para recibir el resultado
     */
    public void obtenerTotalPasteles(DatabaseCallback<Integer> callback) {
        Log.d(TAG, "Obteniendo total de pasteles...");

        databaseRef.child(CAKES_NODE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        int total = (int) snapshot.getChildrenCount();
                        Log.d(TAG, "Total de pasteles: " + total);
                        callback.onSuccess(total);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e(TAG, "Error obteniendo total de pasteles", error.toException());
                        callback.onError("Error: " + error.getMessage());
                    }
                });
    }

    // ==================== METODO PARA OBTENER EL TOTAL DE RESERVAS ====================
    /**
     * Obtiene el total de reservas en la base de datos
     * @param callback Callback para recibir el resultado
     */
    public void obtenerTotalReservas(DatabaseCallback<Integer> callback) {
        Log.d(TAG, "Obteniendo total de reservas...");

        databaseRef.child(RESERVATIONS_NODE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        int total = (int) snapshot.getChildrenCount();
                        Log.d(TAG, "Total de reservas: " + total);
                        callback.onSuccess(total);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e(TAG, "Error obteniendo total de reservas", error.toException());
                        callback.onError("Error: " + error.getMessage());
                    }
                });
    }

}