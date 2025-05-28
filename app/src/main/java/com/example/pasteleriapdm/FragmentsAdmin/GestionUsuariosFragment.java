package com.example.pasteleriapdm.FragmentsAdmin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
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

import java.util.ArrayList;
import java.util.List;

public class GestionUsuariosFragment extends Fragment implements UsuariosDialog.UsuarioDialogListener {
    private static final String TAG = "GestionUsuariosFragment";

    private Button btnAbrirDialogoUsuarios;
    private RecyclerView rvcUsuarios;
    private TextView lblerrorClientes;
    private TextView lblContadorUsuarios;
    private Spinner spinnerFiltroRol;
    private ImageButton btnLimpiarFiltro;
    private LinearLayout layoutNoUsuarios;
    private TextView txtMensajeNoUsuarios;

    private GestionarUsuariosAdapter gestionarUsuariosAdapter;
    private DatabaseHelper databaseHelper;
    private boolean hayUsuarios = false;

    // Listas para el filtrado
    private List<User> listaUsuariosCompleta = new ArrayList<>();
    private List<User> listaUsuariosFiltrada = new ArrayList<>();

    // Variables para el filtro
    private String filtroRolActual = "TODOS";
    private ArrayAdapter<String> spinnerAdapter;

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
        configurarSpinnerFiltro();
        configurarRecyclerView();
        configurarEventos();
        verificarYCargarUsuarios();

