package com.example.pasteleriapdm;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pasteleriapdm.FragmentsAdmin.GestionClientesFragment;
import com.example.pasteleriapdm.FragmentsAdmin.PanelAdministrativoFragment;
import com.example.pasteleriapdm.FragmentsAdmin.ReservaFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MunuSellerActivity extends AppCompatActivity {

    private BottomNavigationView bottomnavigationSeller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_munu_seller);

        // Ajustamos solo los insets necesarios, quitando el padding inferior
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0); // <- Corregido aquí
            return insets;
        });

        bottomnavigationSeller = findViewById(R.id.bottomnavigationSeller);
        // Cargar fragmento por defecto
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmnetContainer, new GestionClientesFragment())
                    .commit();
            bottomnavigationSeller.setSelectedItemId(R.id.navPanelAdministrativo);
        }

        // Lógica del BottomNavigationView
        bottomnavigationSeller.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navGestionarClientes) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmnetContainer, new GestionClientesFragment())
                        .commit();
                return true;
            } else if (id == R.id.navReservas) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmnetContainer, new ReservaFragment())
                        .commit();
                return true;
            } else if (id == R.id.navSalir) {
                mostrarDialogoSalirApp();
                return true;
            } else if (id == R.id.navCerrarSesion) {
                mostrarDialogoCerrarSesion();
                return true;
            }
            return false;
        });
    }

    private void mostrarDialogoCerrarSesion() {
        new AlertDialog.Builder(this)
                .setTitle("Cerrar Sesión")
                .setMessage("¿Está seguro que desea cerrar sesión?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Sí, cerrar sesión", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cerrarSesion();
                    }
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
    }

    private void mostrarDialogoSalirApp() {
        new AlertDialog.Builder(this)
                .setTitle("Salir de la Aplicación")
                .setMessage("¿Desea salir de la aplicación? Su sesión permanecerá activa.")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("Salir", (dialog, which) -> {
                    moveTaskToBack(true);
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(0);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
    }

    private void cerrarSesion() {
        try {
            Log.d(TAG, "Iniciando proceso de cierre de sesión");
            Toast.makeText(this, "Cerrando sesión...", Toast.LENGTH_SHORT).show();
            LoginActivity.cerrarSesionYRedireccionar(this);
        } catch (Exception e) {
            Log.e(TAG, "Error cerrando sesión", e);
            Toast.makeText(this, "Error cerrando sesión: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
