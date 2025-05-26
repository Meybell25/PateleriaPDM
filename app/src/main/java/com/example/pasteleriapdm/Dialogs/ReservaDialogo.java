package com.example.pasteleriapdm.Dialogs;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ReservaDialogo extends DialogFragment {

    private Spinner spinnerCliente, spinnerEstadoReserva, spinnerPrioridad;
    private TextView txtFechaEntrega, txtHoraEntrega;
    private EditText txtDireccionEntrega;
    private TextView btnSalir;
    private Button btnCrearReserva, btnMetodoPago;

    private Calendar calendar;

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_reserva, container, false);
        AsociarElementoXML(view);
        btnSalir.setOnClickListener(v -> dismiss());

        //ACCION DE BTN METODO DE PAGO
        btnMetodoPago.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PagosDialog dialog = new PagosDialog();
                dialog.show(getParentFragmentManager(), "pagosDialogo");
            }
        });
        btnSalir.setOnClickListener(v -> dismiss());


        return view;
    }

    private void AsociarElementoXML(View view) {
        spinnerCliente = view.findViewById(R.id.spinnerCliente);
        spinnerEstadoReserva = view.findViewById(R.id.spinnerEstadoReserva);
        txtFechaEntrega = view.findViewById(R.id.txtFechaEntrega);
        txtHoraEntrega = view.findViewById(R.id.txtHoraEntrega);
        txtDireccionEntrega = view.findViewById(R.id.txtDireccionEntrega);
        spinnerPrioridad = view.findViewById(R.id.spinnerPrioridad);
        btnSalir = view.findViewById(R.id.btnSalir);
        btnMetodoPago = view.findViewById(R.id.btnMetodoPago);
        btnCrearReserva = view.findViewById(R.id.btnCrearReserva);

        calendar = Calendar.getInstance();

        // Datos locales
        List<String> clientes = Arrays.asList("Seleccione un cliente", "Juan Pérez", "María Gómez", "Carlos Ramírez");
        List<String> estados = Arrays.asList("Pendiente", "En preparación", "Entregado", "Cancelado");
        List<String> pioridad = Arrays.asList("baja", "normal", "alta", "urgente");

        // Adaptadores
        ArrayAdapter<String> adapterClientes = new ArrayAdapter<>(getContext(), R.layout.spinner_personalizado, clientes);
        adapterClientes.setDropDownViewResource(R.layout.spinner_personalizado);
        spinnerCliente.setAdapter(adapterClientes);

        ArrayAdapter<String> adapterEstados = new ArrayAdapter<>(getContext(), R.layout.spinner_personalizado, estados);
        adapterEstados.setDropDownViewResource(R.layout.spinner_personalizado);
        spinnerEstadoReserva.setAdapter(adapterEstados);

        ArrayAdapter<String> adapterPioridad = new ArrayAdapter<>(getContext(), R.layout.spinner_personalizado, pioridad);
        adapterPioridad.setDropDownViewResource(R.layout.spinner_personalizado);
        spinnerPrioridad.setAdapter(adapterPioridad);

        // Fecha
        txtFechaEntrega.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePicker = new DatePickerDialog(
                    getContext(),
                    (view1, year1, month1, dayOfMonth) -> {
                        String fecha = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                        txtFechaEntrega.setText(fecha);
                    },
                    year, month, day
            );
            datePicker.show();
        });

        // Hora
        txtHoraEntrega.setOnClickListener(v -> {
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePicker = new TimePickerDialog(
                    getContext(),
                    (view12, hourOfDay, minute1) -> {
                        String hora = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1);
                        txtHoraEntrega.setText(hora);
                    },
                    hour, minute, true
            );
            timePicker.show();
        });
    }
}
