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
import com.example.pasteleriapdm.Dialogs.PastelesDialog;
import com.example.pasteleriapdm.Models.Cake;
import com.example.pasteleriapdm.R;
import com.example.pasteleriapdm.Utils.DatabaseHelper;

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

    // Adapter y Data
    private GestionarPastelesAdapter gestionarPastelesAdapter;
    private List<Cake> listasPasteles;
    private DatabaseHelper databaseHelper;

    public GestionPastelesFragment() {
        // Required empty public constructor
    }

    public static GestionPastelesFragment newInstance(String param1, String param2) {
        GestionPastelesFragment fragment = new GestionPastelesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseHelper = DatabaseHelper.getInstance();
        listasPasteles = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gestion_pasteles, container, false);

        AsociarElementoXML(view);
        configurarRecyclerView();
        configurarEventos();
        configurarBusqueda();
        cargarPasteles();

        return view;
    }

    private void AsociarElementoXML(View view) {
        btnAbrirDialogoPasteles = view.findViewById(R.id.btnAbrirDialogoPasteles);
        rvcPasteles = view.findViewById(R.id.rvcPasteles);
        txtBuscarPastel = view.findViewById(R.id.txtBuscarPastel);
        iconClear = view.findViewById(R.id.iconClear);
        layoutNoResults = view.findViewById(R.id.layoutNoResults);
    }

    private void configurarRecyclerView() {
        // CAMBIO CLAVE: Pasar la lista principal, no la filtrada
        gestionarPastelesAdapter = new GestionarPastelesAdapter(
                getContext(),
                getParentFragmentManager(),
                listasPasteles, // ← Usar lista principal
                this::editarPastel,
                this::eliminarPastel
        );
        rvcPasteles.setLayoutManager(new LinearLayoutManager(getContext()));
        rvcPasteles.setAdapter(gestionarPastelesAdapter);
    }

    private void configurarEventos() {
        btnAbrirDialogoPasteles.setOnClickListener(v -> {
            PastelesDialog dialog = PastelesDialog.newInstance(PastelesDialog.MODE_CREATE, null);
            dialog.setOnCakeOperationListener(this);
            dialog.show(getParentFragmentManager(), "crearPastelDialog");
        });
    }

    private void configurarBusqueda() {
        // Configurar TextWatcher para el campo de búsqueda
        txtBuscarPastel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No necesario
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // CAMBIO CLAVE: Usar el método del adapter
                String textoBusqueda = s.toString();
                Log.d(TAG, "Búsqueda iniciada: '" + textoBusqueda + "'");

                gestionarPastelesAdapter.filtrarPorNombre(textoBusqueda);

                // Mostrar/ocultar el botón de limpiar
                if (s.length() > 0) {
                    iconClear.setVisibility(View.VISIBLE);
                } else {
                    iconClear.setVisibility(View.GONE);
                }

                // Mostrar/ocultar mensaje de no resultados
                mostrarMensajeNoResultados();
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
            gestionarPastelesAdapter.limpiarFiltro();
            mostrarMensajeNoResultados();
        });
    }

    private void mostrarMensajeNoResultados() {
        String textoBusqueda = txtBuscarPastel.getText().toString().trim();
        boolean hayBusqueda = !textoBusqueda.isEmpty();
        boolean hayResultados = gestionarPastelesAdapter.hayResultados();

        Log.d(TAG, "Verificando resultados - Búsqueda: '" + textoBusqueda + "', Hay resultados: " + hayResultados);

        if (hayBusqueda && !hayResultados) {
            // Hay búsqueda pero no hay resultados
            layoutNoResults.setVisibility(View.VISIBLE);
            rvcPasteles.setVisibility(View.GONE);
            Log.d(TAG, "Mostrando mensaje de no resultados");
        } else {
            // Hay resultados o no hay búsqueda
            layoutNoResults.setVisibility(View.GONE);
            rvcPasteles.setVisibility(View.VISIBLE);
            Log.d(TAG, "Mostrando RecyclerView con " + gestionarPastelesAdapter.getCantidadResultados() + " resultados");
        }
    }

    private void cargarPasteles() {
        Log.d(TAG, "Iniciando carga de pasteles...");

        databaseHelper.getAllCakes(new DatabaseHelper.DatabaseCallback<List<Cake>>() {
            @Override
            public void onSuccess(List<Cake> result) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Log.d(TAG, "Pasteles recibidos de BD: " + result.size());

                        // Limpiar y actualizar la lista principal
                        listasPasteles.clear();
                        listasPasteles.addAll(result);

                        // Log para verificar los pasteles cargados
                        for (Cake cake : result) {
                            Log.d(TAG, "Pastel cargado: " + cake.getName());
                        }

                        // CAMBIO CLAVE: Usar actualizarLista del adapter
                        gestionarPastelesAdapter.actualizarLista(listasPasteles);

                        // Aplicar filtro actual si hay búsqueda activa
                        String textoBusquedaActual = txtBuscarPastel.getText().toString();
                        if (!textoBusquedaActual.isEmpty()) {
                            gestionarPastelesAdapter.filtrarPorNombre(textoBusquedaActual);
                        }

                        mostrarMensajeNoResultados();

                        Log.d(TAG, "Pasteles cargados y adapter actualizado");
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Log.e(TAG, "Error cargando pasteles: " + error);
                        Toast.makeText(getContext(), "Error cargando pasteles: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void editarPastel(Cake cake) {
        PastelesDialog dialog = PastelesDialog.newInstance(PastelesDialog.MODE_EDIT, cake);
        dialog.setOnCakeOperationListener(this);
        dialog.show(getParentFragmentManager(), "editarPastelDialog");
    }

    private void eliminarPastel(Cake cake) {
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
        databaseHelper.permanentDeleteCake(cake.getId(), new DatabaseHelper.DatabaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Pastel eliminado permanentemente", Toast.LENGTH_SHORT).show();

                        // CAMBIO CLAVE: Usar métodos del adapter
                        listasPasteles.removeIf(c -> c.getId().equals(cake.getId()));
                        gestionarPastelesAdapter.eliminarPastel(cake.getId());

                        mostrarMensajeNoResultados();
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Log.e(TAG, "Error eliminando pastel permanentemente: " + error);
                        Toast.makeText(getContext(), "Error eliminando pastel: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    // Implementación del listener del diálogo
    @Override
    public void onCakeCreated(Cake cake) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Log.d(TAG, "Pastel creado: " + cake.getName());

                listasPasteles.add(cake);
                gestionarPastelesAdapter.agregarPastel(cake);

                // Si hay búsqueda activa, aplicar filtro
                String textoBusquedaActual = txtBuscarPastel.getText().toString();
                if (!textoBusquedaActual.isEmpty()) {
                    gestionarPastelesAdapter.filtrarPorNombre(textoBusquedaActual);
                }

                mostrarMensajeNoResultados();
                Toast.makeText(getContext(), "Pastel creado exitosamente", Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public void onCakeUpdated(Cake cake) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Log.d(TAG, "Pastel actualizado: " + cake.getName());

                // Actualizar en la lista principal
                for (int i = 0; i < listasPasteles.size(); i++) {
                    if (listasPasteles.get(i).getId().equals(cake.getId())) {
                        listasPasteles.set(i, cake);
                        break;
                    }
                }

                // Usar método del adapter
                gestionarPastelesAdapter.actualizarPastel(cake);

                // Si hay búsqueda activa, aplicar filtro
                String textoBusquedaActual = txtBuscarPastel.getText().toString();
                if (!textoBusquedaActual.isEmpty()) {
                    gestionarPastelesAdapter.filtrarPorNombre(textoBusquedaActual);
                }

                mostrarMensajeNoResultados();
                Toast.makeText(getContext(), "Pastel actualizado exitosamente", Toast.LENGTH_SHORT).show();
            });
        }
    }

    // Método público para refrescar la lista desde fuera si es necesario
    public void refrescarListaPasteles() {
        cargarPasteles();
    }

    // Método para limpiar la búsqueda programáticamente
    public void limpiarBusqueda() {
        if (txtBuscarPastel != null) {
            txtBuscarPastel.setText("");
            gestionarPastelesAdapter.limpiarFiltro();
            mostrarMensajeNoResultados();
        }
    }
}