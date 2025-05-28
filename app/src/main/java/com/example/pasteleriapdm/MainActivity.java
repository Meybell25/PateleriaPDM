package com.example.pasteleriapdm;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pasteleriapdm.Models.User;
import com.example.pasteleriapdm.Utils.DatabaseHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    // SharedPreferences para controlar el auto-login (mismas constantes que LoginActivity)
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_AUTO_LOGIN_ENABLED = "auto_login_enabled";
    private static final String KEY_LAST_LOGIN_TIME = "last_login_time";
    private static final String KEY_SESSION_VALID = "session_valid";
    private static final String KEY_LOGGED_USER_UID = "logged_user_uid";
    private static final String KEY_LOGGED_USER_EMAIL = "logged_user_email";

    // Tiempo máximo de sesión (24 horas en milisegundos)
    private static final long MAX_SESSION_TIME = 24 * 60 * 60 * 1000;

    private ProgressBar progressBar;
    private TextView tvLoading;
    private Handler handler;

    private FirebaseAuth firebaseAuth;
    private DatabaseHelper databaseHelper;
    private SharedPreferences preferences;

    // Textos de carga que van cambiando
    private String[] loadingTexts = {
            "Preparando deliciosos sabores...",
            "Horneando los mejores pasteles...",
            "Decorando con amor...",
            "Agregando el toque final...",
            "¡Listo para endulzar tu día!"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "MainActivity iniciada");

        // Inicializar Firebase y preferencias
        firebaseAuth = FirebaseAuth.getInstance();
        databaseHelper = DatabaseHelper.getInstance();
        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Inicializar vistas
        progressBar = findViewById(R.id.progress_bar);
        tvLoading = findViewById(R.id.tv_loading);
        handler = new Handler(Looper.getMainLooper());

        // Iniciar animacion del splash
        startSplashAnimation();
    }

    private void startSplashAnimation() {
        // Animacion suave del ProgressBar
        ObjectAnimator progressAnimator = ObjectAnimator.ofInt(progressBar, "progress", 0, 100);
        progressAnimator.setDuration(3000); // 3 segundos
        progressAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        progressAnimator.start();

        // Cambiar textos durante la carga
        changeLoadingTexts();

        // Navegar a la siguiente pantalla despues de 3.5 segundos
        handler.postDelayed(() -> {
            verificarSesionYNavegar();
        }, 3500);
    }

    private void changeLoadingTexts() {
        // Cambiar texto cada 700ms
        for (int i = 0; i < loadingTexts.length; i++) {
            final int index = i;
            handler.postDelayed(() -> {
                tvLoading.setText(loadingTexts[index]);

                // Pequeña animacion de fade para el texto
                tvLoading.setAlpha(0.5f);
                tvLoading.animate()
                        .alpha(1.0f)
                        .setDuration(300)
                        .start();

            }, i * 700);
        }
    }

    /**
     * Verificar sesión existente y navegar apropiadamente
     */
    private void verificarSesionYNavegar() {
        Log.d(TAG, "Verificando sesión existente...");

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            Log.d(TAG, "Usuario de Firebase encontrado: " + currentUser.getEmail());

            // Verificar si es el usuario correcto que debería estar logueado
            String expectedUID = preferences.getString(KEY_LOGGED_USER_UID, "");
            String expectedEmail = preferences.getString(KEY_LOGGED_USER_EMAIL, "");

            Log.d(TAG, "Usuario actual: " + currentUser.getUid() + " (" + currentUser.getEmail() + ")");
            Log.d(TAG, "Usuario esperado: " + expectedUID + " (" + expectedEmail + ")");

            // Verificar si es el usuario correcto
            if (!currentUser.getUid().equals(expectedUID)) {
                Log.d(TAG, "Usuario actual no coincide con el esperado, yendo a login");
                cerrarSesionYIrALogin();
                return;
            }

            // Verificar si la sesión es válida y no ha expirado
            if (isSesionValida()) {
                Log.d(TAG, "Sesión válida encontrada, verificando en BD...");
                verificarUsuarioEnBDYRedireccionar(currentUser.getUid());
            } else {
                Log.d(TAG, "Sesión expirada o inválida, yendo a login");
                cerrarSesionYIrALogin();
            }
        } else {
            Log.d(TAG, "No hay usuario de Firebase, yendo a login");
            // No hay usuario, limpiar cualquier preferencia residual
            limpiarPreferencias();
            irALogin();
        }
    }

    /**
     * Verificar si la sesión actual es válida
     */
    private boolean isSesionValida() {
        boolean autoLoginEnabled = preferences.getBoolean(KEY_AUTO_LOGIN_ENABLED, false);
        boolean sessionValid = preferences.getBoolean(KEY_SESSION_VALID, false);
        long lastLoginTime = preferences.getLong(KEY_LAST_LOGIN_TIME, 0);
        long currentTime = System.currentTimeMillis();

        Log.d(TAG, "Verificando sesión: autoLogin=" + autoLoginEnabled +
                ", sessionValid=" + sessionValid +
                ", tiempoTranscurrido=" + (currentTime - lastLoginTime) + "ms");

        return autoLoginEnabled && sessionValid && (currentTime - lastLoginTime) < MAX_SESSION_TIME;
    }

    /**
     * Verificar usuario en base de datos y redireccionar según rol
     */
    private void verificarUsuarioEnBDYRedireccionar(String uid) {
        databaseHelper.getUser(uid, new DatabaseHelper.DatabaseCallback<User>() {
            @Override
            public void onSuccess(User user) {
                Log.d(TAG, "Usuario encontrado en BD: " + user.getName());
                Log.d(TAG, "Rol del usuario: '" + user.getRole() + "'");

                // Actualizar último login
                user.updateLastLogin();
                databaseHelper.updateUser(user, new DatabaseHelper.DatabaseCallback<User>() {
                    @Override
                    public void onSuccess(User updatedUser) {
                        Log.d(TAG, "Último login actualizado");
                        redirigirSegunRol(updatedUser);
                    }

                    @Override
                    public void onError(String error) {
                        Log.w(TAG, "Error actualizando último login: " + error);
                        redirigirSegunRol(user);
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Usuario no encontrado en BD: " + error);
                cerrarSesionYIrALogin();
            }
        });
    }

    /**
     * Redireccionar según el rol del usuario
     */
    private void redirigirSegunRol(User user) {
        Log.d(TAG, "Redirigiendo según rol...");
        Log.d(TAG, "Rol del usuario: '" + user.getRole() + "'");
        Log.d(TAG, "ROLE_ADMIN: '" + User.ROLE_ADMIN + "'");
        Log.d(TAG, "ROLE_SELLER: '" + User.ROLE_SELLER + "'");
        Log.d(TAG, "ROLE_PRODUCTION: '" + User.ROLE_PRODUCTION + "'");

        if (!user.isActive()) {
            Log.d(TAG, "Usuario inactivo o bloqueado");
            cerrarSesionYIrALogin();
            return;
        }

        Intent intent = null;
        String userRole = user.getRole() != null ? user.getRole().trim() : "";

        Log.d(TAG, "Rol limpio: '" + userRole + "'");

        if (User.ROLE_ADMIN.equals(userRole)) {
            Log.d(TAG, "Redirigiendo a ADMIN");
            intent = new Intent(this, MunuAdminActivity.class);
        } else if (User.ROLE_SELLER.equals(userRole)) {
            Log.d(TAG, "Redirigiendo a SELLER");
            intent = new Intent(this, MunuSellerActivity.class);
        } else if (User.ROLE_PRODUCTION.equals(userRole)) {
            Log.d(TAG, "Redirigiendo a PRODUCTION");
            intent = new Intent(this, MunuProductionActivity.class);
        } else {
            Log.e(TAG, "Rol no reconocido: '" + userRole + "'");
            Log.e(TAG, "Longitud del rol: " + userRole.length());
            Log.e(TAG, "Bytes del rol: " + java.util.Arrays.toString(userRole.getBytes()));
            cerrarSesionYIrALogin();
            return;
        }

        if (intent == null) {
            Log.e(TAG, "Intent es null, yendo a login");
            cerrarSesionYIrALogin();
            return;
        }

        Log.d(TAG, "Navegando a: " + intent.getComponent().getClassName());
        startActivity(intent);
        finish();
    }

    /**
     * Cerrar sesión completa y ir a login
     */
    private void cerrarSesionYIrALogin() {
        Log.d(TAG, "Cerrando sesión completa");

        // Cerrar sesión de Firebase
        firebaseAuth.signOut();

        // Limpiar preferencias
        limpiarPreferencias();

        // Ir a login
        irALogin();
    }

    /**
     * Limpiar preferencias de sesión
     */
    private void limpiarPreferencias() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_AUTO_LOGIN_ENABLED, false);
        editor.putBoolean(KEY_SESSION_VALID, false);
        editor.putLong(KEY_LAST_LOGIN_TIME, 0);
        editor.remove(KEY_LOGGED_USER_UID);
        editor.remove(KEY_LOGGED_USER_EMAIL);
        editor.apply();

        Log.d(TAG, "Preferencias de sesión limpiadas");
    }

    /**
     * Navegar al LoginActivity
     */
    private void irALogin() {
        Log.d(TAG, "Navegando a LoginActivity");
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        // Agregar flag para indicar que viene del splash
        intent.putExtra("FROM_SPLASH", true);
        startActivity(intent);

        // Animación de transicion suave
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Limpiar handlers para evitar memory leaks
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        Log.d(TAG, "MainActivity destruida");
    }
}