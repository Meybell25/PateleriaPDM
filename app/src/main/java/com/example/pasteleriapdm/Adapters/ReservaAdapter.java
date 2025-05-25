package com.example.pasteleriapdm.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pasteleriapdm.Dialogs.ReservaDialogo;
import com.example.pasteleriapdm.Dialogs.UsuariosDialog;
import com.example.pasteleriapdm.R;

public class ReservaAdapter extends RecyclerView.Adapter<ReservaAdapter.ViewHolderReservaAdapter> {
    private Context context;
    private FragmentManager fragmentManager;

    public ReservaAdapter(Context context, FragmentManager fragmentManager) {
        this.context = context;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public ReservaAdapter.ViewHolderReservaAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reservas, parent, false);
        return new ViewHolderReservaAdapter(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservaAdapter.ViewHolderReservaAdapter holder, int position) {

        // Datos de ejemplo para probar visualmente
        holder.lblIdReserva.setText("ID Reserva: R000" + (position + 1));
        holder.lblEstadoReserva.setText("Estado: Pendiente");
        holder.lblNombreCliente.setText("Cliente: Carlos López");
        holder.lblNombreVendedor.setText("Vendedor: Ana Martínez");
        holder.lblFechaEntrega.setText("Fecha Entrega: 2025-05-30");
        holder.lblHoraEntrega.setText("Hora Entrega: 15:00");
        holder.lblDireccionEntrega.setText("Dirección: Calle Falsa 123");
        holder.lblNombresPasteles.setText("Pasteles: Chocolate, Vainilla");
        holder.lblPrioridadReserva.setText("Prioridad: Alta");
        holder.lblTotalReserva.setText("Total: $45.00");

        // Cambiar color por ejemplo si es alta prioridad
        if (holder.lblPrioridadReserva.getText().toString().contains("Alta")) {
            holder.lblPrioridadReserva.setTextColor(Color.parseColor("#D32F2F")); // rojo fuerte
        } else {
            holder.lblPrioridadReserva.setTextColor(Color.BLACK);
        }

        //EVENTO DEL BOTON EDITAR
        holder.btnEditarReserva.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReservaDialogo reservaDialogo = new ReservaDialogo();
                reservaDialogo.show(fragmentManager, "editar");
            }
        });

        //EVENTO DEL BOTON ELIMINAR (PERO SOLO EL DIALOGO)
        holder.btnEliminarReserva.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("¿Eliminar Pastel?")
                    .setMessage("¿Estás seguro de que deseas eliminar esta reserva ")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        Toast.makeText(context, "Elimindo", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public class ViewHolderReservaAdapter extends RecyclerView.ViewHolder {
        TextView lblIdReserva, lblEstadoReserva, lblNombreCliente, lblNombreVendedor,
                lblFechaEntrega, lblHoraEntrega, lblDireccionEntrega,
                lblNombresPasteles, lblPrioridadReserva, lblTotalReserva;
        ImageButton btnEditarReserva, btnEliminarReserva;
        public ViewHolderReservaAdapter(@NonNull View itemView) {
            super(itemView);
            lblIdReserva = itemView.findViewById(R.id.lblIdReserva);
            lblEstadoReserva = itemView.findViewById(R.id.lblEstadoReserva);
            lblNombreCliente = itemView.findViewById(R.id.lblNombreCliente);
            lblNombreVendedor = itemView.findViewById(R.id.lblNombreVendedor);
            lblFechaEntrega = itemView.findViewById(R.id.lblFechaEntrega);
            lblHoraEntrega = itemView.findViewById(R.id.lblHoraEntrega);
            lblDireccionEntrega = itemView.findViewById(R.id.lblDireccionEntrega);
            lblNombresPasteles = itemView.findViewById(R.id.lblNombresPasteles);
            lblPrioridadReserva = itemView.findViewById(R.id.lblPrioridadReserva);
            lblTotalReserva = itemView.findViewById(R.id.lblTotalReserva);
            btnEliminarReserva = itemView.findViewById(R.id.btnEliminarReserva);
            btnEditarReserva = itemView.findViewById(R.id.btnEditarReserva);
        }
    }
}
