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

public class UsuarioPaneleAdapter extends RecyclerView.Adapter<UsuarioPaneleAdapter.ViewHolderUsuarioPaneleAdapter> {
    private Context context;
    private FragmentManager fragmentManager;

    public UsuarioPaneleAdapter(Context context, FragmentManager fragmentManager) {
        this.context = context;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public UsuarioPaneleAdapter.ViewHolderUsuarioPaneleAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_panel_usuario, parent, false);
        return new ViewHolderUsuarioPaneleAdapter(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioPaneleAdapter.ViewHolderUsuarioPaneleAdapter holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public class ViewHolderUsuarioPaneleAdapter extends RecyclerView.ViewHolder {
        ImageView imgPastel;
        TextView lblNombre, lblCantidadUsuariosPanel;
        public ViewHolderUsuarioPaneleAdapter(@NonNull View itemView) {
            super(itemView);
            imgPastel = itemView.findViewById(R.id.imagen);
            lblNombre = itemView.findViewById(R.id.lblNombre);
            lblCantidadUsuariosPanel = itemView.findViewById(R.id.lblCantidadUsiariosPanel);
        }
    }
}
