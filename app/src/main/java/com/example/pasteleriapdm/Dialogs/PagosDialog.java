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

import com.example.pasteleriapdm.R;
import com.google.android.material.textfield.TextInputLayout;

public class PagosDialog extends DialogFragment {

    private TextView txtPrecioPastel, txtMontoTotal;
    private EditText txtDescuento;
    private Spinner spinnerMetodoPago;
    private Button btnProcesarPago, btnCancelarPago;
    private TextView btnSalir;

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
        View view = inflater.inflate(R.layout.dialog_pagos, container, false);
        AsociarElementoXML(view);

        // Spiner con metodos de pago
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.spinner_personalizado,
                new String[]{"Efectivo", "Tarjeta", "Transferencia", "deposito"}
        );
        adapter.setDropDownViewResource(R.layout.spinner_personalizado);
        spinnerMetodoPago.setAdapter(adapter);

        btnSalir.setOnClickListener(v -> dismiss());

        return view;
    }
    private void AsociarElementoXML(View view) {
        txtPrecioPastel = view.findViewById(R.id.txtPrecioPastel);
        txtMontoTotal = view.findViewById(R.id.txtMontoTotal);
        txtDescuento = view.findViewById(R.id.txtDescuento);
        spinnerMetodoPago = view.findViewById(R.id.spinnerMetodoPago);
        btnProcesarPago = view.findViewById(R.id.btnProcesarPago);
        btnCancelarPago = view.findViewById(R.id.btnCancelarPago);
        btnSalir = view.findViewById(R.id.btnSalir);
    }
}
