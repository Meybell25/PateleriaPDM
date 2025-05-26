package com.example.pasteleriapdm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pasteleriapdm.Models.User;
import com.example.pasteleriapdm.Utils.DatabaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    // SharedPreferences para controlar el auto-login
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_AUTO_LOGIN_ENABLED = "auto_login_enabled";
    private static final String KEY_LAST_LOGIN_TIME = "last_login_time";
    private static final String KEY_SESSION_VALID = "session_valid";

    // Tiempo máximo de sesión (24 horas en milisegundos)
    private static final long MAX_SESSION_TIME = 24 * 60 * 60 * 1000;

    private TextInputEditText txtEmail, txtPassword;
    private MaterialButton btnIniciarSesion;

    private FirebaseAuth firebaseAuth;
    private DatabaseHelper databaseHelper;
    private SharedPreferences preferences;

    // Flag para controlar si venimos de un login manual
    private boolean isManualLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar Firebase y preferencias
        firebaseAuth = FirebaseAuth.getInstance();
        databaseHelper = DatabaseHelper.getInstance();
        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Asociar elementos XML
        AsociarElementoXML();

        // Configurar eventos
        configurarEventos();

        // Solo verificar usuario logueado si la sesión es válida
        verificarSesionExistente();
    }

    private void AsociarElementoXML() {
        txtEmail = findViewById(R.id.txtCorreo);
        txtPassword = findViewById(R.id.txtPassword);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
    }

    private void configurarEventos() {
        btnIniciarSesion.setOnClickListener(v -> {
            isManualLogin = true; // Marcar como login manual
            iniciarSesion();
        });
    }

    /**
     * Verificar si existe una sesión válida y no expirada
     */
    private void verificarSesionExistente() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            Log.d(TAG, "Usuario de Firebase encontrado: " + currentUser.getEmail());

            // Verificar si la sesión es válida y no ha expirado
            if (isSesionValida()) {
                Log.d(TAG, "Sesión válida encontrada, verificando en BD...");
                verificarUsuarioEnBD(currentUser.getUid());
            } else {
                Log.d(TAG, "Sesión expirada o inválida, cerrando sesión de Firebase");
                cerrarSesionCompleta();
            }
        } else {
            Log.d(TAG, "No hay usuario de Firebase, mostrando pantalla de login");
            // No hay usuario, limpiar cualquier preferencia residual
            limpiarPreferencias();
            mostrarPantallaLogin();
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

        // La sesión es válida si:
        // 1. El auto-login está habilitado
        // 2. La sesión está marcada como válida
        // 3. No ha pasado el tiempo máximo de sesión
        return autoLoginEnabled && sessionValid && (currentTime - lastLoginTime) < MAX_SESSION_TIME;
    }

    private void iniciarSesion() {
        if (!validarCampos()) return;

        String email = txtEmail.getText().toString().trim();
        String password = txtPassword.getText().toString().trim();

        // Mostrar progreso
        mostrarCargando(true);

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        Log.d(TAG, "Login exitoso para: " + user.getEmail());

                        // Marcar como login manual exitoso
                        guardarSesionValida();

                        verificarUsuarioEnBD(user.getUid());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error en login", e);
                    mostrarCargando(false);
                    isManualLogin = false; // Reset flag
                    mostrarError("Error: " + e.getMessage());
                });
    }

    private void verificarUsuarioEnBD(String uid) {
        databaseHelper.getUser(uid, new DatabaseHelper.DatabaseCallback<User>() {
            @Override
            public void onSuccess(User user) {
                Log.d(TAG, "Usuario encontrado en BD: " + user.getName());

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
                        // Continuar aunque no se actualice el último login
                        redirigirSegunRol(user);
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Usuario no encontrado en BD: " + error);
                mostrarCargando(false);
                mostrarError("Usuario no autorizado en el sistema");
                cerrarSesionCompleta();
            }
        });
    }

    private void redirigirSegunRol(User user) {
        mostrarCargando(false);

        if (!user.isActive()) {
            mostrarError("Su cuenta está " +
                    (user.isBlocked() ? "bloqueada" : "inactiva") +
                    ". Contacte al administrador.");
            cerrarSesionCompleta();
            return;
        }
        Intent intent;
        String mensaje = "Bienvenido, " + user.getName();
        String mensaje2 = "Bienvenido de nuevo, " + user.getName();

        switch (user.getRole()) {
            case User.ROLE_ADMIN:
                intent = new Intent(this, MunuAdminActivity.class);
                mensaje += " (Administrador)";
                break;
            case User.ROLE_SELLER:
                intent = new Intent(this, MunuAdminActivity.class);
                mensaje += " (Vendedor)";
                break;
            case User.ROLE_PRODUCTION:
                intent = new Intent(this, MunuAdminActivity.class);
                mensaje += " (Producción)";
                break;
            default:
                mostrarError("Rol de usuario no válido");
                cerrarSesionCompleta();
                return;
        }

        //  mostrar mensaje de bienvenida si es login manual y mostrar mensaje2 si la sesion se mantiene activa
        if (isManualLogin) {
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this, mensaje2, Toast.LENGTH_SHORT).show();
        }

        startActivity(intent);
        finish();
    }

    /**
     * Guardar sesión como válida después de login exitoso
     */
    private void guardarSesionValida() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_AUTO_LOGIN_ENABLED, true);
        editor.putBoolean(KEY_SESSION_VALID, true);
        editor.putLong(KEY_LAST_LOGIN_TIME, System.currentTimeMillis());
        editor.apply();

        Log.d(TAG, "Sesión guardada como válida");
    }

    /**
     * Cerrar sesión completamente y limpiar todo
     */
    private void cerrarSesionCompleta() {
        Log.d(TAG, "Cerrando sesión completa");

        // Cerrar sesión de Firebase
        firebaseAuth.signOut();

        // Limpiar preferencias
        limpiarPreferencias();

        // Reset flags
        isManualLogin = false;

        // Mostrar pantalla de login
        mostrarPantallaLogin();
    }

    /**
     * Limpiar preferencias de sesión
     */
    private void limpiarPreferencias() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_AUTO_LOGIN_ENABLED, false);
        editor.putBoolean(KEY_SESSION_VALID, false);
        editor.putLong(KEY_LAST_LOGIN_TIME, 0);
        editor.apply();

        Log.d(TAG, "Preferencias de sesión limpiadas");
    }

    /**
     * Mostrar la pantalla de login (campos habilitados)
     */
    private void mostrarPantallaLogin() {
        mostrarCargando(false);
        // Limpiar campos
        txtEmail.setText("");
        txtPassword.setText("");
    }

    private boolean validarCampos() {
        String email = txtEmail.getText().toString().trim();
        String password = txtPassword.getText().toString().trim();

        if (email.isEmpty()) {
            txtEmail.setError("El email es obligatorio");
            txtEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            txtEmail.setError("Email inválido");
            txtEmail.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            txtPassword.setError("La contraseña es obligatoria");
            txtPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            txtPassword.setError("La contraseña debe tener al menos 6 caracteres");
            txtPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void mostrarCargando(boolean mostrar) {
        btnIniciarSesion.setEnabled(!mostrar);
        btnIniciarSesion.setText(mostrar ? "Iniciando sesión..." : "Iniciar Sesión");
        txtEmail.setEnabled(!mostrar);
        txtPassword.setEnabled(!mostrar);
    }

    private void mostrarError(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // NO hacer verificación automática en onStart()
        // Solo log para debug
        Log.d(TAG, "onStart() - No realizando verificación automática");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "LoginActivity destruida");
    }

    /**
     * Metodo público para cerrar sesión desde otras actividades
     */
    public static void cerrarSesionYRedireccionar(AppCompatActivity fromActivity) {
        // Limpiar preferencias
        SharedPreferences prefs = fromActivity.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_AUTO_LOGIN_ENABLED, false);
        editor.putBoolean(KEY_SESSION_VALID, false);
        editor.putLong(KEY_LAST_LOGIN_TIME, 0);
        editor.apply();

        // Cerrar sesión de Firebase
        FirebaseAuth.getInstance().signOut();

        // Redireccionar al login
        Intent intent = new Intent(fromActivity, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        fromActivity.startActivity(intent);
        fromActivity.finish();
    }
}