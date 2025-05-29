package com.example.pasteleriapdm.Dialogs;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pasteleriapdm.Adapters.ReservaAdapter;
import com.example.pasteleriapdm.Adapters.ReservaItemsAdapter;
import com.example.pasteleriapdm.Models.Cake;
import com.example.pasteleriapdm.Models.Client;
import com.example.pasteleriapdm.Models.Payment;
import com.example.pasteleriapdm.Models.Reservation;
import com.example.pasteleriapdm.Models.User;
import com.example.pasteleriapdm.R;
import com.example.pasteleriapdm.Utils.DatabaseHelper;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReservaDialogo extends DialogFragment {

    private static final String TAG = "ReservaDialogo";

    // Views
    private Spinner spinnerCliente, spinnerEstadoReserva;
    private TextView txtFechaEntrega, txtHoraEntrega;
    private EditText txtDireccionEntrega;
    private TextView btnSalir;
    private Button btnCrearReserva, btnMetodoPago;
    private Spinner spinnerPasteles;
    private EditText txtCantidad;
    private RecyclerView rvPastelesReserva;
    private TextView txtTotalReserva;
    private TextView lblTituloDialogoReserva;
    private Button btnAgregarPastel;
    private TextInputLayout tilNotasEspeciales;
    private EditText txtNotasEspeciales;

    // Data
    private Calendar calendar;
    private DatabaseHelper dbHelper;
    private String currentUserId;
    private String currentUserRole;
    private User currentUser;
    private Reservation currentReservation;
    private List<Client> clientsList = new ArrayList<>();
    private List<Cake> cakesList = new ArrayList<>();
    private Map<String, ReservationItem> selectedItems = new HashMap<>();
    private double totalAmount = 0.0;
    private ReservaItemsAdapter itemsAdapter;

    public interface ReservationCallback {
        void onReservationSaved(Reservation reservation);
        void onReservationDeleted(String reservationId);
    }

    private ReservationCallback callback;

    public void setCallback(ReservationCallback callback) {
        this.callback = callback;
    }

    public static class ReservationItem {
        public String cakeId;
        public String cakeName;
        public int quantity;
        public double unitPrice;
        public double totalPrice;

        public ReservationItem(String cakeId, String cakeName, int quantity, double unitPrice) {
            this.cakeId = cakeId;
            this.cakeName = cakeName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.totalPrice = quantity * unitPrice;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
            loadCurrentUser();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_reserva, container, false);
        initializeViews(view);
        setupListeners();

        dbHelper = DatabaseHelper.getInstance();
        setupRecyclerView();
        loadClients();
        loadCakes();

        if (currentReservation != null) {
            setupForEdit();
        }

        return view;
    }

    private void initializeViews(View view) {
        spinnerCliente = view.findViewById(R.id.spinnerCliente);
        spinnerEstadoReserva = view.findViewById(R.id.spinnerEstadoReserva);
        txtFechaEntrega = view.findViewById(R.id.txtFechaEntrega);
        txtHoraEntrega = view.findViewById(R.id.txtHoraEntrega);
        txtDireccionEntrega = view.findViewById(R.id.txtDireccionEntrega);
        btnSalir = view.findViewById(R.id.btnSalir);
        btnMetodoPago = view.findViewById(R.id.btnMetodoPago);
        btnCrearReserva = view.findViewById(R.id.btnCrearReserva);
        spinnerPasteles = view.findViewById(R.id.spinnerPasteles);
        txtCantidad = view.findViewById(R.id.txtCantidad);
        rvPastelesReserva = view.findViewById(R.id.rvPastelesReserva);
        txtTotalReserva = view.findViewById(R.id.txtTotalReserva);
        lblTituloDialogoReserva = view.findViewById(R.id.lblTituloDialogoReserva);
        btnAgregarPastel = view.findViewById(R.id.btnAgregarPastel);
        tilNotasEspeciales = view.findViewById(R.id.tilNotasEspeciales);
        txtNotasEspeciales = view.findViewById(R.id.txtNotasEspeciales);

        calendar = Calendar.getInstance();
    }

    private void setupListeners() {
        btnSalir.setOnClickListener(v -> dismiss());
        btnAgregarPastel.setOnClickListener(v -> addCakeToReservation());
        btnCrearReserva.setOnClickListener(v -> saveReservation());
        btnMetodoPago.setOnClickListener(v -> showPaymentDialog());
        setupDateTimePickers();
    }

    private void setupDateTimePickers() {
        txtFechaEntrega.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePicker = new DatePickerDialog(
                    getContext(),
                    (view1, year1, month1, dayOfMonth) -> {
                        calendar.set(year1, month1, dayOfMonth);
                        String fecha = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                        txtFechaEntrega.setText(fecha);
                    },
                    year, month, day
            );
            datePicker.getDatePicker().setMinDate(System.currentTimeMillis());
            datePicker.show();
        });

        txtHoraEntrega.setOnClickListener(v -> {
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePicker = new TimePickerDialog(
                    getContext(),
                    (view12, hourOfDay, minute1) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute1);
                        String hora = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1);
                        txtHoraEntrega.setText(hora);
                    },
                    hour, minute, true
            );
            timePicker.show();
        });
    }

    private void setupRecyclerView() {
        itemsAdapter = new ReservaItemsAdapter(getContext(), new ReservaItemsAdapter.OnItemActionListener() {
            @Override
            public void onRemoveItem(String cakeId) {
                selectedItems.remove(cakeId);
                calculateTotal();
                itemsAdapter.updateItems(selectedItems);
            }

            @Override
            public void onUpdateQuantity(String cakeId, int newQuantity) {
                ReservationItem item = selectedItems.get(cakeId);
                if (item != null) {
                    item.quantity = newQuantity;
                    item.totalPrice = item.quantity * item.unitPrice;
                    calculateTotal();
                    itemsAdapter.updateItems(selectedItems);
                }
            }
        });

        rvPastelesReserva.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPastelesReserva.setAdapter(itemsAdapter);
    }
    private void loadCurrentUser() {
        dbHelper.getUserById(currentUserId, new DatabaseHelper.DatabaseCallback<User>() {
            @Override
            public void onSuccess(User user) {
                currentUser = user;
                currentUserRole = user.getRole();
                setupRoleBasedUI();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error cargando usuario actual: " + error);
                Toast.makeText(getContext(), "Error cargando información del usuario", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRoleBasedUI() {
        List<String> estados = new ArrayList<>();

        if ("admin".equals(currentUserRole)) {
            estados.add("Pendiente");
            estados.add("Confirmada");
            estados.add("En producción");
            estados.add("Lista para entrega");
            estados.add("Entregada");
            estados.add("Cancelada");
        } else if ("seller".equals(currentUserRole)) {
            estados.add("Pendiente");
            estados.add("Confirmada");
            estados.add("Cancelada");
        } else if ("production".equals(currentUserRole)) {
            estados.add("En producción");
            estados.add("Lista para entrega");
            estados.add("Entregada");
        }

        ArrayAdapter<String> adapterEstados = new ArrayAdapter<>(getContext(), R.layout.spinner_personalizado, estados);
        spinnerEstadoReserva.setAdapter(adapterEstados);

        // Configurar bn de pago según rol y estado
        if (currentReservation != null && "entregada".equals(currentReservation.getStatus())) {
            btnMetodoPago.setVisibility(View.VISIBLE);
            btnMetodoPago.setText(currentReservation.getPayment() != null ?
                    "Modificar Pago" : "Agregar Pago");
        } else {
            btnMetodoPago.setVisibility(View.GONE);
        }
    }

    private void loadClients() {
        dbHelper.getAllClients(new DatabaseHelper.DatabaseCallback<List<Client>>() {
            @Override
            public void onSuccess(List<Client> clients) {
                clientsList = clients;
                List<String> clientNames = new ArrayList<>();
                clientNames.add("Seleccione un cliente");

                for (Client client : clients) {
                    clientNames.add(client.getName());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        getContext(),
                        R.layout.spinner_personalizado,
                        clientNames
                );
                spinnerCliente.setAdapter(adapter);

                // Si estamos editando, seleccionar el cliente correspondiente
                if (currentReservation != null) {
                    for (int i = 0; i < clients.size(); i++) {
                        if (clients.get(i).getName().equals(currentReservation.getClientId())) {
                            spinnerCliente.setSelection(i + 1); // +1 por el item "Seleccione..."
                            break;
                        }
                    }
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error cargando clientes: " + error);
                Toast.makeText(getContext(), "Error cargando clientes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCakes() {
        dbHelper.getAllCakes(new DatabaseHelper.DatabaseCallback<List<Cake>>() {
            @Override
            public void onSuccess(List<Cake> cakes) {
                cakesList = cakes;
                List<String> cakeNames = new ArrayList<>();
                cakeNames.add("Seleccione un pastel");

                for (Cake cake : cakes) {
                    cakeNames.add(cake.getName() + " - $" + String.format("%.0f", cake.getPrice()));
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        getContext(),
                        R.layout.spinner_personalizado,
                        cakeNames
                );
                spinnerPasteles.setAdapter(adapter);

                // Si estamos editando, cargar los items de la reserva
                if (currentReservation != null) {
                    loadReservationItems();
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error cargando pasteles: " + error);
                Toast.makeText(getContext(), "Error cargando pasteles", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadReservationItems() {
        if (currentReservation == null || currentReservation.getItems() == null) return;

        selectedItems.clear();
        for (Map.Entry<String, Object> entry : currentReservation.getItems().entrySet()) {
            String cakeId = entry.getKey();
            Object itemObj = entry.getValue();

            if (itemObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> itemData = (Map<String, Object>) itemObj;

                // Buscar el pastel en la lista para obtener el nombre
                Cake cake = findCakeById(cakeId);
                if (cake != null) {
                    // Obtener quantity - puede ser Long o Integer
                    int quantity = 0;
                    Object quantityObj = itemData.get("quantity");
                    if (quantityObj instanceof Long) {
                        quantity = ((Long) quantityObj).intValue();
                    } else if (quantityObj instanceof Integer) {
                        quantity = (Integer) quantityObj;
                    }

                    // Obtener unitPrice - puede ser Double o Long
                    double unitPrice = 0.0;
                    Object priceObj = itemData.get("unitPrice");
                    if (priceObj instanceof Double) {
                        unitPrice = (Double) priceObj;
                    } else if (priceObj instanceof Long) {
                        unitPrice = ((Long) priceObj).doubleValue();
                    }

                    // Si no hay precio unitario guardado, usar el precio actual del pastel
                    if (unitPrice == 0.0) {
                        unitPrice = cake.getPrice();
                    }

                    ReservationItem item = new ReservationItem(cakeId, cake.getName(), quantity, unitPrice);
                    selectedItems.put(cakeId, item);
                }
            }
        }

        calculateTotal();
        itemsAdapter.updateItems(selectedItems);
    }

    private Cake findCakeById(String cakeId) {
        for (Cake cake : cakesList) {
            if (cake.getId().equals(cakeId)) {
                return cake;
            }
        }
        return null;
    }

    private void addCakeToReservation() {
        int selectedPosition = spinnerPasteles.getSelectedItemPosition();
        String quantityStr = txtCantidad.getText().toString().trim();

        if (selectedPosition == 0) {
            Toast.makeText(getContext(), "Seleccione un pastel", Toast.LENGTH_SHORT).show();
            return;
        }

        if (quantityStr.isEmpty()) {
            Toast.makeText(getContext(), "Ingrese la cantidad", Toast.LENGTH_SHORT).show();
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) {
                Toast.makeText(getContext(), "La cantidad debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Ingrese una cantidad válida", Toast.LENGTH_SHORT).show();
            return;
        }

        Cake selectedCake = cakesList.get(selectedPosition - 1);

        // Si ya existe el pastel, actualizar cantidad
        if (selectedItems.containsKey(selectedCake.getId())) {
            ReservationItem existingItem = selectedItems.get(selectedCake.getId());
            existingItem.quantity += quantity;
            existingItem.totalPrice = existingItem.quantity * existingItem.unitPrice;
            Toast.makeText(getContext(), "Cantidad actualizada", Toast.LENGTH_SHORT).show();
        } else {
            ReservationItem item = new ReservationItem(
                    selectedCake.getId(),
                    selectedCake.getName(),
                    quantity,
                    selectedCake.getPrice()
            );
            selectedItems.put(selectedCake.getId(), item);
            Toast.makeText(getContext(), "Pastel agregado", Toast.LENGTH_SHORT).show();
        }

        spinnerPasteles.setSelection(0);
        txtCantidad.setText("");
        itemsAdapter.updateItems(selectedItems);
        calculateTotal();
    }

    private void calculateTotal() {
        totalAmount = 0.0;
        for (ReservationItem item : selectedItems.values()) {
            totalAmount += item.totalPrice;
        }
        txtTotalReserva.setText(String.format("$%,.0f COP", totalAmount));
    }

    private void saveReservation() {
        if (!validateReservation()) {
            return;
        }

        String clientName = getSelectedClientName();
        String createdByName = currentUser.getName();
        String deliveryAddress = txtDireccionEntrega.getText().toString().trim();
        String notes = txtNotasEspeciales.getText().toString().trim();
        String status = getSelectedStatus();
        long deliveryTimestamp = calendar.getTimeInMillis();

        // Crear map de items para Firebase con el formato correcto
        Map<String, Object> itemsMap = new HashMap<>();
        for (ReservationItem item : selectedItems.values()) {
            Map<String, Object> itemData = new HashMap<>();
            itemData.put("quantity", item.quantity);
            itemData.put("unitPrice", item.unitPrice);
            itemData.put("cakeName", item.cakeName);
            itemsMap.put(item.cakeId, itemData);
        }

        if (currentReservation == null) {
            createNewReservation(clientName, createdByName, deliveryAddress, notes, status, deliveryTimestamp, itemsMap);
        } else {
            updateExistingReservation(clientName, createdByName, deliveryAddress, notes, status, deliveryTimestamp, itemsMap);
        }
    }

    private void createNewReservation(String clientName, String createdByName, String deliveryAddress,
                                      String notes, String status, long deliveryTimestamp,
                                      Map<String, Object> itemsMap) {

        // Crear objeto Reservation usando el constructor
        Reservation reservation = new Reservation(clientName, createdByName, deliveryTimestamp, itemsMap, notes);

        // Configurar campos adicionales
        reservation.setStatus(status);
        reservation.setDeliveryAddress(deliveryAddress);
        reservation.setTotalAmount(totalAmount);
        reservation.setLastUpdatedBy(currentUser.getName());

        // Crear pago inicial pendiente
        Payment payment = new Payment();
        payment.setAmount(totalAmount);
        payment.setMethod("efectivo");
        payment.setStatus("pendiente");
        payment.setTimestamp(System.currentTimeMillis());
        reservation.setPayment(payment);

        dbHelper.createReservation(reservation, new DatabaseHelper.DatabaseCallback<Reservation>() {
            @Override
            public void onSuccess(Reservation createdReservation) {
                Toast.makeText(getContext(), "Reserva creada exitosamente", Toast.LENGTH_SHORT).show();
                if (callback != null) {
                    callback.onReservationSaved(createdReservation);
                }
                dismiss();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error creando reserva: " + error);
                Toast.makeText(getContext(), "Error creando reserva: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateExistingReservation(String clientName, String createdByName, String deliveryAddress,
                                           String notes, String status, long deliveryTimestamp,
                                           Map<String, Object> itemsMap) {

        // Actualizar campos de la reserva existente
        currentReservation.setClientId(clientName);
        currentReservation.setCreatedBy(createdByName);
        currentReservation.setDeliveryAt(deliveryTimestamp);
        currentReservation.setStatus(status);
        currentReservation.setItems(itemsMap);
        currentReservation.setNotes(notes);
        currentReservation.setDeliveryAddress(deliveryAddress);
        currentReservation.setTotalAmount(totalAmount);
        currentReservation.setLastUpdatedBy(currentUser.getName());

        // Actualizar el pago si existe
        if (currentReservation.getPayment() != null) {
            currentReservation.getPayment().setAmount(totalAmount);
        }

        dbHelper.updateReservation(currentReservation, new DatabaseHelper.DatabaseCallback<Reservation>() {
            @Override
            public void onSuccess(Reservation updatedReservation) {
                Toast.makeText(getContext(), "Reserva actualizada exitosamente", Toast.LENGTH_SHORT).show();
                if (callback != null) {
                    callback.onReservationSaved(updatedReservation);
                }
                dismiss();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error actualizando reserva: " + error);
                Toast.makeText(getContext(), "Error actualizando reserva: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateReservation() {
        if (selectedItems.isEmpty()) {
            Toast.makeText(getContext(), "Agregue al menos un pastel", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (spinnerCliente.getSelectedItemPosition() == 0) {
            Toast.makeText(getContext(), "Seleccione un cliente", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (txtFechaEntrega.getText().toString().equals("Seleccionar fecha")) {
            Toast.makeText(getContext(), "Seleccione una fecha de entrega", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (txtHoraEntrega.getText().toString().equals("Seleccionar hora")) {
            Toast.makeText(getContext(), "Seleccione una hora de entrega", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (txtDireccionEntrega.getText().toString().trim().isEmpty()) {
            Toast.makeText(getContext(), "Ingrese la dirección de entrega", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private String getSelectedClientName() {
        int position = spinnerCliente.getSelectedItemPosition();
        if (position > 0 && position <= clientsList.size()) {
            return clientsList.get(position - 1).getName();
        }
        return "";
    }

    private String getSelectedStatus() {
        String selectedStatus = (String) spinnerEstadoReserva.getSelectedItem();
        switch (selectedStatus) {
            case "Pendiente": return "pendiente";
            case "Confirmada": return "confirmada";
            case "En producción": return "en_produccion";
            case "Lista para entrega": return "lista_para_entrega";
            case "Entregada": return "entregada";
            case "Cancelada": return "cancelada";
            default: return "pendiente";
        }
    }

    private void showPaymentDialog() {
        if (currentReservation == null) {
            Toast.makeText(getContext(), "Guarde la reserva primero", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!"entregada".equals(currentReservation.getStatus())) {
            Toast.makeText(getContext(), "El pago solo está disponible cuando la reserva esté entregada", Toast.LENGTH_SHORT).show();
            return;
        }

        PagosDialog dialog = new PagosDialog();

        // Usar el método corregido con 3 parámetros
        dialog.setPaymentData(
                currentReservation.getTotalAmount(),
                currentReservation.getPayment(),
                new PagosDialog.PaymentCallback() {
                    @Override
                    public void onPaymentProcessed(Payment payment) {
                        // Actualizar el pago en la reserva
                        currentReservation.setPayment(payment);
                        currentReservation.setLastUpdatedBy(currentUser.getName());

                        dbHelper.updateReservation(currentReservation, new DatabaseHelper.DatabaseCallback<Reservation>() {
                            @Override
                            public void onSuccess(Reservation result) {
                                Toast.makeText(getContext(), "Pago actualizado", Toast.LENGTH_SHORT).show();
                                btnMetodoPago.setText("Modificar Pago");

                                // Actualizar la reserva actual con la información devuelta
                                currentReservation = result;
                            }

                            @Override
                            public void onError(String error) {
                                Toast.makeText(getContext(), "Error actualizando pago: " + error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
        );

        dialog.show(getParentFragmentManager(), "pagosDialogo");
    }

    public void setReservationToEdit(Reservation reservation) {
        this.currentReservation = reservation;
    }

    private void setupForEdit() {
        if (currentReservation == null) return;

        lblTituloDialogoReserva.setText("EDITAR RESERVA");
        btnCrearReserva.setText("Actualizar Reserva");

        // Cargar datos existentes
        calendar.setTimeInMillis(currentReservation.getDeliveryAt());
        txtFechaEntrega.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(new Date(currentReservation.getDeliveryAt())));
        txtHoraEntrega.setText(new SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(new Date(currentReservation.getDeliveryAt())));
        txtDireccionEntrega.setText(currentReservation.getDeliveryAddress());
        txtNotasEspeciales.setText(currentReservation.getNotes());
        totalAmount = currentReservation.getTotalAmount();
        txtTotalReserva.setText(String.format("$%,.0f COP", totalAmount));

        // Configurar estado
        String status = currentReservation.getStatus();
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerEstadoReserva.getAdapter();
        if (adapter != null) {
            for (int i = 0; i < adapter.getCount(); i++) {
                String item = adapter.getItem(i);
                if (statusMatches(status, item)) {
                    spinnerEstadoReserva.setSelection(i);
                    break;
                }
            }
        }
    }

    private boolean statusMatches(String status, String displayName) {
        switch (status) {
            case "pendiente": return "Pendiente".equals(displayName);
            case "confirmada": return "Confirmada".equals(displayName);
            case "en_produccion": return "En producción".equals(displayName);
            case "lista_para_entrega": return "Lista para entrega".equals(displayName);
            case "entregada": return "Entregada".equals(displayName);
            case "cancelada": return "Cancelada".equals(displayName);
            default: return false;
        }
    }
}