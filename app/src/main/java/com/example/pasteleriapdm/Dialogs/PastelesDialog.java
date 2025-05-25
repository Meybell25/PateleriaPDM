package com.example.pasteleriapdm.Dialogs;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.pasteleriapdm.R;

public class PastelesDialog extends DialogFragment {
    private EditText  txtPrecioPastel, txtPorcionesPastel, txtDescripcionPastel, txtNombrePastel;
    private TextView lblSeleccionarImagen, lblTituloDailogoPastel, btnSalir;
    private Button  btnInsertarPastel;
    private ImageView imgPreview;
    private Spinner spinnerCategoria, spinnerTamano;

    //VARIABLES PARA SELECCIONAR LA IMAGEN
    private ActivityResultLauncher<Intent> imagenLauncher;
    private Uri imagenUriSeleccionada;


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
        View view = inflater.inflate(R.layout.dialog_pasteles, container, false);
        AsociarElementoXML(view);
        configurarActivityResultLaunchers();

        imgPreview.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            imagenLauncher.launch(intent);
        });

        btnSalir.setOnClickListener(v -> dismiss());

        return view;

    }
    private void AsociarElementoXML(View view) {
        txtNombrePastel = view.findViewById(R.id.txtNombrePastel);
        txtDescripcionPastel = view.findViewById(R.id.txtDescripcionPastel);
        txtPrecioPastel = view.findViewById(R.id.txtPrecioPastel);
        txtPorcionesPastel = view.findViewById(R.id.txtPorcionesPastel);
        lblSeleccionarImagen = view.findViewById(R.id.lblSeleccionarImagen);
        imgPreview = view.findViewById(R.id.imgPreview);
        spinnerCategoria = view.findViewById(R.id.spinnerCategoria);
        spinnerTamano = view.findViewById(R.id.spinnerTamano);
        btnSalir = view.findViewById(R.id.btnSalir);
        btnInsertarPastel = view.findViewById(R.id.btnInsertarPastel);
        lblTituloDailogoPastel = view.findViewById(R.id.lblTituloDailogoPastel);
    }

    private void configurarActivityResultLaunchers() {
        imagenLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        imagenUriSeleccionada = result.getData().getData();
                        if (imagenUriSeleccionada != null) {
                            imgPreview.setImageURI(imagenUriSeleccionada);
                            Toast.makeText(getContext(), "Imagen seleccionada correctamente", Toast.LENGTH_SHORT).show();
                            getActivity().getContentResolver().takePersistableUriPermission(imagenUriSeleccionada, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }
                    }
                });

    }


}
