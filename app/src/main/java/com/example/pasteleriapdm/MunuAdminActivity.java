package com.example.pasteleriapdm;

import static android.content.ContentValues.TAG;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;

import com.example.pasteleriapdm.FragmentsAdmin.GestionClientesFragment;
import com.example.pasteleriapdm.FragmentsAdmin.GestionPastelesFragment;
import com.example.pasteleriapdm.FragmentsAdmin.GestionUsuariosFragment;
import com.example.pasteleriapdm.FragmentsAdmin.PanelAdministrativoFragment;
import com.example.pasteleriapdm.FragmentsAdmin.ReportesFragment;
import com.example.pasteleriapdm.FragmentsAdmin.ReservaFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class MunuAdminActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbarAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_munu_admin);

        bottomNavigationView = findViewById(R.id.bottomnavigation);
        toolbarAdmin = findViewById(R.id.toolbar);

        // Cargar fragmento por defecto
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmnetContainer, new PanelAdministrativoFragment())
                    .commit();
            bottomNavigationView.setSelectedItemId(R.id.navPanelAdministrativo);
        }

        //LOGICA DEL TOOLBAR
        toolbarAdmin.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.navReservas:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragmnetContainer, new ReservaFragment()).commit();
                        break;
                    case R.id.navVerReportes:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragmnetContainer, new ReportesFragment()).commit();
                        break;
                    case R.id.navCerrarSesion: //  caso para cerrar sesión
                        mostrarDialogoCerrarSesion();
                        break;

                    case R.id.navSalir: // Opcional: salir de la app sin cerrar sesión
                        mostrarDialogoSalirApp();
                        break;

                    default:
                        Log.w(TAG, "Opción de menú no reconocida: " + item.getItemId());
                        return false;
                }
                return true;
            }
        });

        //LOGICA DEL BTNNAVIGATION
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


    /**
     * Mostrar diálogo de confirmación para cerrar sesión
     */
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
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(true)
                .show();
    }

    /**
     * Método para cerrar sesión
     */
    private void cerrarSesion() {
        try {
            Log.d(TAG, "Iniciando proceso de cierre de sesión");

            // Mostrar mensaje de despedida
            Toast.makeText(this, "Cerrando sesión...", Toast.LENGTH_SHORT).show();

            // Usar el metodo estático del LoginActivity para cerrar sesión completa
            LoginActivity.cerrarSesionYRedireccionar(this);

        } catch (Exception e) {
            Log.e(TAG, "Error cerrando sesión", e);
            Toast.makeText(this, "Error cerrando sesión: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Diálogo opcional para salir de la app (opcional)
     */
    private void mostrarDialogoSalirApp() {
        new AlertDialog.Builder(this)
                .setTitle("Salir de la Aplicación")
                .setMessage("¿Desea salir de la aplicación? Su sesión permanecerá activa.")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("Salir", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Salir de la app sin cerrar sesión
                        moveTaskToBack(true);
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(0);
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(true)
                .show();
    }

    /**
     * Manejar el botón físico de "atrás"
     */
    @Override
    public void onBackPressed() {
        // Mostrar diálogo para confirmar si quiere salir o cerrar sesión
        mostrarDialogoBackPressed();
    }

    /**
     * Diálogo cuando se presiona el botón atrás
     */
    private void mostrarDialogoBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("¿Qué desea hacer?")
                .setMessage("Seleccione una opción:")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("Cerrar Sesión", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cerrarSesion();
                    }
                })
                .setNegativeButton("Solo Salir", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        moveTaskToBack(true);
                    }
                })
                .setNeutralButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(true)
                .show();
    }

}
