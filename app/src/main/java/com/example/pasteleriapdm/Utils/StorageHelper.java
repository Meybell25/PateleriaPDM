package com.example.pasteleriapdm.Utils;

import android.net.Uri;
import android.util.Log;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

/**
 * Helper class para manejar operaciones con Firebase Storage
 */
public class StorageHelper {

    private static final String TAG = "StorageHelper";
    private static StorageHelper instance;

    // Referencias de Firebase Storage
    private final FirebaseStorage storage;
    private final StorageReference storageRef;
    private final StorageReference cakesImagesRef;

    // Constantes
    private static final String CAKES_FOLDER = "cakes";
    private static final String STORAGE_BUCKET = "gs://pasteleriapdm.firebasestorage.app";
    private static final int MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    // Constructor privado (Singleton)
    private StorageHelper() {
        try {
            // Configurar Firebase Storage con el bucket específico
            storage = FirebaseStorage.getInstance(STORAGE_BUCKET);
            storageRef = storage.getReference();
            cakesImagesRef = storageRef.child(CAKES_FOLDER);

            Log.d(TAG, "StorageHelper inicializado con bucket: " + STORAGE_BUCKET);
        } catch (Exception e) {
            Log.e(TAG, "Error inicializando StorageHelper", e);
            throw new RuntimeException("Error inicializando StorageHelper: " + e.getMessage());
        }
    }

    // Obtener instancia singleton
    public static synchronized StorageHelper getInstance() {
        if (instance == null) {
            instance = new StorageHelper();
        }
        return instance;
    }

    /**
     * Interface para callbacks de operaciones de Storage
     */
    public interface StorageCallback {
        void onSuccess(String downloadUrl, String storagePath);
        void onError(String error);
        void onProgress(int progress);
    }

    /**
     * Interface para callbacks de eliminación
     */
    public interface DeleteCallback {
        void onSuccess();
        void onError(String error);
    }

