package com.example.pasteleriapdm.Dialogs;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.pasteleriapdm.Models.Cake;
import com.example.pasteleriapdm.Models.User;
import com.example.pasteleriapdm.R;
import com.example.pasteleriapdm.Utils.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PastelesDialog extends DialogFragment {

    private static final String TAG = "PastelesDialog";
    private static final String ARG_CAKE = "cake";
    private static final String ARG_MODE = "mode";

    public static final String MODE_CREATE = "create";
    public static final String MODE_EDIT = "edit";

    // UI Elements
    private EditText txtPrecioPastel, txtPorcionesPastel, txtDescripcionPastel, txtNombrePastel;
    private TextView lblTituloDailogoPastel, btnSalir;
    private TextView lblFechaCreacion, lblUltimaActualizacion, lblCreadoPor;
    private Button btnInsertarPastel;
    private Spinner spinnerCategoria, spinnerTamano, spinnerStatus;
    private View cardInfoCreacion;
    private EditText txtUrlImagen;
    private ImageView imgPreview;
    private String imageUrlSeleccionada = "";

    // Variables para imagen
    private ActivityResultLauncher<Intent> imagenLauncher;
    private Uri imagenUriSeleccionada;
    private int selectedImageResource = R.drawable.ic_imagen1; // Imagen por defecto

    // Variables para el modelo
    private Cake cakeToEdit;
    private String currentMode;
    private DatabaseHelper databaseHelper;

    // Listener para comunicar con la actividad padre
    public interface OnCakeOperationListener {
        void onCakeCreated(Cake cake);
        void onCakeUpdated(Cake cake);
    }

    private OnCakeOperationListener listener;

    public static PastelesDialog newInstance(String mode, Cake cake) {
        PastelesDialog dialog = new PastelesDialog();
        Bundle args = new Bundle();
        args.putString(ARG_MODE, mode);
        if (cake != null) {
            args.putSerializable(ARG_CAKE, cake);
        }
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        databaseHelper = DatabaseHelper.getInstance();

        if (getArguments() != null) {
            currentMode = getArguments().getString(ARG_MODE, MODE_CREATE);
            cakeToEdit = (Cake) getArguments().getSerializable(ARG_CAKE);
        }
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
        View view = inflater.inflate(R.layout.dialog_pasteles, container, false);

        asociarElementosXML(view);
        configurarActivityResultLaunchers();
        configurarSpinners();
        configurarEventos();

        // Si estamos editando, cargar los datos y mostrar informacion adicional
        if (MODE_EDIT.equals(currentMode) && cakeToEdit != null) {
            cargarDatosParaEdicion();
            mostrarInformacionCreacion();
        }
        return view;
    }

    private void asociarElementosXML(View view) {
        txtNombrePastel = view.findViewById(R.id.txtNombrePastel);
        txtDescripcionPastel = view.findViewById(R.id.txtDescripcionPastel);
        txtPrecioPastel = view.findViewById(R.id.txtPrecioPastel);
        txtPorcionesPastel = view.findViewById(R.id.txtPorcionesPastel);
        spinnerCategoria = view.findViewById(R.id.spinnerCategoria);
        spinnerTamano = view.findViewById(R.id.spinnerTamano);
        spinnerStatus = view.findViewById(R.id.spinnerStatus);
        btnSalir = view.findViewById(R.id.btnSalir);
        btnInsertarPastel = view.findViewById(R.id.btnInsertarPastel);
        lblTituloDailogoPastel = view.findViewById(R.id.lblTituloDailogoPastel);

        // Elementos de imagen
        txtUrlImagen = view.findViewById(R.id.txtUrlImagen);
        imgPreview = view.findViewById(R.id.imgPreview);

        // Elementos de informacion de creacion
        cardInfoCreacion = view.findViewById(R.id.cardInfoCreacion);
        lblFechaCreacion = view.findViewById(R.id.lblFechaCreacion);
        lblUltimaActualizacion = view.findViewById(R.id.lblUltimaActualizacion);
        lblCreadoPor = view.findViewById(R.id.lblCreadoPor);

        // Configurar titulo y boton segun el modo
        if (MODE_EDIT.equals(currentMode)) {
            lblTituloDailogoPastel.setText("EDITAR PASTEL");
            btnInsertarPastel.setText("Actualizar Pastel");
        } else {
            lblTituloDailogoPastel.setText("CREAR PASTEL");
            btnInsertarPastel.setText("Crear Pastel");
        }
    }

    private void configurarSpinners() {
        // Configurar spinner de categorias
        List<String> categorias = Arrays.asList(
                "Chocolate", "Vainilla", "Frutas", "Cheesecake",
                "Especial", "Cumpleaños", "Bodas"
        );
        ArrayAdapter<String> categoriasAdapter = new ArrayAdapter<>(
                requireContext(), R.layout.spinner_personalizado, categorias);
        categoriasAdapter.setDropDownViewResource(R.layout.spinner_personalizado);
        spinnerCategoria.setAdapter(categoriasAdapter);

        // Configurar spinner de tamaños
        List<String> tamanos = Arrays.asList(
                "Pequeño (6-8 personas)",
                "Mediano (10-12 personas)",
                "Grande (15-20 personas)",
                "Extra Grande (25+ personas)"
        );
        ArrayAdapter<String> tamanosAdapter = new ArrayAdapter<>(
                requireContext(), R.layout.spinner_personalizado, tamanos);
        tamanosAdapter.setDropDownViewResource(R.layout.spinner_personalizado);
        spinnerTamano.setAdapter(tamanosAdapter);

        // Configurar spinner de status
        List<String> statusOptions = Arrays.asList("Activo", "Inactivo");
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                requireContext(), R.layout.spinner_personalizado, statusOptions);
        statusAdapter.setDropDownViewResource(R.layout.spinner_personalizado);
        spinnerStatus.setAdapter(statusAdapter);
    }

    private void configurarEventos() {
        btnSalir.setOnClickListener(v -> dismiss());
        btnInsertarPastel.setOnClickListener(v -> procesarPastel());

        // Cargar imagen automáticamente cuando se escriba una URL
        txtUrlImagen.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ACTUALIZAR imageUrlSeleccionada inmediatamente cuando cambia el texto
                String url = s.toString().trim();
                imageUrlSeleccionada = url;
            }

            @Override
            public void afterTextChanged(Editable s) {
                String url = s.toString().trim();
                if (esUrlValida(url)) {
                    // Pequeño delay para evitar múltiples cargas mientras el usuario escribe
                    txtUrlImagen.removeCallbacks(cargarImagenRunnable);
                    txtUrlImagen.postDelayed(cargarImagenRunnable, 1000); // 1 segundo de delay
                } else if (url.isEmpty()) {
                    // Si se borra la URL, limpiar la imagen
                    imgPreview.setImageResource(R.drawable.decoracion_pastel);
                    // imageUrlSeleccionada ya se actualizó en onTextChanged
                }
            }
        });
    }

    // Runnable para cargar imagen con delay
    private final Runnable cargarImagenRunnable = new Runnable() {
        @Override
        public void run() {
            cargarPreviewImagen();
        }
    };

    private void cargarPreviewImagen() {
        String url = txtUrlImagen.getText().toString().trim();

        if (url.isEmpty()) {
            imgPreview.setImageResource(R.drawable.decoracion_pastel);
            imageUrlSeleccionada = ""; // Asegurar que esté vacío
            return;
        }

        if (!esUrlValida(url)) {
            Toast.makeText(getContext(), "La URL no parece ser válida", Toast.LENGTH_SHORT).show();
            imgPreview.setImageResource(R.drawable.decoracion_pastel);
            return;
        }

        // Usar Glide para cargar la imagen
        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.ic_imagen1) // Imagen mientras carga
                .error(R.drawable.decoracion_pastel) // Imagen si hay error
                .into(imgPreview);
    }


    private boolean esUrlValida(String url) {
        return url.startsWith("http://") || url.startsWith("https://");
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
                            selectedImageResource = R.drawable.ic_pastel;
                        }
                    }
                }
        );
    }

    private void cargarDatosParaEdicion() {
        if (cakeToEdit == null) return;

        txtNombrePastel.setText(cakeToEdit.getName());
        txtDescripcionPastel.setText(cakeToEdit.getDescription());
        txtPrecioPastel.setText(String.valueOf(cakeToEdit.getPrice()));
        txtPorcionesPastel.setText(String.valueOf(cakeToEdit.getServings()));

        // Seleccionar categoria en el spinner
        String categoria = convertirCategoriaParaDisplay(cakeToEdit.getCategory());
        ArrayAdapter<String> categoriasAdapter = (ArrayAdapter<String>) spinnerCategoria.getAdapter();
        int categoriaPosition = categoriasAdapter.getPosition(categoria);
        if (categoriaPosition >= 0) {
            spinnerCategoria.setSelection(categoriaPosition);
        }

        // Seleccionar tamaño en el spinner
        String tamanoDisplay = convertirTamanoParaDisplay(cakeToEdit.getSize());
        ArrayAdapter<String> tamanosAdapter = (ArrayAdapter<String>) spinnerTamano.getAdapter();
        int tamanoPosition = tamanosAdapter.getPosition(tamanoDisplay);
        if (tamanoPosition >= 0) {
            spinnerTamano.setSelection(tamanoPosition);
        }

        // Seleccionar status en el spinner
        String statusDisplay = convertirStatusParaDisplay(cakeToEdit.getStatus());
        ArrayAdapter<String> statusAdapter = (ArrayAdapter<String>) spinnerStatus.getAdapter();
        int statusPosition = statusAdapter.getPosition(statusDisplay);
        if (statusPosition >= 0) {
            spinnerStatus.setSelection(statusPosition);
        }

        // Cargar imagen si existe
        if (cakeToEdit.getImageUrl() != null && !cakeToEdit.getImageUrl().isEmpty()) {
            txtUrlImagen.setText(cakeToEdit.getImageUrl());
            cargarImagenDesdeUrl(cakeToEdit.getImageUrl(), imgPreview);
            imageUrlSeleccionada = cakeToEdit.getImageUrl();
        }
    }

    private void cargarImagenDesdeUrl(String url, ImageView imageView) {
        if (url != null && !url.isEmpty() && esUrlValida(url)) {
            Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.ic_imagen1)
                    .error(R.drawable.decoracion_pastel)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.decoracion_pastel);
        }
    }

    private void mostrarInformacionCreacion() {
        if (cakeToEdit == null) return;

        cardInfoCreacion.setVisibility(View.VISIBLE);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        // Mostrar fecha de creación
        if (cakeToEdit.getCreatedAt() > 0) {
            String fechaCreacion = dateFormat.format(new Date(cakeToEdit.getCreatedAt()));
            lblFechaCreacion.setText("Creado: " + fechaCreacion);
        }

        // Mostrar fecha de actualización
        if (cakeToEdit.getUpdatedAt() > 0) {
            String fechaActualizacion = dateFormat.format(new Date(cakeToEdit.getUpdatedAt()));
            lblUltimaActualizacion.setText("Actualizado: " + fechaActualizacion);
        }

        // Mostrar creado por
        if (cakeToEdit.getCreatedBy() != null && !cakeToEdit.getCreatedBy().isEmpty()) {
            // Si createdBy contiene un ID (más de 20 caracteres), buscar el nombre
            if (cakeToEdit.getCreatedBy().length() > 20) {
                // Es un ID, buscar el nombre del usuario
                lblCreadoPor.setText("Creado por: Cargando...");
                DatabaseHelper.getInstance().getUserById(cakeToEdit.getCreatedBy(), new DatabaseHelper.DatabaseCallback<User>() {
                    @Override
                    public void onSuccess(User user) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                lblCreadoPor.setText("Creado por: " + user.getName());
                            });
                        }
                    }

                    @Override
                    public void onError(String error) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                lblCreadoPor.setText("Creado por: Usuario desconocido");
                            });
                        }
                    }
                });
            } else {
                // Ya es un nombre, mostrarlo directamente
                lblCreadoPor.setText("Creado por: " + cakeToEdit.getCreatedBy());
            }
        } else {
            lblCreadoPor.setText("Creado por: --");
        }
    }

    // Método helper para convertir tamaño de BD a display
    private String convertirTamanoParaDisplay(String dbSize) {
        switch (dbSize) {
            case Cake.SIZE_SMALL: return "Pequeño (6-8 personas)";
            case Cake.SIZE_MEDIUM: return "Mediano (10-12 personas)";
            case Cake.SIZE_LARGE: return "Grande (15-20 personas)";
            case Cake.SIZE_XLARGE: return "Extra Grande (25+ personas)";
            default: return "Mediano (10-12 personas)";
        }
    }

    // Método helper para convertir status de BD a display
    private String convertirStatusParaDisplay(String dbStatus) {
        switch (dbStatus) {
            case Cake.STATUS_ACTIVE: return "Activo";
            case Cake.STATUS_INACTIVE: return "Inactivo";
            default: return "Activo";
        }
    }

    private void procesarPastel() {
        if (!validarCampos()) {
            return;
        }

        // Crear o actualizar el pastel
        Cake cake;
        if (MODE_EDIT.equals(currentMode) && cakeToEdit != null) {
            cake = cakeToEdit;
            actualizarDatosPastel(cake);
            actualizarPastel(cake);
        } else {
            cake = crearNuevoPastel();
            guardarPastel(cake);
        }
    }

    private boolean validarCampos() {
        if (txtNombrePastel.getText().toString().trim().isEmpty()) {
            txtNombrePastel.setError("El nombre es requerido");
            txtNombrePastel.requestFocus();
            return false;
        }

        if (txtDescripcionPastel.getText().toString().trim().isEmpty()) {
            txtDescripcionPastel.setError("La descripción es requerida");
            txtDescripcionPastel.requestFocus();
            return false;
        }

        if (txtPrecioPastel.getText().toString().trim().isEmpty()) {
            txtPrecioPastel.setError("El precio es requerido");
            txtPrecioPastel.requestFocus();
            return false;
        }

        try {
            double precio = Double.parseDouble(txtPrecioPastel.getText().toString().trim());
            if (precio <= 0) {
                txtPrecioPastel.setError("El precio debe ser mayor a 0");
                txtPrecioPastel.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            txtPrecioPastel.setError("Ingrese un precio válido");
            txtPrecioPastel.requestFocus();
            return false;
        }

        if (txtPorcionesPastel.getText().toString().trim().isEmpty()) {
            txtPorcionesPastel.setError("Las porciones son requeridas");
            txtPorcionesPastel.requestFocus();
            return false;
        }

        try {
            int porciones = Integer.parseInt(txtPorcionesPastel.getText().toString().trim());
            if (porciones <= 0) {
                txtPorcionesPastel.setError("Las porciones deben ser mayor a 0");
                txtPorcionesPastel.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            txtPorcionesPastel.setError("Ingrese un número válido de porciones");
            txtPorcionesPastel.requestFocus();
            return false;
        }

        return true;
    }

    private Cake crearNuevoPastel() {
        String nombre = txtNombrePastel.getText().toString().trim();
        String descripcion = txtDescripcionPastel.getText().toString().trim();
        double precio = Double.parseDouble(txtPrecioPastel.getText().toString().trim());
        String categoria = convertirCategoriaDesdeDisplay(spinnerCategoria.getSelectedItem().toString());
        String tamano = convertirTamanoDesdeDisplay(spinnerTamano.getSelectedItem().toString());
        String status = convertirStatusDesdeDisplay(spinnerStatus.getSelectedItem().toString());
        int porciones = Integer.parseInt(txtPorcionesPastel.getText().toString().trim());

        String currentUserId = databaseHelper.getCurrentUserId();

        Cake cake = new Cake(nombre, descripcion, precio, categoria, tamano, currentUserId);
        cake.setServings(porciones);
        cake.setStatus(status);

        cake.setImageUrl(imageUrlSeleccionada.isEmpty() ? "" : imageUrlSeleccionada);

        return cake;
    }

    private void actualizarDatosPastel(Cake cake) {
        cake.setName(txtNombrePastel.getText().toString().trim());
        cake.setDescription(txtDescripcionPastel.getText().toString().trim());
        cake.setPrice(Double.parseDouble(txtPrecioPastel.getText().toString().trim()));
        cake.setCategory(convertirCategoriaDesdeDisplay(spinnerCategoria.getSelectedItem().toString()));
        cake.setSize(convertirTamanoDesdeDisplay(spinnerTamano.getSelectedItem().toString()));
        cake.setStatus(convertirStatusDesdeDisplay(spinnerStatus.getSelectedItem().toString()));
        cake.setServings(Integer.parseInt(txtPorcionesPastel.getText().toString().trim()));

        // Actualizar timestamp de modificación
        cake.setUpdatedAt(System.currentTimeMillis());

        // Actualizar imagen siempre con el valor actual del campo
        String urlActual = txtUrlImagen.getText().toString().trim();
        cake.setImageUrl(urlActual); // Usar directamente el valor del campo

        // Log para debugging
        Log.d(TAG, "Actualizando pastel con imagen URL: " + urlActual);
    }


    private void guardarPastel(Cake cake) {
        btnInsertarPastel.setEnabled(false);
        btnInsertarPastel.setText("Guardando...");

        databaseHelper.createCake(cake, new DatabaseHelper.DatabaseCallback<Cake>() {
            @Override
            public void onSuccess(Cake result) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Pastel creado exitosamente", Toast.LENGTH_SHORT).show();

                        if (listener != null) {
                            listener.onCakeCreated(result);
                        }

                        dismiss();
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Log.e(TAG, "Error creando pastel: " + error);
                        Toast.makeText(getContext(), "Error creando pastel: " + error, Toast.LENGTH_LONG).show();

                        btnInsertarPastel.setEnabled(true);
                        btnInsertarPastel.setText("Crear Pastel");
                    });
                }
            }
        });
    }

    private void actualizarPastel(Cake cake) {
        btnInsertarPastel.setEnabled(false);
        btnInsertarPastel.setText("Actualizando...");

        databaseHelper.updateCake(cake, new DatabaseHelper.DatabaseCallback<Cake>() {
            @Override
            public void onSuccess(Cake result) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Pastel actualizado exitosamente", Toast.LENGTH_SHORT).show();

                        if (listener != null) {
                            listener.onCakeUpdated(result);
                        }

                        dismiss();
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Log.e(TAG, "Error actualizando pastel: " + error);
                        Toast.makeText(getContext(), "Error actualizando pastel: " + error, Toast.LENGTH_LONG).show();

                        btnInsertarPastel.setEnabled(true);
                        btnInsertarPastel.setText("Actualizar Pastel");
                    });
                }
            }
        });
    }

    // Métodos para convertir entre valores de display y valores de BD
    private String convertirCategoriaDesdeDisplay(String displayCategory) {
        switch (displayCategory) {
            case "Chocolate": return Cake.CATEGORY_CHOCOLATE;
            case "Vainilla": return Cake.CATEGORY_VANILLA;
            case "Frutas": return Cake.CATEGORY_FRUIT;
            case "Cheesecake": return Cake.CATEGORY_CHEESECAKE;
            case "Especial": return Cake.CATEGORY_SPECIAL;
            case "Cumpleaños": return Cake.CATEGORY_BIRTHDAY;
            case "Bodas": return Cake.CATEGORY_WEDDING;
            default: return Cake.CATEGORY_SPECIAL;
        }
    }

    private String convertirCategoriaParaDisplay(String dbCategory) {
        switch (dbCategory) {
            case Cake.CATEGORY_CHOCOLATE: return "Chocolate";
            case Cake.CATEGORY_VANILLA: return "Vainilla";
            case Cake.CATEGORY_FRUIT: return "Frutas";
            case Cake.CATEGORY_CHEESECAKE: return "Cheesecake";
            case Cake.CATEGORY_SPECIAL: return "Especial";
            case Cake.CATEGORY_BIRTHDAY: return "Cumpleaños";
            case Cake.CATEGORY_WEDDING: return "Bodas";
            default: return "Especial";
        }
    }

    private String convertirTamanoDesdeDisplay(String displaySize) {
        switch (displaySize) {
            case "Pequeño (6-8 personas)": return Cake.SIZE_SMALL;
            case "Mediano (10-12 personas)": return Cake.SIZE_MEDIUM;
            case "Grande (15-20 personas)": return Cake.SIZE_LARGE;
            case "Extra Grande (25+ personas)": return Cake.SIZE_XLARGE;
            default: return Cake.SIZE_MEDIUM;
        }
    }

    private String convertirStatusDesdeDisplay(String displayStatus) {
        switch (displayStatus) {
            case "Activo": return Cake.STATUS_ACTIVE;
            case "Inactivo": return Cake.STATUS_INACTIVE;
            default: return Cake.STATUS_ACTIVE;
        }
    }

    public void setOnCakeOperationListener(OnCakeOperationListener listener) {
        this.listener = listener;
    }
}