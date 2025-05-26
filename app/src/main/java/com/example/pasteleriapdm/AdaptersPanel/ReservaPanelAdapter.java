package com.example.pasteleriapdm.AdaptersPanel;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pasteleriapdm.R;

public class ReservaPanelAdapter extends RecyclerView.Adapter<ReservaPanelAdapter.ViewHolderReservaPanelAdapter> {
    private Context context;
    private FragmentManager fragmentManager;

    public ReservaPanelAdapter(Context context, FragmentManager fragmentManager) {
        this.context = context;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public ReservaPanelAdapter.ViewHolderReservaPanelAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_panel_reservas, parent, false);
        return new ViewHolderReservaPanelAdapter(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservaPanelAdapter.ViewHolderReservaPanelAdapter holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public class ViewHolderReservaPanelAdapter extends RecyclerView.ViewHolder {
        ImageView image;
        TextView lblReserva, lblCantidaReseservasPanel;
        public ViewHolderReservaPanelAdapter(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imagen);
            lblReserva = itemView.findViewById(R.id.lblReserva);
            lblCantidaReseservasPanel = itemView.findViewById(R.id.lblCantidaReseservasPanel);
        }
    }
}
