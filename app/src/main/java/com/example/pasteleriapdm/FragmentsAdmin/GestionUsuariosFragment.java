package com.example.pasteleriapdm.FragmentsAdmin;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.pasteleriapdm.Adapters.GestionarClientesAdapter;
import com.example.pasteleriapdm.Adapters.GestionarUsuariosAdapter;
import com.example.pasteleriapdm.Dialogs.ClientesDialog;
import com.example.pasteleriapdm.R;


public class GestionUsuariosFragment extends Fragment {
    private Button btnAbrirDialogoUsuarios;
    private RecyclerView rvcUsuarios;

    public GestionUsuariosFragment() {
        // Required empty public constructor
    }


    public static GestionUsuariosFragment newInstance(String param1, String param2) {
        GestionUsuariosFragment fragment = new GestionUsuariosFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_gestion_usuarios, container, false);
        AsociarElementoXML(view);
        GestionarUsuariosAdapter gestionarUsuariosAdapter= new GestionarUsuariosAdapter(getContext(), getParentFragmentManager());
        rvcUsuarios.setLayoutManager(new LinearLayoutManager(getContext()));
        rvcUsuarios.setAdapter(gestionarUsuariosAdapter);
        btnAbrirDialogoUsuarios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClientesDialog dialog = new ClientesDialog();
                // dialog.setProductoListener(this::actualizar); // this debe implementar ProductoListener
                dialog.show(getParentFragmentManager(), "clientesDialogo");
            }
        });
        return view;
    }

    private  void AsociarElementoXML(View view){
        btnAbrirDialogoUsuarios = view.findViewById(R.id.btnAbrirDialogoUsuarios);
        rvcUsuarios = view.findViewById(R.id.rvcUsuarios);
    }
}