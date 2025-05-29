package com.example.pasteleriapdm.Dialogs;

import android.os.Bundle;
import android.text.TextUtils;
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

import com.example.pasteleriapdm.Models.User;
import com.example.pasteleriapdm.Utils.DatabaseHelper;
import com.example.pasteleriapdm.Models.Client;
import com.example.pasteleriapdm.R;
import com.google.firebase.auth.FirebaseAuth;

public class ClientesDialog extends DialogFragment {
    private EditText txtNombreCliente, txtTelefonoCliente, txtDireccionCliente;
    private TextView btnSalir, lblTituloDialogoCliente;
    private Button btnInsertarCliente;
    private Spinner spinnerEstadoCliente;

    private DatabaseHelper databaseHelper;
    private FirebaseAuth mAuth;

    // Variables para modo edición
    private boolean isEditMode = false;
    private Client clienteAEditar;

    // Interface para callback
    public interface ClienteDialogListener {
        void onClienteCreado();
        void onClienteEditado();
    }

    private ClienteDialogListener listener;

    public ClientesDialog() {
        // Constructor vacioo requerido
    }

    // Constructor para modo edición
    public static ClientesDialog newInstanceForEdit(Client cliente) {
        ClientesDialog dialog = new ClientesDialog();
        Bundle args = new Bundle();
        args.putString("clienteId", cliente.getId());
        args.putString("clienteNombre", cliente.getName());
        args.putString("clienteTelefono", cliente.getPhone());
        args.putString("clienteDireccion", cliente.getAddress());
        args.putString("clienteEstado", cliente.getStatus());

        // CRÍTICO: Pasar los campos de auditoría
        args.putString("clienteCreatedBy", cliente.getCreatedBy());
        args.putLong("clienteCreatedAt", cliente.getCreatedAt());
        args.putLong("clienteUpdatedAt", cliente.getUpdatedAt());

        // Campos opcionales de estadísticas
        args.putInt("clienteTotalOrders", cliente.getTotalOrders());
        args.putDouble("clienteTotalSpent", cliente.getTotalSpent());
        args.putLong("clienteLastOrderDate", cliente.getLastOrderDate());
        args.putBoolean("clienteIsPreferred", cliente.isPreferredClient());

        args.putBoolean("isEditMode", true);
        dialog.setArguments(args);
        return dialog;
    }

    public void setClienteDialogListener(ClienteDialogListener listener) {
        this.listener = listener;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            // Evitar que se cierre al tocar fuera
            getDialog().setCanceledOnTouchOutside(false);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_clientes, container, false);


        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        databaseHelper = DatabaseHelper.getInstance();

        AsociarElementoXML(view);
        configurarSpinner();
        verificarModoEdicion();
        configurarEventos();

