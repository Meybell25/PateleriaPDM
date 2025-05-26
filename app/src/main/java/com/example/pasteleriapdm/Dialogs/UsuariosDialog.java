package com.example.pasteleriapdm.Dialogs;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.pasteleriapdm.Models.User;
import com.example.pasteleriapdm.R;
import com.example.pasteleriapdm.Utils.DatabaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class UsuariosDialog extends DialogFragment {
    private static final String TAG = "UsuariosDialog";

    private TextInputEditText txtNombreUsuario, txtEmailUsuario, txtPasswordUsuario;
    private Spinner spinnerRolUsuario, spinnerEstadoUsuario;
    private MaterialButton btnInsertarUsuario;
    private TextView btnSalir, lblTituloDialogoUsuario;

    private DatabaseHelper databaseHelper;
    private FirebaseAuth firebaseAuth;

    // Variables para modo edición
    private User usuarioParaEditar;
    private boolean modoEdicion = false;
    private boolean esPrimerAdmin = false;

    // Interface para notificar cambios
    public interface UsuarioDialogListener {
        void onUsuarioCreado(User usuario);
        void onUsuarioActualizado(User usuario);
    }

    private UsuarioDialogListener listener;

    // Constructor para crear usuario
    public UsuariosDialog() {
        this.modoEdicion = false;
    }

    // Constructor para crear primer admin
    public UsuariosDialog(boolean esPrimerAdmin) {
        this.modoEdicion = false;
        this.esPrimerAdmin = esPrimerAdmin;
    }

    // Constructor para editar usuario
    public UsuariosDialog(User usuario) {
        this.usuarioParaEditar = usuario;
        this.modoEdicion = true;
    }

    public void setUsuarioDialogListener(UsuarioDialogListener listener) {
        this.listener = listener;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_usuarios, container, false);

        // Inicializar Firebase
        databaseHelper = DatabaseHelper.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        AsociarElementoXML(view);
        configurarSpinners();
        configurarEventos();

        // Configurar para primer admin si es necesario
        if (esPrimerAdmin) {
            configurarParaPrimerAdmin();
        }

        // Si es modo edición, llenar los campos
        if (modoEdicion && usuarioParaEditar != null) {
            llenarCamposParaEdicion();
        }

        return view;
    }

    private void configurarParaPrimerAdmin() {
        lblTituloDialogoUsuario.setText("CREAR PRIMER ADMINISTRADOR");
        btnInsertarUsuario.setText("Crear Administrador");

        // Forzar rol de admin y estado activo
        spinnerRolUsuario.setSelection(0); // admin
        spinnerRolUsuario.setEnabled(false); // No permitir cambiar

        spinnerEstadoUsuario.setSelection(0); // active
        spinnerEstadoUsuario.setEnabled(false); // No permitir cambiar

        // Establecer contraseña por defecto para primer admin (pero el usuario puede cambiarla)
        txtPasswordUsuario.setText("admin123");
        txtPasswordUsuario.setHint("Contraseña del administrador (mínimo 6 caracteres)");

        Toast.makeText(getContext(), "Creando el primer administrador del sistema", Toast.LENGTH_LONG).show();
    }

    private void configurarSpinners() {
        // Spinner de estados
        String[] estados = {"Activo", "Inactivo", "Bloqueado"};
        ArrayAdapter<String> adapterEstado = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, estados);
        adapterEstado.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstadoUsuario.setAdapter(adapterEstado);
        spinnerEstadoUsuario.setSelection(0); // Selecciona "Activo" por defecto

        // Spinner de roles
        String[] roles = {"admin", "seller", "production"};
        ArrayAdapter<String> adapterRol = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, roles);
        adapterRol.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRolUsuario.setAdapter(adapterRol);
        spinnerRolUsuario.setSelection(0); // Selecciona "admin" por defecto
    }

    private void configurarEventos() {
        btnSalir.setOnClickListener(v -> dismiss());
        btnInsertarUsuario.setOnClickListener(v -> {
            if (modoEdicion) {
                actualizarUsuario();
            } else {
                crearUsuario();
            }
        });
    }

    private void llenarCamposParaEdicion() {
        if (usuarioParaEditar == null) return;

        // Cambiar título
        lblTituloDialogoUsuario.setText("EDITAR USUARIO");
        btnInsertarUsuario.setText("Actualizar Usuario");
        btnInsertarUsuario.setIcon(getResources().getDrawable(R.drawable.ic_edit, null));

        // Llenar campos
        txtNombreUsuario.setText(usuarioParaEditar.getName());
        txtEmailUsuario.setText(usuarioParaEditar.getEmail());
        txtEmailUsuario.setEnabled(false); // No permitir cambiar email

        // *** MOSTRAR LA CONTRASEÑA ACTUAL EN MODO EDICIÓN ***
        if (usuarioParaEditar.getPassword() != null && !usuarioParaEditar.getPassword().isEmpty()) {
            txtPasswordUsuario.setText(usuarioParaEditar.getPassword());
            txtPasswordUsuario.setHint("Contraseña actual (editable)");
        } else {
            txtPasswordUsuario.setText("");
            txtPasswordUsuario.setHint("Nueva contraseña (opcional - mínimo 6 caracteres)");
        }

        // Seleccionar rol
        String[] roles = {"admin", "seller", "production"};
        for (int i = 0; i < roles.length; i++) {
            if (roles[i].equals(usuarioParaEditar.getRole())) {
                spinnerRolUsuario.setSelection(i);
                break;
            }
        }

        // Seleccionar estado
        String[] estados = {"active", "inactive", "blocked"};
        for (int i = 0; i < estados.length; i++) {
            if (estados[i].equals(usuarioParaEditar.getStatus())) {
                spinnerEstadoUsuario.setSelection(i);
                break;
            }
        }
    }

    private void crearUsuario() {
        if (!validarCampos()) return;

        mostrarCargando(true);

        String nombre = txtNombreUsuario.getText().toString().trim();
        String email = txtEmailUsuario.getText().toString().trim();
        String password = txtPasswordUsuario.getText().toString().trim();
        String rol = obtenerRolSeleccionado();
        String estado = obtenerEstadoSeleccionado();

        Log.d(TAG, "Iniciando creación de usuario:");
        Log.d(TAG, "- Nombre: " + nombre);
        Log.d(TAG, "- Email: " + email);
        Log.d(TAG, "- Rol: " + rol);
        Log.d(TAG, "- Estado: " + estado);
        Log.d(TAG, "- Es primer admin: " + esPrimerAdmin);

        // Crear usuario en Firebase Auth primero
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    Log.d(TAG, "Usuario creado en Auth con UID: " + uid);

                    // Crear usuario en la base de datos
                    User nuevoUsuario = new User(uid, nombre, email, rol, password);
                    nuevoUsuario.setStatus(estado);
                    nuevoUsuario.setCreatedAt(System.currentTimeMillis());
                    nuevoUsuario.setLastLogin(0);

                    Log.d(TAG, "Usuario a crear en BD: " + nuevoUsuario.toString());

                    // Usar el método apropiado según si es primer admin o no
                    if (esPrimerAdmin) {
                        Log.d(TAG, "Creando primer administrador con UID: " + uid);
                        databaseHelper.createFirstAdmin(nuevoUsuario, new DatabaseHelper.DatabaseCallback<User>() {
                            @Override
                            public void onSuccess(User usuario) {
                                Log.d(TAG, "Primer administrador creado exitosamente");
                                mostrarCargando(false);
                                Toast.makeText(getContext(), "Primer administrador creado exitosamente", Toast.LENGTH_SHORT).show();

                                if (listener != null) {
                                    listener.onUsuarioCreado(usuario);
                                }
                                dismiss();
                            }

                            @Override
                            public void onError(String error) {
                                Log.e(TAG, "Error creando primer administrador en BD: " + error);
                                mostrarCargando(false);

                                // Mostrar error específico
                                String mensajeError = "Error creando primer administrador: " + error;
                                Toast.makeText(getContext(), mensajeError, Toast.LENGTH_LONG).show();

                                // Eliminar usuario de Auth si falló en BD
                                if (authResult.getUser() != null) {
                                    authResult.getUser().delete()
                                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Usuario eliminado de Auth después del error"))
                                            .addOnFailureListener(e -> Log.e(TAG, "Error eliminando usuario de Auth", e));
                                }
                            }
                        });
                    } else {
                        // Usuario normal
                        databaseHelper.createUser(nuevoUsuario, new DatabaseHelper.DatabaseCallback<User>() {
                            @Override
                            public void onSuccess(User usuario) {
                                Log.d(TAG, "Usuario creado exitosamente");
                                mostrarCargando(false);
                                Toast.makeText(getContext(), "Usuario creado exitosamente", Toast.LENGTH_SHORT).show();

                                if (listener != null) {
                                    listener.onUsuarioCreado(usuario);
                                }
                                dismiss();
                            }

                            @Override
                            public void onError(String error) {
                                Log.e(TAG, "Error creando usuario en BD: " + error);
                                mostrarCargando(false);
                                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_LONG).show();

                                // Eliminar usuario de Auth si falló en BD
                                if (authResult.getUser() != null) {
                                    authResult.getUser().delete()
                                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Usuario eliminado de Auth después del error"))
                                            .addOnFailureListener(e -> Log.e(TAG, "Error eliminando usuario de Auth", e));
                                }
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creando usuario en Auth", e);
                    mostrarCargando(false);

                    String mensajeError;
                    if (e.getMessage().contains("email-already-in-use")) {
                        mensajeError = "Este email ya está registrado";
                    } else if (e.getMessage().contains("weak-password")) {
                        mensajeError = "La contraseña es muy débil";
                    } else if (e.getMessage().contains("invalid-email")) {
                        mensajeError = "Email inválido";
                    } else {
                        mensajeError = "Error creando usuario: " + e.getMessage();
                    }

                    Toast.makeText(getContext(), mensajeError, Toast.LENGTH_LONG).show();
                });
    }

    private void actualizarUsuario() {
        if (!validarCamposEdicion()) return;

        mostrarCargando(true);

        String nombre = txtNombreUsuario.getText().toString().trim();
        String password = txtPasswordUsuario.getText().toString().trim();
        String rol = obtenerRolSeleccionado();
        String estado = obtenerEstadoSeleccionado();

        Log.d(TAG, "Actualizando usuario: " + usuarioParaEditar.getUid());
        Log.d(TAG, "Nuevo nombre: " + nombre);
        Log.d(TAG, "Nueva contraseña: " + (password.isEmpty() ? "Sin cambios" : "Se actualiza"));
        Log.d(TAG, "Nuevo rol: " + rol);
        Log.d(TAG, "Nuevo estado: " + estado);

        // Actualizar datos del usuario
        usuarioParaEditar.setName(nombre);
        usuarioParaEditar.setRole(rol);
        usuarioParaEditar.setStatus(estado);

        // Solo actualizar contraseña si se cambió
        if (!password.isEmpty()) {
            usuarioParaEditar.setPassword(password);
            Log.d(TAG, "Contraseña será actualizada");
        } else {
            Log.d(TAG, "Contraseña no se actualiza (campo vacío)");
        }

        databaseHelper.updateUser(usuarioParaEditar, new DatabaseHelper.DatabaseCallback<User>() {
            @Override
            public void onSuccess(User usuario) {
                Log.d(TAG, "Usuario actualizado exitosamente en Firebase");
                mostrarCargando(false);
                Toast.makeText(getContext(), "Usuario actualizado exitosamente", Toast.LENGTH_SHORT).show();

                if (listener != null) {
                    listener.onUsuarioActualizado(usuario);
                }
                dismiss();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error actualizando usuario en Firebase: " + error);
                mostrarCargando(false);
                Toast.makeText(getContext(), "Error actualizando usuario: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validarCampos() {
        String nombre = txtNombreUsuario.getText().toString().trim();
        String email = txtEmailUsuario.getText().toString().trim();
        String password = txtPasswordUsuario.getText().toString().trim();

        if (nombre.isEmpty()) {
            txtNombreUsuario.setError("El nombre es obligatorio");
            txtNombreUsuario.requestFocus();
            return false;
        }

        if (nombre.length() < 3) {
            txtNombreUsuario.setError("El nombre debe tener al menos 3 caracteres");
            txtNombreUsuario.requestFocus();
            return false;
        }

        if (email.isEmpty()) {
            txtEmailUsuario.setError("El email es obligatorio");
            txtEmailUsuario.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            txtEmailUsuario.setError("Email inválido");
            txtEmailUsuario.requestFocus();
            return false;
        }

        // *** VALIDACIÓN OBLIGATORIA DE CONTRASEÑA PARA CREACIÓN ***
        if (password.isEmpty()) {
            txtPasswordUsuario.setError("La contraseña es obligatoria");
            txtPasswordUsuario.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            txtPasswordUsuario.setError("La contraseña debe tener al menos 6 caracteres");
            txtPasswordUsuario.requestFocus();
            return false;
        }

        // Validación adicional de contraseña fuerte (opcional)
        if (!esContrasenaSegura(password)) {
            txtPasswordUsuario.setError("La contraseña debe contener al menos una letra y un número");
            txtPasswordUsuario.requestFocus();
            return false;
        }

        return true;
    }

    private boolean validarCamposEdicion() {
        String nombre = txtNombreUsuario.getText().toString().trim();
        String password = txtPasswordUsuario.getText().toString().trim();

        if (nombre.isEmpty()) {
            txtNombreUsuario.setError("El nombre es obligatorio");
            txtNombreUsuario.requestFocus();
            return false;
        }

        if (nombre.length() < 3) {
            txtNombreUsuario.setError("El nombre debe tener al menos 3 caracteres");
            txtNombreUsuario.requestFocus();
            return false;
        }

        // En edición, la contraseña es opcional, pero si se ingresa debe ser válida
        if (!password.isEmpty()) {
            if (password.length() < 6) {
                txtPasswordUsuario.setError("La contraseña debe tener al menos 6 caracteres");
                txtPasswordUsuario.requestFocus();
                return false;
            }

            if (!esContrasenaSegura(password)) {
                txtPasswordUsuario.setError("La contraseña debe contener al menos una letra y un número");
                txtPasswordUsuario.requestFocus();
                return false;
            }
        }

        return true;
    }

    /**
     * Valida que la contraseña tenga al menos una letra y un número
     */
    private boolean esContrasenaSegura(String password) {
        boolean tieneLetra = false;
        boolean tieneNumero = false;

        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                tieneLetra = true;
            }
            if (Character.isDigit(c)) {
                tieneNumero = true;
            }
            if (tieneLetra && tieneNumero) {
                break;
            }
        }

        return tieneLetra && tieneNumero;
    }

    private String obtenerRolSeleccionado() {
        String[] roles = {"admin", "seller", "production"};
        return roles[spinnerRolUsuario.getSelectedItemPosition()];
    }

    private String obtenerEstadoSeleccionado() {
        String[] estados = {"active", "inactive", "blocked"};
        return estados[spinnerEstadoUsuario.getSelectedItemPosition()];
    }

    private void mostrarCargando(boolean mostrar) {
        btnInsertarUsuario.setEnabled(!mostrar);
        btnSalir.setEnabled(!mostrar);
        txtNombreUsuario.setEnabled(!mostrar);
        txtPasswordUsuario.setEnabled(!mostrar);
        if (!modoEdicion) {
            txtEmailUsuario.setEnabled(!mostrar);
        }
        spinnerRolUsuario.setEnabled(!mostrar && !esPrimerAdmin);
        spinnerEstadoUsuario.setEnabled(!mostrar && !esPrimerAdmin);

        if (mostrar) {
            btnInsertarUsuario.setText(modoEdicion ? "Actualizando..." : (esPrimerAdmin ? "Creando Admin..." : "Creando..."));
        } else {
            btnInsertarUsuario.setText(modoEdicion ? "Actualizar Usuario" : (esPrimerAdmin ? "Crear Administrador" : "Registrar Usuario"));
        }
    }

    private void AsociarElementoXML(View view) {
        // TextViews
        lblTituloDialogoUsuario = view.findViewById(R.id.lblTituloDialogoUsuario);
        btnSalir = view.findViewById(R.id.btnSalir);

        // TextInputEditTexts
        txtNombreUsuario = view.findViewById(R.id.txtNombreUsuario);
        txtEmailUsuario = view.findViewById(R.id.txtEmailUsuario);
        txtPasswordUsuario = view.findViewById(R.id.txtPasswordUsuario);

        // Spinners
        spinnerRolUsuario = view.findViewById(R.id.spinnerRolUsuario);
        spinnerEstadoUsuario = view.findViewById(R.id.spinnerEstadoUsuario);

        // Botón
        btnInsertarUsuario = view.findViewById(R.id.btnInsertarUsuario);
    }
}