    /**
     * Subir imagen de pastel a Firebase Storage
     *
     * @param imageUri URI de la imagen seleccionada
     * @param cakeId ID del pastel ( se genera uno si es null)
     * @param callback Callback para manejar el resultado
     */
    public void uploadCakeImage(Uri imageUri, String cakeId, StorageCallback callback) {
        if (imageUri == null) {
            callback.onError("No se ha seleccionado ninguna imagen");
            return;
        }

        if (!isValidImageUri(imageUri)) {
            callback.onError("URI de imagen no valido");
            return;
        }

        try {
            // Generar nombre único para el archivo
            String fileName = generateImageFileName(cakeId);
            String imagePath = CAKES_FOLDER + "/" + fileName;

            // Crear referencia al archivo en Storage
            StorageReference imageRef = cakesImagesRef.child(fileName);

            Log.d(TAG, "Subiendo imagen a: " + imagePath);
            Log.d(TAG, "URI de imagen: " + imageUri.toString());

            // Subir archivo
            UploadTask uploadTask = imageRef.putFile(imageUri);

            // Escuchar progreso
            uploadTask.addOnProgressListener(taskSnapshot -> {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                int progressInt = (int) Math.round(progress);
                callback.onProgress(progressInt);
                Log.d(TAG, "Progreso de subida: " + progressInt + "%");
            });

            // Manejar resultado
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                Log.d(TAG, "Imagen subida exitosamente");

                // Obtener URL de descarga
                imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    String downloadUrl = downloadUri.toString();
                    Log.d(TAG, "URL de descarga obtenida: " + downloadUrl);
                    callback.onSuccess(downloadUrl, imagePath);
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Error obteniendo URL de descarga", e);
                    callback.onError("Error obteniendo URL de descarga: " + e.getMessage());
                });

            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error subiendo imagen", e);
                String errorMessage = "Error subiendo imagen";
                if (e.getMessage() != null) {
                    errorMessage += ": " + e.getMessage();
                }
                callback.onError(errorMessage);
            });

        } catch (Exception e) {
            Log.e(TAG, "Error preparando subida de imagen", e);
            callback.onError("Error preparando subida: " + e.getMessage());
        }
    }

    /**
     * Eliminar imagen de pastel de Firebase Storage
     *
     * @param imagePath Ruta de la imagen en Storage
     * @param callback Callback para manejar el resultado
     */
    public void deleteCakeImage(String imagePath, DeleteCallback callback) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            callback.onError("Ruta de imagen no válida");
            return;
        }

        try {
            // Limpiar la ruta para asegurar formato correcto
            String cleanPath = imagePath.startsWith(CAKES_FOLDER) ? imagePath : CAKES_FOLDER + "/" + imagePath;

            StorageReference imageRef = storageRef.child(cleanPath);

            Log.d(TAG, "Eliminando imagen: " + cleanPath);

            imageRef.delete().addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Imagen eliminada exitosamente: " + cleanPath);
                callback.onSuccess();
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error eliminando imagen: " + cleanPath, e);
                // Si el archivo no existe, consideramos que es éxito
                if (e.getMessage() != null && e.getMessage().contains("does not exist")) {
                    Log.w(TAG, "El archivo ya no existe, considerando como eliminado exitosamente");
                    callback.onSuccess();
                } else {
                    callback.onError("Error eliminando imagen: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error preparando eliminación de imagen", e);
            callback.onError("Error preparando eliminación: " + e.getMessage());
        }
    }

    /**
     * Actualizar imagen de pastel (elimina la anterior si existe y sube la nueva)
     *
     * @param newImageUri URI de la nueva imagen
     * @param cakeId ID del pastel
     * @param oldImagePath Ruta de la imagen anterior (puede ser null)
     * @param callback Callback para manejar el resultado
     */
    public void updateCakeImage(Uri newImageUri, String cakeId, String oldImagePath, StorageCallback callback) {
        if (newImageUri == null) {
            callback.onError("No se ha proporcionado una nueva imagen");
            return;
        }

        // Primero eliminar imagen anterior si existe
        if (oldImagePath != null && !oldImagePath.trim().isEmpty()) {
            Log.d(TAG, "Eliminando imagen anterior antes de subir nueva");
            deleteCakeImage(oldImagePath, new DeleteCallback() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Imagen anterior eliminada, subiendo nueva imagen");
                    uploadCakeImage(newImageUri, cakeId, callback);
                }

                @Override
                public void onError(String error) {
                    Log.w(TAG, "No se pudo eliminar imagen anterior: " + error);
                    // Continuar con la subida de la nueva imagen aunque falle la eliminación
                    uploadCakeImage(newImageUri, cakeId, callback);
                }
            });
        } else {
            // No hay imagen anterior, solo subir la nueva
            Log.d(TAG, "No hay imagen anterior, subiendo nueva imagen directamente");
            uploadCakeImage(newImageUri, cakeId, callback);
        }
    }

    /**
     * Generar nombre unico para archivo de imagen
     */
    private String generateImageFileName(String cakeId) {
        String prefix = (cakeId != null && !cakeId.isEmpty()) ? "cake_" + cakeId : "cake";
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        long timestamp = System.currentTimeMillis();
        return prefix + "_" + uniqueId + "_" + timestamp + ".jpg";
    }

    /**
     * Validar si el URI es una imagen válida
     */
    public boolean isValidImageUri(Uri imageUri) {
        if (imageUri == null) return false;

        String scheme = imageUri.getScheme();
        boolean isValid = "content".equals(scheme) || "file".equals(scheme);

        Log.d(TAG, "Validando URI: " + imageUri.toString() + " - Válido: " + isValid);
        return isValid;
    }

    /**
     * Obtener referencia a una imagen especifica
     */
    public StorageReference getImageReference(String imagePath) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return null;
        }

        String cleanPath = imagePath.startsWith(CAKES_FOLDER) ? imagePath : CAKES_FOLDER + "/" + imagePath;
        return storageRef.child(cleanPath);
    }

    /**
     * Obtener URL de descarga de una imagen
     */
    public void getImageDownloadUrl(String imagePath, StorageCallback callback) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            callback.onError("Ruta de imagen no válida");
            return;
        }

        try {
            StorageReference imageRef = getImageReference(imagePath);
            if (imageRef == null) {
                callback.onError("No se pudo crear referencia a la imagen");
                return;
            }

            Log.d(TAG, "Obteniendo URL de descarga para: " + imagePath);

            imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                String downloadUrl = downloadUri.toString();
                Log.d(TAG, "URL de descarga obtenida exitosamente");
                callback.onSuccess(downloadUrl, imagePath);
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error obteniendo URL de descarga para: " + imagePath, e);
                callback.onError("Error obteniendo URL: " + e.getMessage());
            });

        } catch (Exception e) {
            Log.e(TAG, "Error preparando obtención de URL", e);
            callback.onError("Error preparando obtención de URL: " + e.getMessage());
        }
    }

    /**
     * Verificar si el servicio de Storage está disponible
     */
    public boolean isStorageAvailable() {
        try {
            return storage != null && storageRef != null;
        } catch (Exception e) {
            Log.e(TAG, "Error verificando disponibilidad de Storage", e);
            return false;
        }
    }

    /**
     * Obtener información del bucket configurado
     */
    public String getStorageBucket() {
        return STORAGE_BUCKET;
    }


}