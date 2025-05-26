package com.example.pasteleriapdm.FragmentsAdmin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pasteleriapdm.Adapters.GestionarUsuariosAdapter;
import com.example.pasteleriapdm.Dialogs.UsuariosDialog;
import com.example.pasteleriapdm.Models.User;
import com.example.pasteleriapdm.R;
import com.example.pasteleriapdm.Utils.DatabaseHelper;

import java.util.List;

public class GestionUsuariosFragment extends Fragment implements UsuariosDialog.UsuarioDialogListener {
    private static final String TAG = "GestionUsuariosFragment";

    private Button btnAbrirDialogoUsuarios;
    private RecyclerView rvcUsuarios;
    private TextView lblerrorClientes;

    private GestionarUsuariosAdapter gestionarUsuariosAdapter;
    private DatabaseHelper databaseHelper;
    private boolean hayUsuarios = false; // Variable para controlar si existen usuarios

    public GestionUsuariosFragment() {
        // Required empty public constructor
    }

    public static GestionUsuariosFragment newInstance() {
        return new GestionUsuariosFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseHelper = DatabaseHelper.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gestion_usuarios, container, false);

        AsociarElementoXML(view);
        configurarRecyclerView();
        configurarEventos();
        verificarYCargarUsuarios();

        return view;
    }

    private void AsociarElementoXML(View view) {
        btnAbrirDialogoUsuarios = view.findViewById(R.id.btnAbrirDialogoUsuarios);
        rvcUsuarios = view.findViewById(R.id.rvcUsuarios);
        lblerrorClientes = view.findViewById(R.id.lblerrorClientes);
    }

    private void configurarRecyclerView() {
        gestionarUsuariosAdapter = new GestionarUsuariosAdapter(getContext(), getParentFragmentManager());
        rvcUsuarios.setLayoutManager(new LinearLayoutManager(getContext()));
        rvcUsuarios.setAdapter(gestionarUsuariosAdapter);
    }

    private void configurarEventos() {
        btnAbrirDialogoUsuarios.setOnClickListener(v -> {
            if (!hayUsuarios) {
                // Si no hay usuarios, crear el primer admin
                crearPrimerAdmin();
            } else if (puedeCrearUsuarios()) {
                // Usuario normal
                UsuariosDialog dialog = new UsuariosDialog();
                dialog.setUsuarioDialogListener(this);
                dialog.show(getParentFragmentManager(), "UsuariosDialog");
            } else {
                Toast.makeText(getContext(), "No tienes permisos para crear usuarios", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void crearPrimerAdmin() {
        Log.d(TAG, "Iniciando creación del primer administrador");
        UsuariosDialog dialog = new UsuariosDialog(true); // true indica que es primer admin
        dialog.setUsuarioDialogListener(this);
        dialog.show(getParentFragmentManager(), "PrimerAdminDialog");
    }

    private void verificarYCargarUsuarios() {
        Log.d(TAG, "Verificando existencia de usuarios...");
        mostrarCargando(true);

        databaseHelper.checkIfUsersExist(new DatabaseHelper.DatabaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean usersExist) {
                hayUsuarios = usersExist;
                Log.d(TAG, "Usuarios existen: " + hayUsuarios);

                if (hayUsuarios) {
                    // Si existen usuarios, cargarlos
                    cargarUsuarios();
                } else {
                    // Si no existen usuarios, mostrar mensaje para crear primer admin
                    mostrarCargando(false);
                    mostrarMensajePrimerAdmin();
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error verificando usuarios: " + error);
                hayUsuarios = false;
                mostrarCargando(false);
                mostrarMensajePrimerAdmin();
            }
        });
    }

    private void cargarUsuarios() {
        Log.d(TAG, "Cargando usuarios existentes...");

        databaseHelper.getAllUsers(new DatabaseHelper.DatabaseCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> usuarios) {
                Log.d(TAG, "Usuarios cargados exitosamente: " + usuarios.size());
                mostrarCargando(false);
                hayUsuarios = !usuarios.isEmpty();

                if (usuarios.isEmpty()) {
                    mostrarMensajePrimerAdmin();
                } else {
                    ocultarMensajeVacio();
                    gestionarUsuariosAdapter.actualizarLista(usuarios);
                    actualizarBotonSegunEstado();
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error cargando usuarios: " + error);
                mostrarCargando(false);
                hayUsuarios = false;

                // Si es error de permisos, probablemente no hay usuarios
                if (error.contains("Permission denied") || error.contains("permission-denied")) {
                    mostrarMensajePrimerAdmin();
                } else {
                    mostrarMensajeError("Error cargando usuarios: " + error);
                }
            }
        });
    }

    private void mostrarMensajePrimerAdmin() {
        btnAbrirDialogoUsuarios.setText("CREAR PRIMER ADMINISTRADOR");
        btnAbrirDialogoUsuarios.setEnabled(true);
        lblerrorClientes.setText("¡Bienvenido!\n\nNo hay usuarios registrados en el sistema.\nDebes crear el primer administrador para comenzar.");
        lblerrorClientes.setVisibility(View.VISIBLE);
        rvcUsuarios.setVisibility(View.GONE);
    }

    private void actualizarBotonSegunEstado() {
        if (hayUsuarios) {
            btnAbrirDialogoUsuarios.setText("AGREGAR USUARIOS");
        } else {
            btnAbrirDialogoUsuarios.setText("CREAR PRIMER ADMINISTRADOR");
        }
    }

    private boolean puedeCrearUsuarios() {
        // Verificar si el usuario actual es admin
        String currentUserId = databaseHelper.getCurrentUserId();
        if (currentUserId == null) {
            return false;
        }

        // Si hay usuarios, verificar permisos (implementación básica)
        // En una implementación completa, deberías verificar el rol del usuario actual
        return hayUsuarios;
    }

    private void mostrarCargando(boolean mostrar) {
        if (mostrar) {
            btnAbrirDialogoUsuarios.setEnabled(false);
            btnAbrirDialogoUsuarios.setText("Verificando...");
            lblerrorClientes.setText("Verificando usuarios...");
            lblerrorClientes.setVisibility(View.VISIBLE);
            rvcUsuarios.setVisibility(View.GONE);
        } else {
            btnAbrirDialogoUsuarios.setEnabled(true);
            actualizarBotonSegunEstado();
        }
    }

    private void mostrarMensajeVacio(String mensaje) {
        lblerrorClientes.setText(mensaje);
        lblerrorClientes.setVisibility(View.VISIBLE);
        rvcUsuarios.setVisibility(View.GONE);
    }

    private void mostrarMensajeError(String mensaje) {
        lblerrorClientes.setText(mensaje);
        lblerrorClientes.setVisibility(View.VISIBLE);
        rvcUsuarios.setVisibility(View.GONE);
    }

    private void ocultarMensajeVacio() {
        lblerrorClientes.setVisibility(View.GONE);
        rvcUsuarios.setVisibility(View.VISIBLE);
    }

    // Implementación de UsuarioDialogListener
    @Override
    public void onUsuarioCreado(User usuario) {
        Log.d(TAG, "Usuario creado: " + usuario.getName());

        // Actualizar estado
        hayUsuarios = true;

        // Agregar el usuario al adapter
        gestionarUsuariosAdapter.agregarUsuario(usuario);

        // Ocultar mensaje de vacío si estaba visible
        ocultarMensajeVacio();

        // Actualizar botón
        actualizarBotonSegunEstado();

        // Mostrar mensaje de éxito
        String mensaje = usuario.isAdmin() && gestionarUsuariosAdapter.getItemCount() == 1
                ? "¡Primer administrador creado exitosamente!\nYa puedes gestionar el sistema."
                : "Usuario " + usuario.getName() + " creado exitosamente";

        Toast.makeText(getContext(), mensaje, Toast.LENGTH_LONG).show();

        // Scroll al final para mostrar el nuevo usuario
        rvcUsuarios.smoothScrollToPosition(gestionarUsuariosAdapter.getItemCount() - 1);
    }

    @Override
    public void onUsuarioActualizado(User usuario) {
        Log.d(TAG, "Usuario actualizado: " + usuario.getName());

        // Actualizar el usuario en el adapter
        gestionarUsuariosAdapter.actualizarUsuario(usuario);

        // Mostrar mensaje de éxito
        Toast.makeText(getContext(), "Usuario " + usuario.getName() + " actualizado exitosamente", Toast.LENGTH_SHORT).show();
    }

    // Método público para recargar usuarios (útil para llamadas externas)
    public void recargarUsuarios() {
        verificarYCargarUsuarios();
    }

    // Método para manejar la eliminación de usuarios (llamado desde el adapter)
    public void onUsuarioEliminado(String uid) {
        Log.d(TAG, "Usuario eliminado: " + uid);
        gestionarUsuariosAdapter.eliminarUsuario(uid);

        // Si no quedan usuarios, actualizar estado
        if (gestionarUsuariosAdapter.getItemCount() == 0) {
            hayUsuarios = false;
            mostrarMensajePrimerAdmin();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recargar usuarios cuando el fragment vuelve a estar visible
        Log.d(TAG, "Fragment resumido, verificando usuarios...");
        verificarYCargarUsuarios();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Limpiar referencias para evitar memory leaks
        gestionarUsuariosAdapter = null;
    }
}