package com.example.pasteleriapdm.Dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.example.pasteleriapdm.R;

public class ClientesDialog extends DialogFragment {
    private EditText txtNombreCliente, txtEmailCliente, txtTelefonoCliente, txtTelefonoAlternativoCliente, txtDireccionClienteCliente, txtBarrioCliente, txtCiudadCliente;
    private TextView btnSalir, lblTituloDialogoCliente;
    private Button btnInsertarCliente;
    private Spinner spinnerEstadoCliente;

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
        View view = inflater.inflate(R.layout.dialog_clientes, container, false);
        AsociarElementoXML(view);

        // Llenar el Spinner con los datos
        String[] estados = {"Activo", "Inactivo", "Bloquedao"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                R.layout.spinner_personalizado, estados);
        adapter.setDropDownViewResource(R.layout.spinner_personalizado);
        spinnerEstadoCliente.setAdapter(adapter);
        spinnerEstadoCliente.setSelection(0); // Selecciona Activo por defecto

        btnSalir.setOnClickListener(v -> dismiss());
        return view;
    }
    private void AsociarElementoXML(View view) {
        lblTituloDialogoCliente = view.findViewById(R.id.lblTituloDialogoCliente);
        txtNombreCliente = view.findViewById(R.id.txtNombreCliente);
        txtDireccionClienteCliente = view.findViewById(R.id.txtDireccionClienteCliente);
        txtEmailCliente = view.findViewById(R.id.txtEmailCliente);
        txtBarrioCliente = view.findViewById(R.id.txtBarrioCliente);
        txtCiudadCliente = view.findViewById(R.id.txtCiudadCliente);
        txtTelefonoCliente = view.findViewById(R.id.txtTelefonoCliente);
        txtTelefonoAlternativoCliente = view.findViewById(R.id.txtTelefonoAlternativoCliente);
        btnInsertarCliente = view.findViewById(R.id.btnInsertarCliente);
        btnSalir = view.findViewById(R.id.btnSalir);
        spinnerEstadoCliente = view.findViewById(R.id.spinnerEstadoCliente);
    }
}
