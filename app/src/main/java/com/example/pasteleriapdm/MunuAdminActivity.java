package com.example.pasteleriapdm;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.pasteleriapdm.FragmentsAdmin.GestionClientesFragment;
import com.example.pasteleriapdm.FragmentsAdmin.GestionPastelesFragment;
import com.example.pasteleriapdm.FragmentsAdmin.GestionUsuariosFragment;
import com.example.pasteleriapdm.FragmentsAdmin.PanelAdministrativoFragment;
import com.example.pasteleriapdm.FragmentsAdmin.ReporteEstadisticasFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.example.pasteleriapdm.R;


public class MunuAdminActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_munu_admin);

        bottomNavigationView = findViewById(R.id.bottomnavigation);


        // Cargar fragmento por defecto
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmnetContainer, new PanelAdministrativoFragment())
                    .commit();
            bottomNavigationView.setSelectedItemId(R.id.navPanelAdministrativo);
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navPanelAdministrativo) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmnetContainer, new PanelAdministrativoFragment())
                        .commit();
                return true;
            }
            else if (id == R.id.navGestionarPasteles) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmnetContainer, new GestionPastelesFragment())
                        .commit();
                return true;
            }
            else if (id == R.id.navGestionarUsuarios) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmnetContainer, new GestionUsuariosFragment())
                        .commit();
                return true;
            }
            else if (id == R.id.navGestionarClientes) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmnetContainer, new GestionClientesFragment())
                        .commit();
                return true;
            }

            return false;
        });

    }
}
