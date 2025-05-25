package com.example.pasteleriapdm.FragmentsAdmin;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.pasteleriapdm.Adapters.GestionarClientesAdapter;
import com.example.pasteleriapdm.Adapters.GestionarPastelesAdapter;
import com.example.pasteleriapdm.Dialogs.ClientesDialog;
import com.example.pasteleriapdm.Dialogs.PastelesDialog;
import com.example.pasteleriapdm.R;


public class GestionClientesFragment extends Fragment {
    private Button btnAbrirDialogoClientes;
    private RecyclerView rvcClientes;



    public GestionClientesFragment() {
        // Required empty public constructor
    }


    public static GestionClientesFragment newInstance(String param1, String param2) {
        GestionClientesFragment fragment = new GestionClientesFragment();
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
        View view = inflater.inflate(R.layout.fragment_gestion_clientes, container, false);
        AsociarElementoXML(view);
        GestionarClientesAdapter gestionarClientesAdapter= new GestionarClientesAdapter(getContext(), getParentFragmentManager());
        rvcClientes.setLayoutManager(new LinearLayoutManager(getContext()));
        rvcClientes.setAdapter(gestionarClientesAdapter);
        btnAbrirDialogoClientes.setOnClickListener(new View.OnClickListener() {
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
        btnAbrirDialogoClientes = view.findViewById(R.id.btnAbrirDialogoClientes);
        rvcClientes = view.findViewById(R.id.rvcClientes);
    }
}