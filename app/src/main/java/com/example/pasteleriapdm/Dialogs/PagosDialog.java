package com.example.pasteleriapdm.Dialogs;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.example.pasteleriapdm.Models.Payment;
import com.example.pasteleriapdm.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PagosDialog extends DialogFragment {

    private TextView txtPrecioPastel, txtMontoTotal;
    private EditText txtDescuento;
    private Spinner spinnerMetodoPago;
    private Button btnProcesarPago, btnCancelarPago;
    private TextView btnSalir;
    private Spinner spinnerEstadoPago;
    private EditText txtImpuesto;
    private TextInputLayout tilImpuesto;

    public interface PaymentCallback {
        void onPaymentProcessed(Payment payment);
    }

    private PaymentCallback callback;
    private double baseAmount;
    private Payment existingPayment; // Para editar pagos existentes

    // Método corregido para configurar datos del pago
    public void setPaymentData(double baseAmount, Payment existingPayment, PaymentCallback callback) {
        this.baseAmount = baseAmount;
        this.existingPayment = existingPayment;
        this.callback = callback;
    }

    // Método sobrecargado para nuevo pago
    public void setPaymentData(double baseAmount, PaymentCallback callback) {
        this.baseAmount = baseAmount;
        this.existingPayment = null;
        this.callback = callback;
    }

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

        setupSpinners();
        setupTextWatchers();
        setupClickListeners();

        // Si hay un pago existente, cargar sus datos
        if (existingPayment != null) {
            loadExistingPaymentData();
        } else {
            // Mostrar monto base para nuevo pago
            txtPrecioPastel.setText(String.format("$%,.0f COP", baseAmount));
            txtMontoTotal.setText(String.format("$%,.0f COP", baseAmount));
        }

        return view;
    }

    private void setupSpinners() {
        // Configurar spinner de metodos de pago
        ArrayAdapter<String> methodAdapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.spinner_personalizado,
                new String[]{"Efectivo", "Tarjeta", "Transferencia", "Depósito"}
        );
        spinnerMetodoPago.setAdapter(methodAdapter);

        // Configurar spinner de estado de pago
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.spinner_personalizado,
                new String[]{"Pendiente", "Pagado", "Cancelado"}
        );
        spinnerEstadoPago.setAdapter(statusAdapter);
    }

    private void setupTextWatchers() {
        // Listeners para calcular total cuando cambian descuento o impuesto
        txtDescuento.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                calculateFinalAmount();
            }
        });

        txtImpuesto.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                calculateFinalAmount();
            }
        });
    }

    private void setupClickListeners() {
        btnProcesarPago.setOnClickListener(v -> processPayment());
        btnCancelarPago.setOnClickListener(v -> dismiss());
        btnSalir.setOnClickListener(v -> dismiss());
    }

    private void loadExistingPaymentData() {
        // Mostrar datos del pago existente
        txtPrecioPastel.setText(String.format("$%,.0f COP", existingPayment.getAmount()));
        txtDescuento.setText(String.valueOf(existingPayment.getDiscount()));
        txtImpuesto.setText(String.valueOf(existingPayment.getTax()));
        txtMontoTotal.setText(String.format("$%,.0f COP", existingPayment.getFinalAmount()));

        // Seleccionar metodo de pago actual
        String currentMethod = existingPayment.getMethod();
        int methodPosition = getMethodPosition(currentMethod);
        if (methodPosition >= 0) {
            spinnerMetodoPago.setSelection(methodPosition);
        }

        // Seleccionar estado actual
        String currentStatus = existingPayment.getStatus();
        int statusPosition = getStatusPosition(currentStatus);
        if (statusPosition >= 0) {
            spinnerEstadoPago.setSelection(statusPosition);
        }
    }

    private int getMethodPosition(String method) {
        switch (method) {
            case Payment.METHOD_CASH: return 0; // "Efectivo"
            case Payment.METHOD_CARD: return 1; // "Tarjeta"
            case Payment.METHOD_TRANSFER: return 2; // "Transferencia"
            case Payment.METHOD_DEPOSIT: return 3; // "Depósito"
            default: return 0;
        }
    }

    private int getStatusPosition(String status) {
        switch (status) {
            case Payment.STATUS_PENDING: return 0; // "Pendiente"
            case Payment.STATUS_PAID: return 1; // "Pagado"
            case Payment.STATUS_CANCELLED: return 2; // "Cancelado"
            default: return 0;
        }
    }

    private void calculateFinalAmount() {
        try {
            double discount = txtDescuento.getText().toString().isEmpty() ? 0 :
                    Double.parseDouble(txtDescuento.getText().toString());
            double tax = txtImpuesto.getText().toString().isEmpty() ? 0 :
                    Double.parseDouble(txtImpuesto.getText().toString());

            double finalAmount = baseAmount - discount + tax;
            if (finalAmount < 0) {
                finalAmount = 0;
            }
            txtMontoTotal.setText(String.format("$%,.0f COP", finalAmount));
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Ingrese valores numéricos válidos", Toast.LENGTH_SHORT).show();
        }
    }

    private void processPayment() {
        try {
            double discount = txtDescuento.getText().toString().isEmpty() ? 0 :
                    Double.parseDouble(txtDescuento.getText().toString());
            double tax = txtImpuesto.getText().toString().isEmpty() ? 0 :
                    Double.parseDouble(txtImpuesto.getText().toString());

            if (discount < 0 || tax < 0) {
                Toast.makeText(getContext(), "Los valores no pueden ser negativos", Toast.LENGTH_SHORT).show();
                return;
            }

            String method = (String) spinnerMetodoPago.getSelectedItem();
            String status = (String) spinnerEstadoPago.getSelectedItem();

            // Crear o actualizar objeto Payment
            Payment payment;
            if (existingPayment != null) {
                payment = existingPayment; // Usar el pago existente
            } else {
                payment = new Payment(); // Crear nuevo pago
            }

            // Configurar datos del pago
            payment.setAmount(baseAmount);
            payment.setDiscount(discount);
            payment.setTax(tax);
            payment.setFinalAmount(baseAmount - discount + tax);

            // Mapear método de pago
            switch (method.toLowerCase()) {
                case "efectivo": payment.setMethod(Payment.METHOD_CASH); break;
                case "tarjeta": payment.setMethod(Payment.METHOD_CARD); break;
                case "transferencia": payment.setMethod(Payment.METHOD_TRANSFER); break;
                case "depósito": payment.setMethod(Payment.METHOD_DEPOSIT); break;
                default: payment.setMethod(Payment.METHOD_CASH); break;
            }

            // Mapear estado de pago
            switch (status.toLowerCase()) {
                case "pendiente": payment.setStatus(Payment.STATUS_PENDING); break;
                case "pagado": payment.setStatus(Payment.STATUS_PAID); break;
                case "cancelado": payment.setStatus(Payment.STATUS_CANCELLED); break;
                default: payment.setStatus(Payment.STATUS_PENDING); break;
            }

            payment.setTimestamp(System.currentTimeMillis());

            // Obtener UID del usuario actual
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                payment.setProcessedBy(user.getUid());
            }

            // Devolver el pago al callback
            if (callback != null) {
                callback.onPaymentProcessed(payment);
            }

            Toast.makeText(getContext(), "Pago procesado correctamente", Toast.LENGTH_SHORT).show();
            dismiss();
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Error en los valores ingresados", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error procesando el pago: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void AsociarElementoXML(View view) {
        txtPrecioPastel = view.findViewById(R.id.txtPrecioPastel);
        txtMontoTotal = view.findViewById(R.id.txtMontoTotal);
        txtDescuento = view.findViewById(R.id.txtDescuento);
        spinnerMetodoPago = view.findViewById(R.id.spinnerMetodoPago);
        btnProcesarPago = view.findViewById(R.id.btnProcesarPago);
        btnCancelarPago = view.findViewById(R.id.btnCancelarPago);
        btnSalir = view.findViewById(R.id.btnSalir);
        spinnerEstadoPago = view.findViewById(R.id.spinnerEstadoPago);
        txtImpuesto = view.findViewById(R.id.txtImpuesto);
        tilImpuesto = view.findViewById(R.id.tilImpuesto);
    }
}