        return view;
    }

    private void AsociarElementoXML(View view) {
        lblTituloDialogoCliente = view.findViewById(R.id.lblTituloDialogoCliente);
        txtNombreCliente = view.findViewById(R.id.txtNombreCliente);
        txtDireccionCliente = view.findViewById(R.id.txtDireccionCliente);
        txtTelefonoCliente = view.findViewById(R.id.txtTelefonoCliente);
        btnInsertarCliente = view.findViewById(R.id.btnInsertarCliente);
        btnSalir = view.findViewById(R.id.btnSalir);
        spinnerEstadoCliente = view.findViewById(R.id.spinnerEstadoCliente);
    }

    private void configurarSpinner() {
        String[] estados = {"Activo", "Inactivo", "Bloqueado"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                R.layout.spinner_personalizado, estados);
        adapter.setDropDownViewResource(R.layout.spinner_personalizado);
        spinnerEstadoCliente.setAdapter(adapter);
        spinnerEstadoCliente.setSelection(0); // Selecciona activo por defecto
    }

    private void verificarModoEdicion() {
        if (getArguments() != null) {
            isEditMode = getArguments().getBoolean("isEditMode", false);

            if (isEditMode) {
                lblTituloDialogoCliente.setText("EDITAR CLIENTE");
                btnInsertarCliente.setText("Actualizar Cliente");

                // Crear objeto cliente con TODOS los datos
                clienteAEditar = new Client();
                clienteAEditar.setId(getArguments().getString("clienteId"));
                clienteAEditar.setName(getArguments().getString("clienteNombre"));
                clienteAEditar.setPhone(getArguments().getString("clienteTelefono"));
                clienteAEditar.setAddress(getArguments().getString("clienteDireccion"));
                clienteAEditar.setStatus(getArguments().getString("clienteEstado"));

                // CRÍTICO: Asignar los campos de auditoría
                clienteAEditar.setCreatedBy(getArguments().getString("clienteCreatedBy"));
                clienteAEditar.setCreatedAt(getArguments().getLong("clienteCreatedAt", 0));
                clienteAEditar.setUpdatedAt(getArguments().getLong("clienteUpdatedAt", 0));

                // Campos opcionales de estadísticas
                clienteAEditar.setTotalOrders(getArguments().getInt("clienteTotalOrders", 0));
                clienteAEditar.setTotalSpent(getArguments().getDouble("clienteTotalSpent", 0.0));
                clienteAEditar.setLastOrderDate(getArguments().getLong("clienteLastOrderDate", 0));
                clienteAEditar.setPreferredClient(getArguments().getBoolean("clienteIsPreferred", false));

                // Debug log para verificar
                Log.d("ClientesDialog", "Cliente para editar cargado - ID: " + clienteAEditar.getId() +
                        ", CreatedBy: " + clienteAEditar.getCreatedBy() +
                        ", CreatedAt: " + clienteAEditar.getCreatedAt());

                // Llenar campos con datos existentes
                llenarCamposParaEdicion();
            }
        }
    }

    private void llenarCamposParaEdicion() {
        if (clienteAEditar != null) {
            txtNombreCliente.setText(clienteAEditar.getName());
            txtTelefonoCliente.setText(clienteAEditar.getPhone());
            txtDireccionCliente.setText(clienteAEditar.getAddress() != null ? clienteAEditar.getAddress() : "");

            // Configurar spinner según el estado
            String estado = clienteAEditar.getStatus();
            int posicion = 0; // Activo por defecto
            if (Client.STATUS_INACTIVE.equals(estado)) {
                posicion = 1; // Inactivo
            } else if (Client.STATUS_BLOCKED.equals(estado)) {
                posicion = 2; // Bloqueado
            }
            spinnerEstadoCliente.setSelection(posicion);
        }
    }

    private void configurarEventos() {
        btnSalir.setOnClickListener(v -> dismiss());

        btnInsertarCliente.setOnClickListener(v -> {
            if (isEditMode) {
                actualizarCliente();
            } else {
                crearCliente();
            }
        });
    }

    private void crearCliente() {
        if (!validarCampos()) {
            return;
        }

        String nombre = txtNombreCliente.getText().toString().trim();
        String telefono = txtTelefonoCliente.getText().toString().trim();
        String direccion = txtDireccionCliente.getText().toString().trim();
        String estadoSeleccionado = spinnerEstadoCliente.getSelectedItem().toString();

        String estado = convertirEstadoAConstante(estadoSeleccionado);
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";

        if (TextUtils.isEmpty(userId)) {
            Toast.makeText(getContext(), "Error: Usuario no autenticado", Toast.LENGTH_LONG).show();
            return;
        }

        Client nuevoCliente = new Client(nombre, telefono, direccion, userId);
        nuevoCliente.setStatus(estado);

        // Mostrar progreso
        btnInsertarCliente.setEnabled(false);
        btnInsertarCliente.setText("Creando...");

        databaseHelper.createClient(nuevoCliente, new DatabaseHelper.DatabaseCallback<Client>() {
            @Override
            public void onSuccess(Client result) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Cliente creado exitosamente", Toast.LENGTH_SHORT).show();
                        if (listener != null) {
                            listener.onClienteCreado();
                        }
                        dismiss();
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_LONG).show();
                        btnInsertarCliente.setEnabled(true);
                        btnInsertarCliente.setText("Registrar Cliente");
                    });
                }
            }
        });
    }

    private void actualizarCliente() {
        if (!validarCampos()) {
            return;
        }

        // Obtener datos actualizados
        clienteAEditar.setName(txtNombreCliente.getText().toString().trim());
        clienteAEditar.setPhone(txtTelefonoCliente.getText().toString().trim());
        clienteAEditar.setAddress(txtDireccionCliente.getText().toString().trim());
        clienteAEditar.setStatus(convertirEstadoAConstante(spinnerEstadoCliente.getSelectedItem().toString()));
        clienteAEditar.setUpdatedAt(System.currentTimeMillis());

        // Mostrar progreso
        btnInsertarCliente.setEnabled(false);
        btnInsertarCliente.setText("Actualizando...");

        // Actualizar en base de datos - La verificacion de permisos se hace en DatabaseHelper
        databaseHelper.updateClient(clienteAEditar, new DatabaseHelper.DatabaseCallback<Client>() {
            @Override
            public void onSuccess(Client result) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Cliente actualizado exitosamente", Toast.LENGTH_SHORT).show();
                        if (listener != null) {
                            listener.onClienteEditado();
                        }
                        dismiss();
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        btnInsertarCliente.setEnabled(true);
                        btnInsertarCliente.setText("Actualizar Cliente");
                        Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    private boolean validarCampos() {
        String nombre = txtNombreCliente.getText().toString().trim();
        String telefono = txtTelefonoCliente.getText().toString().trim();
        String direccion = txtDireccionCliente.getText().toString().trim();

        // Limpiar errores previos
        txtNombreCliente.setError(null);
        txtTelefonoCliente.setError(null);
        txtDireccionCliente.setError(null);

        boolean esValido = true;

        if (TextUtils.isEmpty(nombre)) {
            txtNombreCliente.setError("El nombre es requerido");
            if (esValido) txtNombreCliente.requestFocus();
            esValido = false;
        } else if (nombre.length() < 3) {
            txtNombreCliente.setError("El nombre debe tener al menos 3 caracteres");
            if (esValido) txtNombreCliente.requestFocus();
            esValido = false;
        } else if (!nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) {
            txtNombreCliente.setError("El nombre solo puede contener letras");
            if (esValido) txtNombreCliente.requestFocus();
            esValido = false;
        }

        if (TextUtils.isEmpty(telefono)) {
            txtTelefonoCliente.setError("El teléfono es requerido");
            if (esValido) txtTelefonoCliente.requestFocus();
            esValido = false;
        } else if (telefono.length() < 8) {
            txtTelefonoCliente.setError("El teléfono debe tener al menos 8 dígitos");
            if (esValido) txtTelefonoCliente.requestFocus();
            esValido = false;
        } else if (!telefono.matches("^[0-9+\\-\\s]+$")) {
            txtTelefonoCliente.setError("El teléfono solo puede contener números");
            if (esValido) txtTelefonoCliente.requestFocus();
            esValido = false;
        }

        // La dirección es opcional, pero si se proporciona debe tener al menos 5 caracteres
        if (!TextUtils.isEmpty(direccion) && direccion.length() < 5) {
            txtDireccionCliente.setError("La dirección debe tener al menos 5 caracteres");
            if (esValido) txtDireccionCliente.requestFocus();
            esValido = false;
        }

        return esValido;
    }
    private void limpiarCampos() {
        txtNombreCliente.setText("");
        txtTelefonoCliente.setText("");
        txtDireccionCliente.setText("");
        spinnerEstadoCliente.setSelection(0);

        // Limpiar errores
        txtNombreCliente.setError(null);
        txtTelefonoCliente.setError(null);
        txtDireccionCliente.setError(null);
    }


    private String convertirEstadoAConstante(String estadoSpinner) {
        switch (estadoSpinner) {
            case "Activo":
                return Client.STATUS_ACTIVE;
            case "Inactivo":
                return Client.STATUS_INACTIVE;
            case "Bloqueado":
                return Client.STATUS_BLOCKED;
            default:
                return Client.STATUS_ACTIVE;
        }
    }
}