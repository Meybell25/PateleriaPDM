package com.example.pasteleriapdm.Adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pasteleriapdm.Dialogs.ReservaDialogo;
import com.example.pasteleriapdm.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReservaItemsAdapter extends RecyclerView.Adapter<ReservaItemsAdapter.ItemViewHolder> {

    private Context context;
    private List<ReservaDialogo.ReservationItem> items;
    private OnItemActionListener listener;

    public interface OnItemActionListener {
        void onRemoveItem(String cakeId);
        void onUpdateQuantity(String cakeId, int newQuantity);
    }

    public ReservaItemsAdapter(Context context, OnItemActionListener listener) {
        this.context = context;
        this.listener = listener;
        this.items = new ArrayList<>();
    }

    public void updateItems(Map<String, ReservaDialogo.ReservationItem> itemsMap) {
        this.items.clear();
        this.items.addAll(itemsMap.values());
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reseva_cake, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        ReservaDialogo.ReservationItem item = items.get(position);

        holder.txtCakeName.setText(item.cakeName);
        holder.txtUnitPrice.setText(String.format("$%.0f", item.unitPrice));
        holder.txtTotalPrice.setText(String.format("$%.0f", item.totalPrice));

        // Configurar cantidad sin trigger del TextWatcher
        holder.txtQuantity.removeTextChangedListener(holder.quantityWatcher);
        holder.txtQuantity.setText(String.valueOf(item.quantity));
        holder.txtQuantity.addTextChangedListener(holder.quantityWatcher);

        // Configurar botón eliminar
        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveItem(item.cakeId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView txtCakeName, txtUnitPrice, txtTotalPrice;
        EditText txtQuantity;
        ImageButton btnRemove;
        TextWatcher quantityWatcher;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            txtCakeName = itemView.findViewById(R.id.txtCakeName);
            txtUnitPrice = itemView.findViewById(R.id.txtUnitPrice);
            txtTotalPrice = itemView.findViewById(R.id.txtTotalPrice);
            txtQuantity = itemView.findViewById(R.id.txtQuantity);
            btnRemove = itemView.findViewById(R.id.btnRemove);

            // Crear TextWatcher para cantidad
            quantityWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.toString().trim().isEmpty()) return;

                    try {
                        int newQuantity = Integer.parseInt(s.toString().trim());
                        if (newQuantity > 0 && listener != null) {
                            ReservaDialogo.ReservationItem item = items.get(getAdapterPosition());
                            listener.onUpdateQuantity(item.cakeId, newQuantity);
                        }
                    } catch (NumberFormatException e) {
                        // Ignorar entrada inválida
                    }
                }
            };
        }
    }
}