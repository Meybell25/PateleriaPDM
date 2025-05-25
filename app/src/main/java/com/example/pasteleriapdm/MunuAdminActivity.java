package com.example.pasteleriapdm;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentContainerView;

import com.example.pasteleriapdm.FragmentsAdmin.GestionPastelesFragment;
import com.example.pasteleriapdm.FragmentsAdmin.GestionUsuariosFragment;
import com.example.pasteleriapdm.FragmentsAdmin.PanelAdministrativoFragment;
import com.example.pasteleriapdm.FragmentsAdmin.ReporteEstadisticasFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MunuAdminActivity extends AppCompatActivity {
    public FragmentContainerView fragmnetContainer;
    public BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_munu_admin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        AsociarElemntosXML();

        // Cargar el fragmento por defecto al iniciar
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmnetContainer, new GestionPastelesFragment())
                    .commit();
            // Establecer también el ítem seleccionado en la barra de navegación
            bottomNavigationView.setSelectedItemId(R.id.navGetionarPasteles);
        }

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                //consultar a que se le esta dando clic
                switch (item.getItemId()){
                    case R.id.navPanelAdminitrativo:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragmnetContainer, new PanelAdministrativoFragment()).commit();
                        break;
                    case R.id.navGetionarPasteles:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragmnetContainer, new GestionPastelesFragment()).commit();
                        break;
                    case R.id.navGestionarUsuarios:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragmnetContainer, new GestionUsuariosFragment()).commit();
                        break;
                    case R.id.navReportesEstadisticas:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragmnetContainer, new ReporteEstadisticasFragment()).commit();
                        break;
                    default:
                        System.out.println("Opcion no calida");
                }
                return true;
            }
        });
    }
    public void AsociarElemntosXML(){
        bottomNavigationView = findViewById(R.id.bottomnavigation);
    }
}