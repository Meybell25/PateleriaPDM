package com.example.pasteleriapdm.Dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.pasteleriapdm.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class UsuariosDialog extends DialogFragment {
    private TextInputEditText txtNombreUsuario, txtEmailUsuario, txtNotasUsuario;
    private Spinner spinnerRolUsuario, spinnerEstadoUsuario;
    private MaterialButton btnInsertarUsuario;
    private TextView btnSalir, lblTituloDialogoUsuario;


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
        View view = inflater.inflate(R.layout.dialog_usuarios, container, false);
        AsociarElementoXML(view);

        // Llenar el Spinner con los datos
        String[] estados = {"Activo", "Inactivo", "Bloqueado"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, estados);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstadoUsuario.setAdapter(adapter);
        spinnerEstadoUsuario.setSelection(0); // Selecciona "Activo" por defecto

        // Llenar el Spinner de roles
        String[] roles = {"admin", "seller", "production"};
        ArrayAdapter<String> adapterRol = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, roles);
        adapterRol.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRolUsuario.setAdapter(adapterRol);
        spinnerRolUsuario.setSelection(0); // Selecciona "admin" por defecto


        return view;
    }

    private void AsociarElementoXML(View view) {
        // TextViews
        lblTituloDialogoUsuario = view.findViewById(R.id.lblTituloDialogoUsuario);
        btnSalir = view.findViewById(R.id.btnSalir);


        // TextInputEditTexts
        txtNombreUsuario = view.findViewById(R.id.txtNombreUsuario);
        txtEmailUsuario = view.findViewById(R.id.txtEmailUsuario);
        txtNotasUsuario = view.findViewById(R.id.txtNotasUsuario);

        // Spinners
        spinnerRolUsuario = view.findViewById(R.id.spinnerRolUsuario);
        spinnerEstadoUsuario = view.findViewById(R.id.spinnerEstadoUsuario);

        // Botón
        btnInsertarUsuario = view.findViewById(R.id.btnInsertarUsuario);

    }

}
