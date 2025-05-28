package com.example.pasteleriapdm.FragmentsAdmin;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.pasteleriapdm.Adapters.GestionarClientesAdapter;
import com.example.pasteleriapdm.Adapters.ClientesSellerAdapter;
import com.example.pasteleriapdm.Dialogs.ClientesDialog;
import com.example.pasteleriapdm.Models.Client;
import com.example.pasteleriapdm.Models.User;
import com.example.pasteleriapdm.R;
import com.example.pasteleriapdm.Utils.DatabaseHelper;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class GestionClientesFragment extends Fragment implements
        ClientesDialog.ClienteDialogListener,
        GestionarClientesAdapter.ClienteAdapterListener,
        ClientesSellerAdapter.ClienteSellerAdapterListener {

    private static final String TAG = "GestionClientesFragment";

    // Views
    private Button btnAbrirDialogoClientes;
    private RecyclerView rvcClientes;
    private EditText txtBuscarClientes;
    private ImageView iconClear;
    private TextView lblerrorClientes;

    // Adapters para diferentes roles
    private GestionarClientesAdapter gestionarClientesAdapter; // Para Admin
    private ClientesSellerAdapter clientesSellerAdapter;       // Para Seller

    // Datos
    private List<Client> listaClientesCompleta; // Lista completa de clientes
    private List<Client> listaClientesFiltrada; // Lista filtrada para mostrar

    private DatabaseHelper databaseHelper;
    private FirebaseAuth mAuth;
    private User currentUser;
    private boolean isAdmin = false;
    private boolean isInitialized = false;

    // Variables para búsqueda
    private String textoBusquedaActual = "";

    public GestionClientesFragment() {
        // Required empty public constructor
    }

    public static GestionClientesFragment newInstance() {
        return new GestionClientesFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() - Inicializando fragment");

        try {
            databaseHelper = DatabaseHelper.getInstance();
            mAuth = FirebaseAuth.getInstance();

            // Inicializar listas
            listaClientesCompleta = new ArrayList<>();
            listaClientesFiltrada = new ArrayList<>();

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
            View view = inflater.inflate(R.layout.fragment_gestion_clientes, container, false);

            initializeViews(view);

            // Verificar que las vistas se inicializaron correctamente
            if (btnAbrirDialogoClientes == null || rvcClientes == null || txtBuscarClientes == null) {
                Log.e(TAG, "Error: Vistas no inicializadas correctamente");
                Toast.makeText(getContext(), "Error inicializando vistas", Toast.LENGTH_LONG).show();
                return view;
            }

            // Configurar búsqueda
            setupSearchFunctionality();

            verificarUsuarioYConfigurar();

            return view;

        } catch (Exception e) {
            Log.e(TAG, "Error en onCreateView: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error creando vista: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return inflater.inflate(R.layout.fragment_gestion_clientes, container, false);
        }
    }

    private void initializeViews(View view) {
        try {
            Log.d(TAG, "Inicializando vistas...");

            btnAbrirDialogoClientes = view.findViewById(R.id.btnAbrirDialogoClientes);
            rvcClientes = view.findViewById(R.id.rvcClientes);
            txtBuscarClientes = view.findViewById(R.id.txtBuscarClientes);
            iconClear = view.findViewById(R.id.iconClear);
            lblerrorClientes = view.findViewById(R.id.lblerrorClientes);

            // Verificar vistas críticas
            if (btnAbrirDialogoClientes == null) {
                Log.e(TAG, "btnAbrirDialogoClientes es null - verificar R.id.btnAbrirDialogoClientes");
            }
            if (rvcClientes == null) {
                Log.e(TAG, "rvcClientes es null - verificar R.id.rvcClientes");
            }
            if (txtBuscarClientes == null) {
                Log.e(TAG, "txtBuscarClientes es null - verificar R.id.txtBuscarClientes");
            }
            if (iconClear == null) {
                Log.e(TAG, "iconClear es null - verificar R.id.iconClear");
            }

            Log.d(TAG, "Vistas inicializadas correctamente");

        } catch (Exception e) {
            Log.e(TAG, "Error inicializando vistas: " + e.getMessage(), e);
        }
    }

    private void setupSearchFunctionality() {
        try {
            Log.d(TAG, "Configurando funcionalidad de búsqueda");

            if (txtBuscarClientes == null || iconClear == null) {
                Log.e(TAG, "Vistas de búsqueda no inicializadas");
                return;
            }

            // Configurar TextWatcher para búsqueda en tiempo real
            txtBuscarClientes.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // No necesitamos implementar esto
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Actualizar búsqueda en tiempo real
                    String textoBusqueda = s.toString().trim();
                    textoBusquedaActual = textoBusqueda;

                    // Mostrar/ocultar botón de limpiar
                    if (textoBusqueda.isEmpty()) {
                        iconClear.setVisibility(View.GONE);
                    } else {
                        iconClear.setVisibility(View.VISIBLE);
                    }

                    // Filtrar lista
                    filtrarClientes(textoBusqueda);
                }

                @Override
                public void afterTextChanged(Editable s) {
                    // No necesitamos implementar esto
                }
            });

            // Configurar botón de limpiar
            iconClear.setOnClickListener(v -> {
                txtBuscarClientes.setText("");
                txtBuscarClientes.clearFocus();
                // El TextWatcher se encargará del resto
            });

            Log.d(TAG, "Funcionalidad de búsqueda configurada correctamente");

        } catch (Exception e) {
            Log.e(TAG, "Error configurando búsqueda: " + e.getMessage(), e);
        }
    }

    private void filtrarClientes(String textoBusqueda) {
        try {
            Log.d(TAG, "Filtrando clientes con texto: '" + textoBusqueda + "'");

            if (listaClientesCompleta == null) {
                Log.w(TAG, "Lista completa de clientes es null");
                return;
            }

            // Limpiar lista filtrada
            listaClientesFiltrada.clear();

            if (textoBusqueda.isEmpty()) {
                // Si no hay texto de búsqueda, mostrar todos los clientes
                listaClientesFiltrada.addAll(listaClientesCompleta);
            } else {
                // Filtrar por nombre (búsqueda case-insensitive)
                String textoBusquedaLower = textoBusqueda.toLowerCase();

                for (Client cliente : listaClientesCompleta) {
                    if (cliente != null && cliente.getName() != null) {
                        String nombreCliente = cliente.getName().toLowerCase();

                        // Verificar si el nombre contiene el texto de búsqueda
                        if (nombreCliente.contains(textoBusquedaLower)) {
                            listaClientesFiltrada.add(cliente);
                        }
                    }
                }
            }

            Log.d(TAG, "Filtro aplicado - Total: " + listaClientesCompleta.size() +
                    ", Filtrados: " + listaClientesFiltrada.size());

            // Actualizar adapter correspondiente
            actualizarAdapterConListaFiltrada();

            // Mostrar mensaje si no hay resultados
            mostrarMensajeResultados();

        } catch (Exception e) {
            Log.e(TAG, "Error filtrando clientes: " + e.getMessage(), e);
        }
    }

    private void actualizarAdapterConListaFiltrada() {
        try {
            if (isAdmin && gestionarClientesAdapter != null) {
                gestionarClientesAdapter.actualizarLista(listaClientesFiltrada);
            } else if (!isAdmin && clientesSellerAdapter != null) {
                clientesSellerAdapter.actualizarLista(listaClientesFiltrada);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error actualizando adapter: " + e.getMessage(), e);
        }
    }

    private void mostrarMensajeResultados() {
        try {
            if (lblerrorClientes == null) return;

            if (!textoBusquedaActual.isEmpty() && listaClientesFiltrada.isEmpty()) {
                // No hay resultados para la búsqueda
                lblerrorClientes.setText("No se encontraron clientes con el nombre: \"" + textoBusquedaActual + "\"");
                lblerrorClientes.setVisibility(View.VISIBLE);
            } else if (textoBusquedaActual.isEmpty() && listaClientesCompleta.isEmpty()) {
                // No hay clientes en total
                String mensaje = isAdmin ? "No hay clientes registrados" : "No has creado clientes aún";
                lblerrorClientes.setText(mensaje);
                lblerrorClientes.setVisibility(View.VISIBLE);
            } else {
                // Hay resultados o no se está buscando
                lblerrorClientes.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error mostrando mensaje de resultados: " + e.getMessage(), e);
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
                                    setupRecyclerView();
                                    setupListeners();
                                    cargarClientes();
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

    private void setupRecyclerView() {
        try {
            if (rvcClientes == null) {
                Log.e(TAG, "rvcClientes es null, no se puede configurar RecyclerView");
                return;
            }

            // Limpiar adapter anterior si existe
            rvcClientes.setAdapter(null);

            if (isAdmin) {
                // Configurar adapter para Admin (vista completa)
                Log.d(TAG, "Configurando adapter para Admin");
                gestionarClientesAdapter = new GestionarClientesAdapter(getContext(), getParentFragmentManager());
                gestionarClientesAdapter.setClienteAdapterListener(this);

                rvcClientes.setLayoutManager(new LinearLayoutManager(getContext()));
                rvcClientes.setAdapter(gestionarClientesAdapter);

                // Limpiar adapter de seller
                clientesSellerAdapter = null;

                Log.d(TAG, "Adapter para Admin configurado correctamente");
            } else {
                // Configurar adapter para Seller (vista simplificada)
                Log.d(TAG, "Configurando adapter para Seller");
                clientesSellerAdapter = new ClientesSellerAdapter(getContext(), getParentFragmentManager());
                clientesSellerAdapter.setClienteSellerAdapterListener(this);

                rvcClientes.setLayoutManager(new LinearLayoutManager(getContext()));
                rvcClientes.setAdapter(clientesSellerAdapter);

                // Limpiar adapter de admin
                gestionarClientesAdapter = null;

                Log.d(TAG, "Adapter para Seller configurado correctamente");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error configurando RecyclerView: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error configurando lista", Toast.LENGTH_LONG).show();
        }
    }

    private void setupListeners() {
        try {
            if (btnAbrirDialogoClientes != null) {
                btnAbrirDialogoClientes.setOnClickListener(v -> {
                    try {
                        ClientesDialog dialog = new ClientesDialog();
                        dialog.setClienteDialogListener(this);
                        dialog.show(getParentFragmentManager(), "clientesDialogo");
                    } catch (Exception e) {
                        Log.e(TAG, "Error abriendo diálogo: " + e.getMessage(), e);
                        Toast.makeText(getContext(), "Error abriendo diálogo", Toast.LENGTH_SHORT).show();
                    }
                });
                Log.d(TAG, "Listeners configurados correctamente");
            } else {
                Log.e(TAG, "btnAbrirDialogoClientes es null, no se pueden configurar listeners");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error configurando listeners: " + e.getMessage(), e);
        }
    }

    private void cargarClientes() {
        try {
            Log.d(TAG, "Cargando clientes... Es Admin: " + isAdmin);

            if (isAdmin) {
                // Admin: cargar todos los clientes
                cargarTodosLosClientes();
            } else {
                // Seller: cargar solo sus clientes
                cargarClientesDelSeller();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error en cargarClientes: " + e.getMessage(), e);
        }
    }

    private void cargarTodosLosClientes() {
        try {
            databaseHelper.getAllClients(new DatabaseHelper.DatabaseCallback<List<Client>>() {
                @Override
                public void onSuccess(List<Client> clientes) {
                    if (getActivity() != null && isAdded()) {
                        getActivity().runOnUiThread(() -> {
                            try {
                                Log.d(TAG, "Clientes cargados (Admin): " + clientes.size());

                                // Actualizar lista completa
                                listaClientesCompleta.clear();
                                if (clientes != null) {
                                    listaClientesCompleta.addAll(clientes);
                                }

                                // Aplicar filtro actual (si existe)
                                filtrarClientes(textoBusquedaActual);

                                Log.d(TAG, "Lista de clientes actualizada correctamente");

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
                            Log.e(TAG, "Error cargando clientes: " + error);
                            Toast.makeText(getContext(), "Error cargando clientes: " + error, Toast.LENGTH_LONG).show();

                            // Mostrar mensaje de error
                            if (lblerrorClientes != null) {
                                lblerrorClientes.setText("Error cargando clientes: " + error);
                                lblerrorClientes.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error en cargarTodosLosClientes: " + e.getMessage(), e);
        }
    }

    private void cargarClientesDelSeller() {
        try {
            String sellerId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

            if (sellerId == null) {
                Toast.makeText(getContext(), "Error: Usuario no autenticado", Toast.LENGTH_LONG).show();
                return;
            }

            Log.d(TAG, "Cargando clientes para seller: " + sellerId);

            databaseHelper.getClientsBySeller(sellerId, new DatabaseHelper.DatabaseCallback<List<Client>>() {
                @Override
                public void onSuccess(List<Client> clientes) {
                    if (getActivity() != null && isAdded()) {
                        getActivity().runOnUiThread(() -> {
                            try {
                                Log.d(TAG, "Clientes cargados (Seller): " + clientes.size());

                                // Actualizar lista completa
                                listaClientesCompleta.clear();
                                if (clientes != null) {
                                    listaClientesCompleta.addAll(clientes);
                                }

                                // Aplicar filtro actual (si existe)
                                filtrarClientes(textoBusquedaActual);

                                Log.d(TAG, "Lista de clientes del seller actualizada correctamente");

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
                            Log.e(TAG, "Error cargando clientes del seller: " + error);
                            Toast.makeText(getContext(), "Error cargando tus clientes: " + error, Toast.LENGTH_LONG).show();

                            // Mostrar mensaje de error
                            if (lblerrorClientes != null) {
                                lblerrorClientes.setText("Error cargando tus clientes: " + error);
                                lblerrorClientes.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error en cargarClientesDelSeller: " + e.getMessage(), e);
        }
    }

    // Implementación de ClienteDialogListener
    @Override
    public void onClienteCreado() {
        try {
            Log.d(TAG, "Cliente creado, recargando lista");
            cargarClientes();
            Toast.makeText(getContext(), "Cliente creado - Lista actualizada", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error en onClienteCreado: " + e.getMessage(), e);
        }
    }

    @Override
    public void onClienteEditado() {
        try {
            Log.d(TAG, "Cliente editado, recargando lista");
            cargarClientes();
            Toast.makeText(getContext(), "Cliente editado - Lista actualizada", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error en onClienteEditado: " + e.getMessage(), e);
        }
    }

    // Implementación de ClienteAdapterListener (Admin)
    @Override
    public void onClienteEliminado() {
        try {
            Log.d(TAG, "Cliente eliminado, recargando lista");
            cargarClientes();
            Toast.makeText(getContext(), "Cliente eliminado - Lista actualizada", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error en onClienteEliminado: " + e.getMessage(), e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            Log.d(TAG, "onResume() - isInitialized: " + isInitialized);
            // Recargar datos cuando el fragment se hace visible
            if (currentUser != null && isInitialized) {
                cargarClientes();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error en onResume: " + e.getMessage(), e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            Log.d(TAG, "onDestroy() - Limpiando recursos");

            // Limpiar adapters
            gestionarClientesAdapter = null;
            clientesSellerAdapter = null;

            // Limpiar listas
            if (listaClientesCompleta != null) {
                listaClientesCompleta.clear();
            }
            if (listaClientesFiltrada != null) {
                listaClientesFiltrada.clear();
            }

            // Limpiar referencias
            currentUser = null;
            isInitialized = false;
            textoBusquedaActual = "";

        } catch (Exception e) {
            Log.e(TAG, "Error en onDestroy: " + e.getMessage(), e);
        }
    }

    /**
     * Método público para limpiar la búsqueda (útil para llamar desde otros lugares)
     */
    public void limpiarBusqueda() {
        try {
            if (txtBuscarClientes != null) {
                txtBuscarClientes.setText("");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error limpiando búsqueda: " + e.getMessage(), e);
        }
    }

    /**
     * Método público para obtener el texto de búsqueda actual
     */
    public String getTextoBusquedaActual() {
        return textoBusquedaActual;
    }
}