package com.example.pasteleriapdm.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pasteleriapdm.Dialogs.ClientesDialog;
import com.example.pasteleriapdm.Dialogs.PastelesDialog;
import com.example.pasteleriapdm.R;

public class GestionarClientesAdapter extends RecyclerView.Adapter<GestionarClientesAdapter.ViewHolderGestionarClientesAdapter> {
    private Context context;
    private FragmentManager fragmentManager;

    public GestionarClientesAdapter(Context context, FragmentManager fragmentManager) {
        this.context = context;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public GestionarClientesAdapter.ViewHolderGestionarClientesAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_clientes, parent, false);
        return new ViewHolderGestionarClientesAdapter(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GestionarClientesAdapter.ViewHolderGestionarClientesAdapter holder, int position) {

        // Simular datos de ejemplo
        holder.lblNombreCompleto.setText("María Elena Rodríguez García");
        holder.lblCorreo.setText("maria.rodriguez@gmail.com");
        holder.lblTelefono.setText("Tel: +503 6027 1616");
        holder.lblTelefonoAlternativo.setText("Tel Alt: +503 7990 2121");
        holder.tvDireccion.setText("Calle 45 # 12-34, Apt 201");
        holder.lblBarrio.setText("Barrio: El Centro");
        holder.lblCiudad.setText("Ciudad: San Miguel");
        holder.lblNotasAdicionales.setText("Notas: Cliente preferencial, compra mensualmente");
        holder.tvClientePreferencial.setText("⭐ PREFERENCIAL");

        // Simular datos de ejemplo
        holder.lblNombreCompleto.setText("María Elena Rodríguez García");
        holder.lblCorreo.setText("maria.rodriguez@gmail.com");
        holder.lblTelefono.setText("Tel: +503 6027 1616");
        holder.lblTelefonoAlternativo.setText("Tel Alt: +503 7990 2121");
        holder.tvDireccion.setText("Calle 45 # 12-34, Apt 201");
        holder.lblBarrio.setText("Barrio: El Centro");
        holder.lblCiudad.setText("Ciudad: San Miguel");
        holder.lblNotasAdicionales.setText("Notas: Cliente preferencial, compra mensualmente");
        holder.tvClientePreferencial.setText("⭐ PREFERENCIAL");

        // Spinner de ejemplo
        String[] estados = {"Activo", "Inactivo", "Pendiente"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(holder.itemView.getContext(), R.layout.spinner_personalizado, estados);
        adapter.setDropDownViewResource(R.layout.spinner_personalizado);
        holder.spinnerEstadoCliente.setAdapter(adapter);
        holder.spinnerEstadoCliente.setSelection(0); // Seleccionar "Activo"

        //EVENTO DEL BOTON EDITAR
        holder.btnEditarCliente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClientesDialog clientesDialog = new ClientesDialog();
                clientesDialog.show(fragmentManager, "editar");
            }
        });

        //EVENTO DEL BOTON ELIMINAR (PERO SOLO EL DIALOGO)
        holder.btnEliminarCliente.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("¿Eliminar Pastel?")
                    .setMessage("¿Estás seguro de que deseas eliminar este Cliente ")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        Toast.makeText(context, "Elimindo", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public class ViewHolderGestionarClientesAdapter extends RecyclerView.ViewHolder {

        // Declaración de variables
        public ImageView imgCliente;
        public TextView lblNombreCompleto, lblCorreo, lblTelefono, lblTelefonoAlternativo;
        public TextView tvDireccion, lblBarrio, lblCiudad, lblNotasAdicionales;
        public TextView tvClientePreferencial, labelEstado;
        public Spinner spinnerEstadoCliente;
        public ImageButton btnEditarCliente, btnEliminarCliente;

        public ViewHolderGestionarClientesAdapter(@NonNull View itemView) {
            super(itemView);

            // Asociación con los IDs del layout
            imgCliente = itemView.findViewById(R.id.imgCliente);
            lblNombreCompleto = itemView.findViewById(R.id.lblNombreCompleto);
            lblCorreo = itemView.findViewById(R.id.lblCorreo);
            lblTelefono = itemView.findViewById(R.id.lblTelefono);
            lblTelefonoAlternativo = itemView.findViewById(R.id.lblTelefonoAlternativo);
            tvDireccion = itemView.findViewById(R.id.tvDireccion);
            lblBarrio = itemView.findViewById(R.id.lblBarrio);
            lblCiudad = itemView.findViewById(R.id.lblCiudad);
            lblNotasAdicionales = itemView.findViewById(R.id.lblNotasAdicionales);
            tvClientePreferencial = itemView.findViewById(R.id.tvClientePreferencial);
            labelEstado = itemView.findViewById(R.id.labelEstado);
            spinnerEstadoCliente = itemView.findViewById(R.id.spinnerEstadoCliente);
            btnEditarCliente = itemView.findViewById(R.id.btnEditarCliente);
            btnEliminarCliente = itemView.findViewById(R.id.btnEliminarCliente);
        }
    }
}
