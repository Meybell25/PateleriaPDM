package com.example.pasteleriapdm.FragmentsAdmin;

import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.pasteleriapdm.Adapters.ReservaAdapter;
import com.example.pasteleriapdm.Dialogs.ReservaDialogo;
import com.example.pasteleriapdm.Models.Reservation;
import com.example.pasteleriapdm.Models.User;
import com.example.pasteleriapdm.R;
import com.example.pasteleriapdm.Utils.DatabaseHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class ReservaFragment extends Fragment {

    private static final String TAG = "ReservaFragment";
    private RecyclerView rvcReserva;
    private Spinner spinnerFiltroRservaEstado;
    private Button btnAbrirDialogoReserva;
    private DatabaseHelper dbHelper;
    private String currentUserId;
    private String currentUserRole;
    private ReservaAdapter adapter;
    private boolean isUserRoleLoaded = false;

    public ReservaFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reserva, container, false);
        AsociarElementoXML(view);

        // Obtener instancia de DatabaseHelper (Singleton)
        dbHelper = DatabaseHelper.getInstance();

        // Obtener informacion del usuario actual
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
            Log.d(TAG, "Usuario actual: " + currentUserId);

            // Inicializar RecyclerView primero
            setupRecyclerView();
            setupButtonListener();

            // Obtener el rol del usuario y luego cargar reservas
            getUserRole();
        } else {
            Toast.makeText(getContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return view;
        }

        return view;
    }

    private void AsociarElementoXML(View view) {
        btnAbrirDialogoReserva = view.findViewById(R.id.btnAbrirDialogoReservas);
        rvcReserva = view.findViewById(R.id.rvcReservas);
        spinnerFiltroRservaEstado = view.findViewById(R.id.spinnerFiltroRservaEstado);
    }

    // MÉTODO PARA OBTENER EL ROL DEL USUARIO
    private void getUserRole() {
        Log.d(TAG, "Obteniendo rol del usuario: " + currentUserId);

        dbHelper.getUser(currentUserId, new DatabaseHelper.DatabaseCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user != null && user.getRole() != null) {
                    currentUserRole = user.getRole();
                    Log.d(TAG, "Rol obtenido: " + currentUserRole);
                } else {
                    Log.w(TAG, "Rol de usuario no encontrado, usando rol por defecto");
                    currentUserRole = "seller"; // rol por defecto
                }

                isUserRoleLoaded = true;
                setupRecyclerViewWithRole();
                setupSpinnerListener();
Log.d("succses", "rol obtenido"+ currentUserRole);
                // Cargar reservas después de obtener el rol
                loadReservations();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error obteniendo usuario: " + error);
                Toast.makeText(getContext(), "Error obteniendo usuario: " + error, Toast.LENGTH_SHORT).show();
                currentUserRole = "seller"; // rol por defecto

                isUserRoleLoaded = true;
                setupRecyclerViewWithRole();
                setupSpinnerListener();
                loadReservations();
            }
        });
    }
    private void setupRecyclerView() {
        if (rvcReserva != null) {
            rvcReserva.setLayoutManager(new LinearLayoutManager(getContext()));
            Log.d(TAG, "RecyclerView configurado");
        }
    }

    private void setupRecyclerViewWithRole() {
        if (currentUserRole == null) {
            Log.e(TAG, "No se puede configurar adapter sin rol de usuario");
            return;
        }

        adapter = new ReservaAdapter(getContext(), getParentFragmentManager(), currentUserRole);

        // Configurar listener para recargar cuando hay cambios
        adapter.setOnReservationChangeListener(new ReservaAdapter.OnReservationChangeListener() {
            @Override
            public void onReservationChanged() {
                Log.d(TAG, "Recargando reservas por cambio en adapter");
                loadReservations();
            }
        });

        rvcReserva.setAdapter(adapter);
        Log.d(TAG, "Adapter configurado con rol: " + currentUserRole);
    }

    private void setupSpinnerListener() {
        if (spinnerFiltroRservaEstado != null) {
            spinnerFiltroRservaEstado.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (isUserRoleLoaded && currentUserRole != null) {
                        String selectedItem = (String) parent.getItemAtPosition(position);
                        Log.d(TAG, "Filtro seleccionado: " + selectedItem);
                        loadReservations();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }
    }

    private void setupButtonListener() {
        if (btnAbrirDialogoReserva != null) {
            btnAbrirDialogoReserva.setOnClickListener(v -> {
                ReservaDialogo dialog = new ReservaDialogo()

                // Configurar callback para actualizar la lista cuando se guarde una reserva
                dialog.setCallback(new ReservaDialogo.ReservationCallback() {
                    @Override
                    public void onReservationSaved(Reservation reservation) {
                        Log.d(TAG, "Reserva guardada, recargando lista");
                        loadReservations();
                    }

                    @Override
                    public void onReservationDeleted(String reservationId) {
                        Log.d(TAG, "Reserva eliminada, recargando lista");
                        loadReservations();
                    }
                });


                dialog.show(getParentFragmentManager(), "reservaDialogo");
            });
        }
    }

    private void loadReservations() {
        Log.d("test","hi admin?"+currentUserRole);
        // Verificaciones de seguridad
        if (!isUserRoleLoaded || currentUserRole == null) {
            Log.w(TAG, "Rol de usuario no definido aún");
            return;
        }

        if (spinnerFiltroRservaEstado == null || spinnerFiltroRservaEstado.getSelectedItem() == null) {
            Log.w(TAG, "Spinner no inicializado o sin selección");
            return;
        }

        if (adapter == null) {
            Log.w(TAG, "Adapter no inicializado");
            return;
        }

        String selectedStatus = (String) spinnerFiltroRservaEstado.getSelectedItem();
        Log.d(TAG, "Cargando reservas - Rol: " + currentUserRole + ", Filtro: " + selectedStatus);

        if ("admin".equals(currentUserRole)) {
            Log.d("test","cargando data filtro admin");
            loadAdminReservations(selectedStatus);
        } else if ("seller".equals(currentUserRole)) {
            Log.d("test","cargando data filtro seller");

            loadSellerReservations(selectedStatus);
        } else if ("production".equals(currentUserRole)) {
            Log.d("test","cargando data filtro production");

            loadProductionReservations(selectedStatus);
        } else {
            Log.w(TAG, "Rol no reconocido: " + currentUserRole);
        }
    }

    private void loadAdminReservations(String selectedStatus) {
        Log.d(TAG, "Cargando reservas como admin");

        dbHelper.getAllReservations(new DatabaseHelper.DatabaseCallback<List<Reservation>>() {
            @Override
            public void onSuccess(List<Reservation> reservations) {
                Log.d(TAG, "Reservas obtenidas (admin): " + reservations.size());
                filterAndSetReservations(reservations, selectedStatus);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error cargando reservas (admin): " + error);
                Toast.makeText(getContext(), "Error cargando reservas: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadSellerReservations(String selectedStatus) {
        Log.d(TAG, "Cargando reservas como seller para: " + currentUserId);

        dbHelper.getReservationsBySeller(currentUserId, new DatabaseHelper.DatabaseCallback<List<Reservation>>() {
            @Override
            public void onSuccess(List<Reservation> reservations) {
                Log.d(TAG, "Reservas obtenidas (seller): " + reservations.size());
                filterAndSetReservations(reservations, selectedStatus);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error cargando reservas (seller): " + error);
                Toast.makeText(getContext(), "Error cargando reservas: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProductionReservations(String selectedStatus) {
        Log.d(TAG, "Cargando reservas como production");

        dbHelper.getProductionReservations(new DatabaseHelper.DatabaseCallback<List<Reservation>>() {
            @Override
            public void onSuccess(List<Reservation> reservations) {
                Log.d(TAG, "Reservas obtenidas (production): " + reservations.size());
                filterAndSetReservations(reservations, selectedStatus);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error cargando reservas (production): " + error);
                Toast.makeText(getContext(), "Error cargando reservas: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // MÉTODO AUXILIAR PARA FILTRAR RESERVAS
    private void filterAndSetReservations(List<Reservation> reservations, String selectedStatus) {
        if (getActivity() == null) return;

        getActivity().runOnUiThread(() -> {
                    if (adapter == null) {
                        Log.e(TAG, "Adapter es null al filtrar reservas");
                        return;
                    }});

        if (adapter == null) {
            Log.e(TAG, "Adapter es null al filtrar reservas");
            setupRecyclerViewWithRole(); // Intenta reinicializar
            return;
        }

        Log.d(TAG, "Filtrando reservas - Total: " + reservations.size() + ", Filtro: " + selectedStatus);

        if ("Todos".equals(selectedStatus)) {
            adapter.setReservations(reservations);
            Log.d(TAG, "Mostrando todas las reservas: " + reservations.size());
        } else {
            List<Reservation> filtered = new ArrayList<>();
            for (Reservation r : reservations) {
                if (r.getStatus() != null && r.getStatus().equals(selectedStatus.toLowerCase())) {
                    filtered.add(r);
                }
            }
            adapter.setReservations(filtered);
            Log.d(TAG, "Reservas filtradas: " + filtered.size());
        }

        adapter.notifyDataSetChanged(); // Asegúrate de notificar los cambios
        Log.d(TAG, "Datos actualizados en el adapter");
    }

    // MÉTODO PÚBLICO PARA RECARGAR RESERVAS
    public void refreshReservations() {
        if (isUserRoleLoaded && currentUserRole != null) {
            Log.d(TAG, "Refrescando reservas manualmente");
            loadReservations();
        } else {
            Log.w(TAG, "No se puede refrescar - rol no cargado");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recargar reservas cuando el fragment vuelve a ser visible
        if (isUserRoleLoaded && currentUserRole != null) {
            Log.d(TAG, "Fragment resumido, recargando reservas");
            loadReservations();
        }
    }
}