        return view;
    }

    private void AsociarElementoXML(View view) {
        btnAbrirDialogoUsuarios = view.findViewById(R.id.btnAbrirDialogoUsuarios);
        rvcUsuarios = view.findViewById(R.id.rvcUsuarios);
        lblerrorClientes = view.findViewById(R.id.lblerrorClientes);
        lblContadorUsuarios = view.findViewById(R.id.lblContadorUsuarios);
        spinnerFiltroRol = view.findViewById(R.id.spinnerFiltroRol);
        btnLimpiarFiltro = view.findViewById(R.id.btnLimpiarFiltro);
        layoutNoUsuarios = view.findViewById(R.id.layoutNoUsuarios);
        txtMensajeNoUsuarios = view.findViewById(R.id.txtMensajeNoUsuarios);
    }

    private void configurarSpinnerFiltro() {
        // Crear lista de opciones para el filtro
        List<String> opcionesFiltro = new ArrayList<>();
        opcionesFiltro.add("TODOS");
        opcionesFiltro.add("ADMINISTRADOR");
        opcionesFiltro.add("VENDEDOR");
        opcionesFiltro.add("PRODUCCIÓN");

        // Configurar adapter del spinner
        spinnerAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, opcionesFiltro);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFiltroRol.setAdapter(spinnerAdapter);

        // Configurar listener del spinner
        spinnerFiltroRol.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String filtroSeleccionado = opcionesFiltro.get(position);
                aplicarFiltroRol(filtroSeleccionado);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada
            }
        });
    }

    private void configurarRecyclerView() {
        gestionarUsuariosAdapter = new GestionarUsuariosAdapter(getContext(), getParentFragmentManager());

        // Configurar el listener para el adapter
        gestionarUsuariosAdapter.setOnUsuarioActionListener(new GestionarUsuariosAdapter.OnUsuarioActionListener() {
            @Override
            public void onUsuarioEliminado(String uid) {
                // CORREGIDO: Llamar al método de instancia, no recursivo
                GestionUsuariosFragment.this.onUsuarioEliminado(uid);
            }

            @Override
            public void onUsuarioActualizado(User usuario) {
                // Actualizar la lista completa cuando se actualiza un usuario
                for (int i = 0; i < listaUsuariosCompleta.size(); i++) {
                    if (listaUsuariosCompleta.get(i).getUid().equals(usuario.getUid())) {
                        listaUsuariosCompleta.set(i, usuario);
                        break;
                    }
                }
                // Reaplicar el filtro
                aplicarFiltroRol(filtroRolActual);
            }
        });

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

        // Configurar botón limpiar filtro
        btnLimpiarFiltro.setOnClickListener(v -> {
            limpiarFiltro();
        });
    }

    private void aplicarFiltroRol(String filtro) {
        filtroRolActual = filtro;
        Log.d(TAG, "Aplicando filtro: " + filtro);

        // Mostrar/ocultar botón limpiar filtro
        if ("TODOS".equals(filtro)) {
            btnLimpiarFiltro.setVisibility(View.GONE);
        } else {
            btnLimpiarFiltro.setVisibility(View.VISIBLE);
        }

        // Aplicar filtro a la lista
        listaUsuariosFiltrada.clear();

        if ("TODOS".equals(filtro)) {
            listaUsuariosFiltrada.addAll(listaUsuariosCompleta);
        } else {
            String rolFiltro = convertirFiltroARol(filtro);
            for (User usuario : listaUsuariosCompleta) {
                if (rolFiltro.equals(usuario.getRole())) {
                    listaUsuariosFiltrada.add(usuario);
                }
            }
        }

        // Actualizar adapter y contador
        gestionarUsuariosAdapter.actualizarLista(listaUsuariosFiltrada);
        actualizarContadorUsuarios();
        actualizarVisibilidadListas();

        Log.d(TAG, "Filtro aplicado. Usuarios mostrados: " + listaUsuariosFiltrada.size());
    }

    private String convertirFiltroARol(String filtro) {
        switch (filtro) {
            case "ADMINISTRADOR":
                return User.ROLE_ADMIN;
            case "VENDEDOR":
                return User.ROLE_SELLER;
            case "PRODUCCIÓN":
                return User.ROLE_PRODUCTION;
            default:
                return "";
        }
    }

    private void limpiarFiltro() {
        spinnerFiltroRol.setSelection(0); // Seleccionar "TODOS"
        // El listener del spinner se encargará de aplicar el filtro
    }

    private void actualizarContadorUsuarios() {
        int totalFiltrados = listaUsuariosFiltrada.size();
        int totalCompletos = listaUsuariosCompleta.size();

        if ("TODOS".equals(filtroRolActual)) {
            lblContadorUsuarios.setText("Total: " + totalCompletos + " usuarios");
        } else {
            lblContadorUsuarios.setText("Filtrados: " + totalFiltrados + " de " + totalCompletos + " usuarios");
        }
    }

    private void actualizarVisibilidadListas() {
        if (listaUsuariosFiltrada.isEmpty()) {
            // Mostrar mensaje de "no usuarios"
            rvcUsuarios.setVisibility(View.GONE);
            layoutNoUsuarios.setVisibility(View.VISIBLE);

            if ("TODOS".equals(filtroRolActual)) {
                txtMensajeNoUsuarios.setText("No hay usuarios registrados");
            } else {
                txtMensajeNoUsuarios.setText("No se encontraron usuarios con el rol: " + filtroRolActual.toLowerCase());
            }
        } else {
            // Mostrar lista de usuarios
            rvcUsuarios.setVisibility(View.VISIBLE);
            layoutNoUsuarios.setVisibility(View.GONE);
        }
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

                    // Actualizar listas completas
                    listaUsuariosCompleta.clear();
                    listaUsuariosCompleta.addAll(usuarios);

                    // Aplicar filtro actual
                    aplicarFiltroRol(filtroRolActual);

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
        layoutNoUsuarios.setVisibility(View.GONE);
        lblContadorUsuarios.setText("Total: 0 usuarios");
    }

    private void actualizarBotonSegunEstado() {
        if (hayUsuarios) {
            btnAbrirDialogoUsuarios.setText("AGREGAR USUARIO");
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

        // Si hay usuarios, verificar permisos
        return hayUsuarios;
    }

    private void mostrarCargando(boolean mostrar) {
        if (mostrar) {
            btnAbrirDialogoUsuarios.setEnabled(false);
            btnAbrirDialogoUsuarios.setText("Verificando...");
            lblerrorClientes.setText("Verificando usuarios...");
            lblerrorClientes.setVisibility(View.VISIBLE);
            rvcUsuarios.setVisibility(View.GONE);
            layoutNoUsuarios.setVisibility(View.GONE);
        } else {
            btnAbrirDialogoUsuarios.setEnabled(true);
            actualizarBotonSegunEstado();
        }
    }

    private void mostrarMensajeVacio(String mensaje) {
        lblerrorClientes.setText(mensaje);
        lblerrorClientes.setVisibility(View.VISIBLE);
        rvcUsuarios.setVisibility(View.GONE);
        layoutNoUsuarios.setVisibility(View.GONE);
    }

    private void mostrarMensajeError(String mensaje) {
        lblerrorClientes.setText(mensaje);
        lblerrorClientes.setVisibility(View.VISIBLE);
        rvcUsuarios.setVisibility(View.GONE);
        layoutNoUsuarios.setVisibility(View.GONE);
    }

    private void ocultarMensajeVacio() {
        lblerrorClientes.setVisibility(View.GONE);
    }

    // Implementación de UsuarioDialogListener
    @Override
    public void onUsuarioCreado(User usuario) {
        Log.d(TAG, "Usuario creado: " + usuario.getName());

        // Actualizar estado
        hayUsuarios = true;

        // Agregar el usuario a la lista completa
        listaUsuariosCompleta.add(usuario);

        // Verificar si el usuario cumple con el filtro actual
        boolean cumpleFiltro = false;
        if ("TODOS".equals(filtroRolActual)) {
            cumpleFiltro = true;
        } else {
            String rolFiltro = convertirFiltroARol(filtroRolActual);
            cumpleFiltro = rolFiltro.equals(usuario.getRole());
        }

        if (cumpleFiltro) {
            listaUsuariosFiltrada.add(usuario);
            gestionarUsuariosAdapter.agregarUsuario(usuario);
            // Scroll al final para mostrar el nuevo usuario
            rvcUsuarios.smoothScrollToPosition(gestionarUsuariosAdapter.getItemCount() - 1);
        }

        // Ocultar mensaje de vacío si estaba visible
        ocultarMensajeVacio();

        // Actualizar contador y visibilidad
        actualizarContadorUsuarios();
        actualizarVisibilidadListas();
        actualizarBotonSegunEstado();

        // Mostrar mensaje de éxito
        String mensaje = usuario.isAdmin() && listaUsuariosCompleta.size() == 1
                ? "¡Primer administrador creado exitosamente!\nYa puedes gestionar el sistema."
                : "Usuario " + usuario.getName() + " creado exitosamente";

        Toast.makeText(getContext(), mensaje, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onUsuarioActualizado(User usuario) {
        Log.d(TAG, "Usuario actualizado: " + usuario.getName());

        // Actualizar el usuario en la lista completa
        for (int i = 0; i < listaUsuariosCompleta.size(); i++) {
            if (listaUsuariosCompleta.get(i).getUid().equals(usuario.getUid())) {
                listaUsuariosCompleta.set(i, usuario);
                break;
            }
        }

        // Reaplicar el filtro para reflejar los cambios
        aplicarFiltroRol(filtroRolActual);

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

        // Eliminar de la lista completa
        listaUsuariosCompleta.removeIf(user -> user.getUid().equals(uid));

        // Eliminar de la lista filtrada
        listaUsuariosFiltrada.removeIf(user -> user.getUid().equals(uid));

        // Actualizar adapter
        gestionarUsuariosAdapter.eliminarUsuario(uid);

        // Actualizar contador y visibilidad
        actualizarContadorUsuarios();
        actualizarVisibilidadListas();

        // Si no quedan usuarios, actualizar estado
        if (listaUsuariosCompleta.isEmpty()) {
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
        listaUsuariosCompleta.clear();
        listaUsuariosFiltrada.clear();
    }


}