package com.example.pasteleriapdm.FragmentsAdmin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.pasteleriapdm.Adapters.GestionarPastelesAdapter;
import com.example.pasteleriapdm.Adapters.PastelesSellerAdapter;
import com.example.pasteleriapdm.Dialogs.PastelesDialog;
import com.example.pasteleriapdm.Models.Cake;
import com.example.pasteleriapdm.Models.User;
import com.example.pasteleriapdm.R;
import com.example.pasteleriapdm.Utils.DatabaseHelper;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class GestionPastelesFragment extends Fragment implements PastelesDialog.OnCakeOperationListener {
    private static final String TAG = "GestionPastelesFragment";

    // UI Components
    private Button btnAbrirDialogoPasteles;
    private RecyclerView rvcPasteles;
    private EditText txtBuscarPastel;
    private ImageView iconClear;
    private LinearLayout layoutNoResults;

    // Adapters y Data
    private GestionarPastelesAdapter gestionarPastelesAdapter; // Para admin
    private PastelesSellerAdapter pastelesSellerAdapter; // Para seller
    private List<Cake> listasPasteles;
    private DatabaseHelper databaseHelper;

    // Firebase y Usuario
    private FirebaseAuth mAuth;
    private User currentUser;
    private boolean isAdmin = false;
    private boolean isInitialized = false;

    public GestionPastelesFragment() {
        // Required empty public constructor
    }

    public static GestionPastelesFragment newInstance() {
        return new GestionPastelesFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() - Inicializando fragment");

        try {
            databaseHelper = DatabaseHelper.getInstance();
            mAuth = FirebaseAuth.getInstance();
            listasPasteles = new ArrayList<>();

            Log.d(TAG, "DatabaseHelper y FirebaseAuth inicializados correctamente");
        } catch (Exception e) {
            Log.e(TAG, "Error en onCreate: " + e.getMessage(), e);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView() - Creando vista");

        try {
            View view = inflater.inflate(R.layout.fragment_gestion_pasteles, container, false);

            initializeViews(view);
            configurarBusqueda();

            // Verificar usuario y configurar interfaz
            verificarUsuarioYConfigurar();

            return view;

        } catch (Exception e) {
            Log.e(TAG, "Error en onCreateView: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error creando vista: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return inflater.inflate(R.layout.fragment_gestion_pasteles, container, false);
        }
    }

    private void initializeViews(View view) {
        try {
            Log.d(TAG, "Inicializando vistas...");

            btnAbrirDialogoPasteles = view.findViewById(R.id.btnAbrirDialogoPasteles);
            rvcPasteles = view.findViewById(R.id.rvcPasteles);
            txtBuscarPastel = view.findViewById(R.id.txtBuscarPastel);
            iconClear = view.findViewById(R.id.iconClear);
            layoutNoResults = view.findViewById(R.id.layoutNoResults);

            // Verificar vistas críticas
            if (btnAbrirDialogoPasteles == null) {
                Log.e(TAG, "btnAbrirDialogoPasteles es null");
            }
            if (rvcPasteles == null) {
                Log.e(TAG, "rvcPasteles es null");
            }
            if (txtBuscarPastel == null) {
                Log.e(TAG, "txtBuscarPastel es null");
            }

            Log.d(TAG, "Vistas inicializadas correctamente");

        } catch (Exception e) {
            Log.e(TAG, "Error inicializando vistas: " + e.getMessage(), e);
        }
    }

    private void verificarUsuarioYConfigurar() {
        try {
            String currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

            if (currentUserId == null) {
                Log.e(TAG, "Usuario no autenticado");
                Toast.makeText(getContext(), "Error: Usuario no autenticado", Toast.LENGTH_LONG).show();
                return;
            }

            Log.d(TAG, "Verificando usuario con ID: " + currentUserId);

            databaseHelper.getUser(currentUserId, new DatabaseHelper.DatabaseCallback<User>() {
                @Override
                public void onSuccess(User user) {
                    if (getActivity() != null && isAdded()) {
                        try {
                            currentUser = user;

                            if (user == null) {
                                Log.e(TAG, "Usuario obtenido es null");
                                getActivity().runOnUiThread(() -> {
                                    Toast.makeText(getContext(), "Error: Usuario no encontrado en BD", Toast.LENGTH_LONG).show();
                                });
                                return;
                            }

                            isAdmin = user.isAdmin();
                            Log.d(TAG, "Usuario verificado - Nombre: " + user.getName() + ", Es Admin: " + isAdmin);

                            getActivity().runOnUiThread(() -> {
                                try {
                                    configurarUI();
                                    configurarRecyclerView();
                                    configurarEventos();
                                    cargarPasteles();
                                    isInitialized = true;
                                } catch (Exception e) {
                                    Log.e(TAG, "Error en configuración post-verificación: " + e.getMessage(), e);
                                    Toast.makeText(getContext(), "Error configurando interfaz", Toast.LENGTH_LONG).show();
                                }
                            });

                        } catch (Exception e) {
                            Log.e(TAG, "Error procesando usuario: " + e.getMessage(), e);
                        }
                    }
                }

                @Override
                public void onError(String error) {
                    if (getActivity() != null && isAdded()) {
                        getActivity().runOnUiThread(() -> {
                            Log.e(TAG, "Error obteniendo usuario: " + error);
                            Toast.makeText(getContext(), "Error verificando usuario: " + error, Toast.LENGTH_LONG).show();
                        });
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error en verificarUsuarioYConfigurar: " + e.getMessage(), e);
        }
    }

    private void configurarUI() {
        try {
            Log.d(TAG, "=== CONFIGURAR UI DEBUG ===");
            Log.d(TAG, "configurarUI - Configurando para usuario Admin: " + isAdmin);

            // Configurar visibilidad del botón según tipo de usuario
            if (btnAbrirDialogoPasteles != null) {
                if (isAdmin) {
                    // Los admins sí pueden crear pasteles
                    btnAbrirDialogoPasteles.setVisibility(View.VISIBLE);
                    Log.d(TAG, "configurarUI - Botón VISIBLE para admin");
                } else {
                    // Los sellers no pueden crear pasteles
                    btnAbrirDialogoPasteles.setVisibility(View.GONE);
                    Log.d(TAG, "configurarUI - Botón OCULTADO para seller");
                }
            } else {
                Log.e(TAG, "configurarUI - ERROR: btnAbrirDialogoPasteles es NULL");
            }

            Log.d(TAG, "============================");
        } catch (Exception e) {
            Log.e(TAG, "Error configurando UI: " + e.getMessage(), e);
        }
    }

    private void configurarRecyclerView() {
        try {
            Log.d(TAG, "=== CONFIGURAR RECYCLERVIEW DEBUG ===");
            Log.d(TAG, "configurarRecyclerView - Configurando para Admin: " + isAdmin);

            if (rvcPasteles == null) {
                Log.e(TAG, "rvcPasteles es null, no se puede configurar RecyclerView");
                return;
            }

            // Limpiar adapter anterior si existe
            rvcPasteles.setAdapter(null);

            if (isAdmin) {
                // Configurar adapter para admin (todos los pasteles, con botones de acción)
                Log.d(TAG, "configurarRecyclerView - *** CONFIGURANDO ADAPTER PARA ADMIN ***");
                gestionarPastelesAdapter = new GestionarPastelesAdapter(
                        getContext(),
                        getParentFragmentManager(),
                        listasPasteles,
                        this::editarPastel,
                        this::eliminarPastel
                );
                rvcPasteles.setLayoutManager(new LinearLayoutManager(getContext()));
                rvcPasteles.setAdapter(gestionarPastelesAdapter);


                pastelesSellerAdapter = null;

                Log.d(TAG, "configurarRecyclerView - GestionarPastelesAdapter configurado exitosamente");
            } else {
                // Configurar adapter para seller (solo pasteles activos, sin botones de acción)
                Log.d(TAG, "configurarRecyclerView - *** CONFIGURANDO ADAPTER PARA SELLER ***");
                pastelesSellerAdapter = new PastelesSellerAdapter(getContext(), listasPasteles);
                rvcPasteles.setLayoutManager(new LinearLayoutManager(getContext()));
                rvcPasteles.setAdapter(pastelesSellerAdapter);


                gestionarPastelesAdapter = null;

                Log.d(TAG, "configurarRecyclerView - PastelesSellerAdapter configurado exitosamente");
            }

            Log.d(TAG, "configurarRecyclerView - Estado final:");
            Log.d(TAG, "configurarRecyclerView - pastelesSellerAdapter: " + (pastelesSellerAdapter != null ? "CONFIGURADO" : "NULL"));
            Log.d(TAG, "configurarRecyclerView - gestionarPastelesAdapter: " + (gestionarPastelesAdapter != null ? "CONFIGURADO" : "NULL"));
            Log.d(TAG, "======================================");

        } catch (Exception e) {
            Log.e(TAG, "Error configurando RecyclerView: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error configurando lista", Toast.LENGTH_LONG).show();
        }
    }

    private void configurarEventos() {
        try {
            Log.d(TAG, "=== CONFIGURAR EVENTOS DEBUG ===");
            Log.d(TAG, "configurarEventos - Configurando eventos para Admin: " + isAdmin);

            // Solo configurar el evento del botón si es admin
            if (isAdmin && btnAbrirDialogoPasteles != null) {
                btnAbrirDialogoPasteles.setOnClickListener(v -> {
                    try {
                        Log.d(TAG, "configurarEventos - Botón clickeado por admin");
                        PastelesDialog dialog = PastelesDialog.newInstance(PastelesDialog.MODE_CREATE, null);
                        dialog.setOnCakeOperationListener(this);
                        dialog.show(getParentFragmentManager(), "crearPastelDialog");
                    } catch (Exception e) {
                        Log.e(TAG, "Error abriendo diálogo: " + e.getMessage(), e);
                        Toast.makeText(getContext(), "Error abriendo diálogo", Toast.LENGTH_SHORT).show();
                    }
                });
                Log.d(TAG, "configurarEventos - Listener configurado para admin");
            } else {
                Log.d(TAG, "configurarEventos - No se configura listener para seller");
            }
            Log.d(TAG, "=================================");
        } catch (Exception e) {
            Log.e(TAG, "Error configurando eventos: " + e.getMessage(), e);
        }
    }

    private void configurarBusqueda() {
        try {
            if (txtBuscarPastel == null || iconClear == null) {
                Log.e(TAG, "Vistas de búsqueda no inicializadas");
                return;
            }

            txtBuscarPastel.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String textoBusqueda = s.toString();
                    Log.d(TAG, "=== BÚSQUEDA DEBUG ===");
                    Log.d(TAG, "Búsqueda iniciada: '" + textoBusqueda + "' para Admin: " + isAdmin);
                    Log.d(TAG, "pastelesSellerAdapter: " + (pastelesSellerAdapter != null ? "EXISTE" : "NULL"));
                    Log.d(TAG, "gestionarPastelesAdapter: " + (gestionarPastelesAdapter != null ? "EXISTE" : "NULL"));

                    // Aplicar filtro según el adapter correcto
                    if (!isAdmin && pastelesSellerAdapter != null) {
                        Log.d(TAG, "Aplicando filtro en PastelesSellerAdapter");
                        pastelesSellerAdapter.filtrarPorNombre(textoBusqueda);
                    } else if (isAdmin && gestionarPastelesAdapter != null) {
                        Log.d(TAG, "Aplicando filtro en GestionarPastelesAdapter");
                        gestionarPastelesAdapter.filtrarPorNombre(textoBusqueda);
                    } else {
                        Log.e(TAG, "ERROR: No se pudo aplicar filtro - isAdmin: " + isAdmin);
                        Log.e(TAG, "ERROR: pastelesSellerAdapter null: " + (pastelesSellerAdapter == null));
                        Log.e(TAG, "ERROR: gestionarPastelesAdapter null: " + (gestionarPastelesAdapter == null));
                    }

                    // Mostrar/ocultar el botón de limpiar
                    if (s.length() > 0) {
                        iconClear.setVisibility(View.VISIBLE);
                    } else {
                        iconClear.setVisibility(View.GONE);
                    }

                    // Mostrar/ocultar mensaje de no resultados
                    mostrarMensajeNoResultados();
                    Log.d(TAG, "======================");
                }

                @Override
                public void afterTextChanged(Editable s) {
                    // No necesario
                }
            });

            // Configurar botón para limpiar búsqueda
            iconClear.setOnClickListener(v -> {
                txtBuscarPastel.setText("");
                txtBuscarPastel.clearFocus();

                // Limpiar filtro según el adapter correcto
                if (!isAdmin && pastelesSellerAdapter != null) {
                    pastelesSellerAdapter.limpiarFiltro();
                } else if (isAdmin && gestionarPastelesAdapter != null) {
                    gestionarPastelesAdapter.limpiarFiltro();
                }

                mostrarMensajeNoResultados();
            });

            Log.d(TAG, "Funcionalidad de búsqueda configurada correctamente");

        } catch (Exception e) {
            Log.e(TAG, "Error configurando búsqueda: " + e.getMessage(), e);
        }
    }

    private void mostrarMensajeNoResultados() {
        try {
            String textoBusqueda = txtBuscarPastel.getText().toString().trim();
            boolean hayBusqueda = !textoBusqueda.isEmpty();
            boolean hayResultados = false;

            // Verificar resultados según el adapter correcto
            if (!isAdmin && pastelesSellerAdapter != null) {
                hayResultados = pastelesSellerAdapter.hayResultados();
                Log.d(TAG, "mostrarMensajeNoResultados - Seller adapter tiene resultados: " + hayResultados);
            } else if (isAdmin && gestionarPastelesAdapter != null) {
                hayResultados = gestionarPastelesAdapter.hayResultados();
                Log.d(TAG, "mostrarMensajeNoResultados - Admin adapter tiene resultados: " + hayResultados);
            }

            Log.d(TAG, "Verificando resultados - Búsqueda: '" + textoBusqueda + "', Hay resultados: " + hayResultados);

            if (layoutNoResults != null && rvcPasteles != null) {
                if (hayBusqueda && !hayResultados) {
                    // Hay búsqueda pero no hay resultados
                    layoutNoResults.setVisibility(View.VISIBLE);
                    rvcPasteles.setVisibility(View.GONE);
                    Log.d(TAG, "Mostrando mensaje de no resultados");
                } else {
                    // Hay resultados o no hay búsqueda
                    layoutNoResults.setVisibility(View.GONE);
                    rvcPasteles.setVisibility(View.VISIBLE);

                    int cantidadResultados = 0;
                    if (!isAdmin && pastelesSellerAdapter != null) {
                        cantidadResultados = pastelesSellerAdapter.getCantidadResultados();
                    } else if (isAdmin && gestionarPastelesAdapter != null) {
                        cantidadResultados = gestionarPastelesAdapter.getCantidadResultados();
                    }

                    Log.d(TAG, "Mostrando RecyclerView con " + cantidadResultados + " resultados");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error mostrando mensaje de resultados: " + e.getMessage(), e);
        }
    }

    private void cargarPasteles() {
        try {
            Log.d(TAG, "=== CARGAR PASTELES DEBUG ===");
            Log.d(TAG, "cargarPasteles - Iniciando carga para Admin: " + isAdmin);

            if (!isAdmin) {
                Log.d(TAG, "cargarPasteles - *** CARGANDO PASTELES ACTIVOS PARA SELLER ***");
                // Para sellers: solo pasteles activos
                databaseHelper.getActiveCakes(new DatabaseHelper.DatabaseCallback<List<Cake>>() {
                    @Override
                    public void onSuccess(List<Cake> result) {
                        if (getActivity() != null && isAdded()) {
                            getActivity().runOnUiThread(() -> {
                                try {
                                    Log.d(TAG, "cargarPasteles - Pasteles activos recibidos: " + result.size());

                                    // Limpiar y actualizar la lista principal
                                    listasPasteles.clear();
                                    if (result != null) {
                                        listasPasteles.addAll(result);
                                    }

                                    // Solo usar adapter de seller
                                    if (pastelesSellerAdapter != null) {
                                        pastelesSellerAdapter.actualizarLista(listasPasteles);
                                        Log.d(TAG, "cargarPasteles - PastelesSellerAdapter actualizado");
                                    } else {
                                        Log.e(TAG, "cargarPasteles - ERROR: pastelesSellerAdapter es NULL!");
                                    }

                                    // Aplicar filtro actual si hay búsqueda activa
                                    String textoBusquedaActual = txtBuscarPastel.getText().toString();
                                    if (!textoBusquedaActual.isEmpty() && pastelesSellerAdapter != null) {
                                        pastelesSellerAdapter.filtrarPorNombre(textoBusquedaActual);
                                    }

                                    mostrarMensajeNoResultados();
                                    Log.d(TAG, "cargarPasteles - Pasteles activos cargados exitosamente para seller");
                                } catch (Exception e) {
                                    Log.e(TAG, "Error actualizando lista (Seller): " + e.getMessage(), e);
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(String error) {
                        if (getActivity() != null && isAdded()) {
                            getActivity().runOnUiThread(() -> {
                                Log.e(TAG, "Error cargando pasteles activos: " + error);
                                Toast.makeText(getContext(), "Error cargando pasteles: " + error, Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                });
            } else {
                Log.d(TAG, "cargarPasteles - *** CARGANDO TODOS LOS PASTELES PARA ADMIN ***");
                // Para admins: todos los pasteles
                databaseHelper.getAllCakes(new DatabaseHelper.DatabaseCallback<List<Cake>>() {
                    @Override
                    public void onSuccess(List<Cake> result) {
                        if (getActivity() != null && isAdded()) {
                            getActivity().runOnUiThread(() -> {
                                try {
                                    Log.d(TAG, "cargarPasteles - Todos los pasteles recibidos: " + result.size());

                                    // Limpiar y actualizar la lista principal
                                    listasPasteles.clear();
                                    if (result != null) {
                                        listasPasteles.addAll(result);
                                    }

                                    // Solo usar adapter de admin
                                    if (gestionarPastelesAdapter != null) {
                                        gestionarPastelesAdapter.actualizarLista(listasPasteles);
                                        Log.d(TAG, "cargarPasteles - GestionarPastelesAdapter actualizado");
                                    } else {
                                        Log.e(TAG, "cargarPasteles - ERROR: gestionarPastelesAdapter es NULL!");
                                    }

                                    // Aplicar filtro actual si hay búsqueda activa
                                    String textoBusquedaActual = txtBuscarPastel.getText().toString();
                                    if (!textoBusquedaActual.isEmpty() && gestionarPastelesAdapter != null) {
                                        gestionarPastelesAdapter.filtrarPorNombre(textoBusquedaActual);
                                    }

                                    mostrarMensajeNoResultados();
                                    Log.d(TAG, "cargarPasteles - Todos los pasteles cargados exitosamente para admin");
                                } catch (Exception e) {
                                    Log.e(TAG, "Error actualizando lista (Admin): " + e.getMessage(), e);
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(String error) {
                        if (getActivity() != null && isAdded()) {
                            getActivity().runOnUiThread(() -> {
                                Log.e(TAG, "Error cargando pasteles: " + error);
                                Toast.makeText(getContext(), "Error cargando pasteles: " + error, Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                });
            }
            Log.d(TAG, "==============================");
        } catch (Exception e) {
            Log.e(TAG, "Error en cargarPasteles: " + e.getMessage(), e);
        }
    }

    // ========== MÉTODOS SOLO PARA ADMIN ==========

    private void editarPastel(Cake cake) {
        if (!isAdmin) {
            Log.w(TAG, "editarPastel - Intento de edición bloqueado para seller");
            Toast.makeText(getContext(), "No tienes permisos para editar pasteles", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Log.d(TAG, "editarPastel - Admin editando: " + cake.getName());
            PastelesDialog dialog = PastelesDialog.newInstance(PastelesDialog.MODE_EDIT, cake);
            dialog.setOnCakeOperationListener(this);
            dialog.show(getParentFragmentManager(), "editarPastelDialog");
        } catch (Exception e) {
            Log.e(TAG, "Error abriendo diálogo de edición: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error abriendo editor", Toast.LENGTH_SHORT).show();
        }
    }

    private void eliminarPastel(Cake cake) {
        if (!isAdmin) {
            Log.w(TAG, "eliminarPastel - Intento de eliminación bloqueado para seller");
            Toast.makeText(getContext(), "No tienes permisos para eliminar pasteles", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cake == null || cake.getId() == null) {
            Toast.makeText(getContext(), "Error: Datos del pastel incompletos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Solo permitir eliminar pasteles inactivos
        if (cake.getStatus() == null || !cake.getStatus().equals(Cake.STATUS_INACTIVE)) {
            Toast.makeText(getContext(),
                    "Solo se pueden eliminar pasteles inactivos. Cambie el estado del pastel a 'Inactivo' primero.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Confirmar eliminación permanente
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("¡Atención!")
                .setMessage("¿Está seguro de que desea eliminar permanentemente el pastel '" +
                        cake.getName() + "'?\n\nEsta acción no se puede deshacer.")
                .setPositiveButton("Sí, eliminar", (dialog, which) -> {
                    eliminarPastelPermanentemente(cake);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarPastelPermanentemente(Cake cake) {
        if (!isAdmin) {
            return;
        }

        databaseHelper.permanentDeleteCake(cake.getId(), new DatabaseHelper.DatabaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Pastel eliminado permanentemente", Toast.LENGTH_SHORT).show();

                        // Remover de la lista principal
                        listasPasteles.removeIf(c -> c.getId().equals(cake.getId()));
                        if (gestionarPastelesAdapter != null) {
                            gestionarPastelesAdapter.eliminarPastel(cake.getId());
                        }

                        mostrarMensajeNoResultados();
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        Log.e(TAG, "Error eliminando pastel permanentemente: " + error);
                        Toast.makeText(getContext(), "Error eliminando pastel: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

// ========== IMPLEMENTACIÓN DEL LISTENER DEL DIÁLOGO (SOLO ADMIN) ==========

    @Override
    public void onCakeCreated(Cake cake) {
        if (!isAdmin) {
            Log.w(TAG, "onCakeCreated - Intento de creación bloqueado para seller");
            return;
        }

        if (getActivity() != null && isAdded()) {
            getActivity().runOnUiThread(() -> {
                try {
                    Log.d(TAG, "onCakeCreated - Pastel creado por admin: " + cake.getName());

                    listasPasteles.add(cake);
                    if (gestionarPastelesAdapter != null) {
                        gestionarPastelesAdapter.agregarPastel(cake);
                    }

                    // Si hay búsqueda activa, aplicar filtro
                    String textoBusquedaActual = txtBuscarPastel.getText().toString();
                    if (!textoBusquedaActual.isEmpty() && gestionarPastelesAdapter != null) {
                        gestionarPastelesAdapter.filtrarPorNombre(textoBusquedaActual);
                    }

                    mostrarMensajeNoResultados();
                    Toast.makeText(getContext(), "Pastel creado exitosamente", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e(TAG, "Error en onCakeCreated: " + e.getMessage(), e);
                    Toast.makeText(getContext(), "Error procesando pastel creado", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onCakeUpdated(Cake cake) {
        if (!isAdmin) {
            Log.w(TAG, "onCakeUpdated - Intento de actualización bloqueado para seller");
            return;
        }

        if (getActivity() != null && isAdded()) {
            getActivity().runOnUiThread(() -> {
                try {
                    Log.d(TAG, "onCakeUpdated - Pastel actualizado por admin: " + cake.getName());

                    // Actualizar en la lista principal
                    for (int i = 0; i < listasPasteles.size(); i++) {
                        if (listasPasteles.get(i).getId().equals(cake.getId())) {
                            listasPasteles.set(i, cake);
                            break;
                        }
                    }

                    // Usar método del adapter
                    if (gestionarPastelesAdapter != null) {
                        gestionarPastelesAdapter.actualizarPastel(cake);
                    }

                    // Si hay busqueda activa, aplicar filtro
                    String textoBusquedaActual = txtBuscarPastel.getText().toString();
                    if (!textoBusquedaActual.isEmpty() && gestionarPastelesAdapter != null) {
                        gestionarPastelesAdapter.filtrarPorNombre(textoBusquedaActual);
                    }

                    mostrarMensajeNoResultados();
                    Toast.makeText(getContext(), "Pastel actualizado exitosamente", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e(TAG, "Error en onCakeUpdated: " + e.getMessage(), e);
                    Toast.makeText(getContext(), "Error procesando pastel actualizado", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // ========== MÉTODOS PÚBLICOS ==========

    /**
     * Metodo público para refrescar la lista desde fuera si es necesario
     */
    public void refrescarListaPasteles() {
        try {
            if (isInitialized) {
                cargarPasteles();
            } else {
                Log.w(TAG, "Fragment no inicializado, no se puede refrescar");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error refrescando lista: " + e.getMessage(), e);
        }
    }

    /**
     * Metodo para limpiar la búsqueda
     */
    public void limpiarBusqueda() {
        try {
            if (txtBuscarPastel != null) {
                txtBuscarPastel.setText("");

                if (!isAdmin && pastelesSellerAdapter != null) {
                    pastelesSellerAdapter.limpiarFiltro();
                } else if (isAdmin && gestionarPastelesAdapter != null) {
                    gestionarPastelesAdapter.limpiarFiltro();
                }

                mostrarMensajeNoResultados();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error limpiando búsqueda: " + e.getMessage(), e);
        }
    }

    /**
     * Verificar si el fragment está correctamente inicializado
     */
    public boolean isInitialized() {
        return isInitialized && currentUser != null;
    }

    /**
     * Obtener el usuario actual
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Verificar si el usuario es admin
     */
    public boolean isUserAdmin() {
        return isAdmin && currentUser != null && currentUser.isAdmin();
    }

    /**
     * Verificar si el usuario es seller
     */
    public boolean isUserSeller() {
        return !isAdmin && currentUser != null && !currentUser.isAdmin();
    }

    /**
     * Obtener la cantidad de pasteles cargados
     */
    public int getCantidadPasteles() {
        return listasPasteles != null ? listasPasteles.size() : 0;
    }

    // ========== MÉTODOS DE LIFECYCLE ==========

    @Override
    public void onResume() {
        super.onResume();
        try {
            Log.d(TAG, "onResume - Fragment reanudado");
            // Refrescar datos si es necesario
            if (isInitialized) {
                cargarPasteles();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error en onResume: " + e.getMessage(), e);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause - Fragment pausado");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            Log.d(TAG, "onDestroyView - Limpiando recursos");

            if (gestionarPastelesAdapter != null) {
                gestionarPastelesAdapter = null;
            }
            if (pastelesSellerAdapter != null) {
                pastelesSellerAdapter = null;
            }

            isInitialized = false;

        } catch (Exception e) {
            Log.e(TAG, "Error en onDestroyView: " + e.getMessage(), e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy - Fragment destruido");
    }
}