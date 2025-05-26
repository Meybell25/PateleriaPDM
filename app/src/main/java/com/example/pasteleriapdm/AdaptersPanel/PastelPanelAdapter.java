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

import com.example.pasteleriapdm.Adapters.ReservaAdapter;
import com.example.pasteleriapdm.R;

public class PastelPanelAdapter extends RecyclerView.Adapter<PastelPanelAdapter.ViewHolderPastelPanelAdapter> {
    private Context context;
    private FragmentManager fragmentManager;

    public PastelPanelAdapter(Context context, FragmentManager fragmentManager) {
        this.context = context;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public PastelPanelAdapter.ViewHolderPastelPanelAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_panel_pastel, parent, false);
        return new ViewHolderPastelPanelAdapter(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PastelPanelAdapter.ViewHolderPastelPanelAdapter holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public class ViewHolderPastelPanelAdapter extends RecyclerView.ViewHolder {
        ImageView imgPastel;
        TextView txtNombrePastel, txtCantidadPastelesPanel;
        public ViewHolderPastelPanelAdapter(@NonNull View itemView) {
            super(itemView);
            imgPastel = itemView.findViewById(R.id.imgPastel);
            txtNombrePastel = itemView.findViewById(R.id.txtNombrePastel);
            txtCantidadPastelesPanel = itemView.findViewById(R.id.txtCantidadPastelesPanel);
        }
    }
}
