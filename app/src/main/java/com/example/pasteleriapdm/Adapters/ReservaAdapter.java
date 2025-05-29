package com.example.pasteleriapdm.Adapters;

import android.app.AlertDialog;
import android.content.Context;
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
import com.example.pasteleriapdm.Models.Reservation;
import com.example.pasteleriapdm.R;
import com.example.pasteleriapdm.Utils.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReservaAdapter extends RecyclerView.Adapter<ReservaAdapter.ViewHolderReservaAdapter> {
    private Context context;
    private FragmentManager fragmentManager;
    private String currentUserRole;
    private List<Reservation> reservations = new ArrayList<>();

    // 🔧 INTERFACE PARA COMUNICAR CAMBIOS AL FRAGMENT
    public interface OnReservationChangeListener {
        void onReservationChanged();
    }

    private OnReservationChangeListener changeListener;

    public ReservaAdapter(Context context, FragmentManager fragmentManager, String currentUserRole) {
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.currentUserRole = currentUserRole;
    }

    // 🔧 METODO PARA ESTABLECER EL LISTENER
    public void setOnReservationChangeListener(OnReservationChangeListener listener) {
        this.changeListener = listener;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReservaAdapter.ViewHolderReservaAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reservas, parent, false);
        return new ViewHolderReservaAdapter(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservaAdapter.ViewHolderReservaAdapter holder, int position) {
        Reservation reservation = reservations.get(position);

        // Configurar datos de la reserva
        holder.lblIdReserva.setText("Reserva #" + reservation.getId());
        holder.lblEstadoReserva.setText(reservation.getStatus().toUpperCase());
        holder.lblNombreCliente.setText("Cliente: " + reservation.getClientId());
        holder.lblNombreVendedor.setText("Vendedor: " + reservation.getCreatedBy());

        // Formatear fechas
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        String deliveryDate = dateFormat.format(new Date(reservation.getDeliveryAt()));
        String deliveryTime = timeFormat.format(new Date(reservation.getDeliveryAt()));
        holder.lblFechaEntrega.setText("Entrega: " + deliveryDate + " - " + deliveryTime);

        holder.lblDireccionEntrega.setText(reservation.getDeliveryAddress());

        // Mostrar pasteles - corregir el acceso a los items
        StringBuilder cakesText = new StringBuilder();
        if (reservation.getItems() != null) {
            for (Map.Entry<String, Object> entry : reservation.getItems().entrySet()) {
                if (entry.getValue() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> itemData = (Map<String, Object>) entry.getValue();

                    String cakeName = (String) itemData.get("cakeName");
                    Object quantityObj = itemData.get("quantity");

                    int quantity = 0;
                    if (quantityObj instanceof Long) {
                        quantity = ((Long) quantityObj).intValue();
                    } else if (quantityObj instanceof Integer) {
                        quantity = (Integer) quantityObj;
                    }

                    if (cakeName != null) {
                        cakesText.append(cakeName).append(" x").append(quantity).append(", ");
                    }
                }
            }

            if (cakesText.length() > 0) {
                cakesText.setLength(cakesText.length() - 2); // Eliminar última coma
            }
        }

        holder.lblNombresPasteles.setText(cakesText.toString());
        holder.lblTotalReserva.setText(reservation.getFormattedTotalAmount());

        // Configurar color segun estado
        switch (reservation.getStatus()) {
            case "pendiente":
                holder.lblEstadoReserva.setBackgroundResource(R.drawable.bg_status_pending);
                break;
            case "confirmada":
                holder.lblEstadoReserva.setBackgroundResource(R.drawable.bg_status_confirmed);
                break;
            case "en_produccion":
                holder.lblEstadoReserva.setBackgroundResource(R.drawable.bg_status_production);
                break;
            case "lista_para_entrega":
                holder.lblEstadoReserva.setBackgroundResource(R.drawable.bg_status_ready);
                break;
            case "entregada":
                holder.lblEstadoReserva.setBackgroundResource(R.drawable.bg_status_delivered);
                break;
            case "cancelada":
                holder.lblEstadoReserva.setBackgroundResource(R.drawable.bg_status_cancelled);
                break;
        }

        // Configurar botones según permisos
        setupButtons(holder, reservation);
    }

    private void setupButtons(ViewHolderReservaAdapter holder, Reservation reservation) {
        // Por defecto ocultar botones
        holder.btnEditarReserva.setVisibility(View.GONE);
        holder.btnEliminarReserva.setVisibility(View.GONE);

        // Verificar permisos para editar
        boolean canEdit = false;
        if ("admin".equals(currentUserRole)) {
            canEdit = true;
        } else if ("seller".equals(currentUserRole) && "pendiente".equals(reservation.getStatus())) {
            canEdit = true;
        } else if ("production".equals(currentUserRole) &&
                ("confirmada".equals(reservation.getStatus()) ||
                        "en_produccion".equals(reservation.getStatus()) ||
                        "lista_para_entrega".equals(reservation.getStatus()))) {
            canEdit = true;
        }

        if (canEdit) {
            holder.btnEditarReserva.setVisibility(View.VISIBLE);
            holder.btnEditarReserva.setOnClickListener(v -> {
                ReservaDialogo dialog = new ReservaDialogo();
                dialog.setReservationToEdit(reservation);

                // 🔧 CONFIGURAR CALLBACK PARA ACTUALIZAR LA LISTA DESPUÉS DE EDITAR
                dialog.setCallback(new ReservaDialogo.ReservationCallback() {
                    @Override
                    public void onReservationSaved(Reservation reservation) {
                        // Notificar al fragment que hay cambios
                        if (changeListener != null) {
                            changeListener.onReservationChanged();
                        }
                    }

                    @Override
                    public void onReservationDeleted(String reservationId) {
                        // Notificar al fragment que hay cambios
                        if (changeListener != null) {
                            changeListener.onReservationChanged();
                        }
                    }
                });

                dialog.show(fragmentManager, "editar");
            });
        }


        // Solo mostrar si es admin o seller con reserva cancelada
        if (("admin".equals(currentUserRole) ||
                ("seller".equals(currentUserRole))) &&
                "cancelada".equals(reservation.getStatus())) {

            holder.btnEliminarReserva.setVisibility(View.VISIBLE);
            holder.btnEliminarReserva.setOnClickListener(v -> showDeleteDialog(reservation));
        }
    }

    private void showDeleteDialog(Reservation reservation) {
        new AlertDialog.Builder(context)
                .setTitle("Eliminar Reserva")
                .setMessage("¿Está seguro de eliminar esta reserva? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    DatabaseHelper dbHelper = DatabaseHelper.getInstance();
                    dbHelper.deleteReservation(reservation.getId(), new DatabaseHelper.DatabaseCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Toast.makeText(context, "Reserva eliminada", Toast.LENGTH_SHORT).show();
                            // Actualizar lista
                            int position = reservations.indexOf(reservation);
                            if (position != -1) {
                                reservations.remove(position);
                                notifyItemRemoved(position);
                            }


                            if (changeListener != null) {
                                changeListener.onReservationChanged();
                            }
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(context, "Error eliminando reserva", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return reservations.size();
    }

    public class ViewHolderReservaAdapter extends RecyclerView.ViewHolder {
        TextView lblIdReserva, lblEstadoReserva, lblNombreCliente, lblNombreVendedor,
                lblFechaEntrega, lblDireccionEntrega, lblNombresPasteles, lblTotalReserva;
        ImageButton btnEditarReserva, btnEliminarReserva;

        public ViewHolderReservaAdapter(@NonNull View itemView) {
            super(itemView);
            lblIdReserva = itemView.findViewById(R.id.lblIdReserva);
            lblEstadoReserva = itemView.findViewById(R.id.lblEstadoReserva);
            lblNombreCliente = itemView.findViewById(R.id.lblClienteId);
            lblNombreVendedor = itemView.findViewById(R.id.lblCreador);
            lblFechaEntrega = itemView.findViewById(R.id.lblFechaHoraEntrega);
            lblDireccionEntrega = itemView.findViewById(R.id.lblDireccionEntrega);
            lblNombresPasteles = itemView.findViewById(R.id.lblDetalleItems);
            lblTotalReserva = itemView.findViewById(R.id.lblTotalReserva);
            btnEliminarReserva = itemView.findViewById(R.id.btnEliminarReserva);
            btnEditarReserva = itemView.findViewById(R.id.btnEditarReserva);
        }
    }
}