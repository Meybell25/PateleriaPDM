package com.example.pasteleriapdm.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pasteleriapdm.Utils.DatabaseHelper;
import com.example.pasteleriapdm.Dialogs.ClientesDialog;
import com.example.pasteleriapdm.Models.Client;
import com.example.pasteleriapdm.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ClientesSellerAdapter extends RecyclerView.Adapter<ClientesSellerAdapter.ViewHolderClientesSeller> {
    private static final String TAG = "ClientesSellerAdapter";

    private Context context;
    private FragmentManager fragmentManager;
    private List<Client> listaClientes;
    private DatabaseHelper databaseHelper;
    private SimpleDateFormat dateFormat;

    // Interface para comunicar cambios al fragment
    public interface ClienteSellerAdapterListener {
        void onClienteEliminado();
    }

    private ClienteSellerAdapterListener listener;

    public ClientesSellerAdapter(Context context, FragmentManager fragmentManager) {
        try {
            this.context = context;
            this.fragmentManager = fragmentManager;
            this.listaClientes = new ArrayList<>();
            this.databaseHelper = DatabaseHelper.getInstance();
            this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            Log.d(TAG, "ClientesSellerAdapter inicializado correctamente");
        } catch (Exception e) {
            Log.e(TAG, "Error inicializando ClientesSellerAdapter: " + e.getMessage(), e);
        }
    }

    public void setClienteSellerAdapterListener(ClienteSellerAdapterListener listener) {
        this.listener = listener;
        Log.d(TAG, "Listener configurado: " + (listener != null));
    }

    public void actualizarLista(List<Client> nuevaLista) {
        try {
            Log.d(TAG, "Actualizando lista - elementos anteriores: " + listaClientes.size());

            this.listaClientes.clear();
            if (nuevaLista != null) {
                this.listaClientes.addAll(nuevaLista);
            }

            Log.d(TAG, "Lista actualizada - nuevos elementos: " + listaClientes.size());
            notifyDataSetChanged();

        } catch (Exception e) {
            Log.e(TAG, "Error actualizando lista: " + e.getMessage(), e);
        }
    }

    @NonNull
    @Override
    public ViewHolderClientesSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        try {
            View view = LayoutInflater.from(context).inflate(R.layout.item_clientes_seller, parent, false);
            Log.d(TAG, "ViewHolder creado correctamente");
            return new ViewHolderClientesSeller(view);
        } catch (Exception e) {
            Log.e(TAG, "Error creando ViewHolder: " + e.getMessage(), e);
            // Intentar crear un view básico para evitar crash
            View fallbackView = new View(context);
            return new ViewHolderClientesSeller(fallbackView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderClientesSeller holder, int position) {
        try {
            if (position < 0 || position >= listaClientes.size()) {
                Log.w(TAG, "Posición inválida: " + position + ", tamaño lista: " + listaClientes.size());
                return;
            }

            Client cliente = listaClientes.get(position);
            if (cliente == null) {
                Log.w(TAG, "Cliente es null en posición: " + position);
                return;
            }

            Log.d(TAG, "Vinculando cliente: " + cliente.getName() + " en posición: " + position);
            Log.d(TAG, "Datos del cliente - ID: " + cliente.getId() + ", CreatedBy: " + cliente.getCreatedBy() + ", CreatedAt: " + cliente.getCreatedAt());

            // CORRECCIÓN: Validación completa antes de mostrar
            if (cliente.getCreatedBy() == null || cliente.getCreatedBy().trim().isEmpty()) {
                Log.e(TAG, "ADVERTENCIA: Cliente sin createdBy: " + cliente.getName() + " (ID: " + cliente.getId() + ")");
                // No return aquí, mostrar el cliente pero deshabilitar edición
            }

            // Configurar datos básicos del cliente
            configurarDatosBasicos(holder, cliente);

            // Estado del cliente
            configurarEstado(holder, cliente);

            // Información de pedidos
            configurarInfoPedidos(holder, cliente);

            // Configurar eventos de los botones
            configurarEventos(holder, cliente, position);

        } catch (Exception e) {
            Log.e(TAG, "Error en onBindViewHolder posición " + position + ": " + e.getMessage(), e);
        }
    }

    private void configurarDatosBasicos(ViewHolderClientesSeller holder, Client cliente) {
        try {
            // Nombre del cliente
            if (holder.lblNombreCliente != null) {
                String nombre = cliente.getName() != null ? cliente.getName() : "Sin nombre";
                holder.lblNombreCliente.setText(nombre);
            }

            // Teléfono del cliente
            if (holder.lblTelefonoCliente != null) {
                String telefono = cliente.getPhone() != null ? cliente.getPhone() : "Sin teléfono";
                holder.lblTelefonoCliente.setText("📞 " + telefono);
            }

            // Dirección
            if (holder.lblDireccionCliente != null) {
                if (cliente.getAddress() != null && !cliente.getAddress().trim().isEmpty()) {
                    holder.lblDireccionCliente.setText("📍 " + cliente.getAddress());
                    holder.lblDireccionCliente.setVisibility(View.VISIBLE);
                } else {
                    holder.lblDireccionCliente.setVisibility(View.GONE);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error configurando datos básicos: " + e.getMessage(), e);
        }
    }

    private void configurarEstado(ViewHolderClientesSeller holder, Client cliente) {
        try {
            if (holder.lblEstadoCliente == null) return;

            String estado = cliente.getStatus() != null ? cliente.getStatus() : Client.STATUS_INACTIVE;
            String estadoTexto;
            int colorEstado;

            switch (estado) {
                case Client.STATUS_ACTIVE:
                    estadoTexto = "✓ Activo";
                    colorEstado = R.color.verde_exitoso;
                    break;
                case Client.STATUS_INACTIVE:
                    estadoTexto = "⏸ Inactivo";
                    colorEstado = R.color.naranja_advertencia;
                    break;
                case Client.STATUS_BLOCKED:
                    estadoTexto = "🚫 Bloqueado";
                    colorEstado = R.color.rojo_error;
                    break;
                default:
                    estadoTexto = "? Desconocido";
                    colorEstado = R.color.gris_texto;
                    break;
            }

            holder.lblEstadoCliente.setText(estadoTexto);
            if (context != null) {
                try {
                    holder.lblEstadoCliente.setTextColor(context.getResources().getColor(colorEstado));
                } catch (Exception e) {
                    Log.w(TAG, "Error configurando color de estado: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error configurando estado: " + e.getMessage(), e);
        }
    }

    private void configurarInfoPedidos(ViewHolderClientesSeller holder, Client cliente) {
        try {
            // Información de pedidos
            if (holder.lblTotalPedidos != null) {
                holder.lblTotalPedidos.setText(cliente.getTotalOrders() + " pedidos");
            }

            if (holder.lblTotalGastado != null) {
                holder.lblTotalGastado.setText(cliente.getFormattedTotalSpent());
            }

            // Último pedido
            if (holder.lblUltimoPedido != null) {
                if (cliente.getLastOrderDate() > 0) {
                    String ultimoPedido = dateFormat.format(new Date(cliente.getLastOrderDate()));
                    holder.lblUltimoPedido.setText("Último: " + ultimoPedido);
                    holder.lblUltimoPedido.setVisibility(View.VISIBLE);
                } else {
                    holder.lblUltimoPedido.setText("Sin pedidos");
                    holder.lblUltimoPedido.setVisibility(View.VISIBLE);
                }
            }

            // Cliente preferencial
            if (holder.lblClientePreferencial != null) {
                if (cliente.isPreferredClient()) {
                    holder.lblClientePreferencial.setVisibility(View.VISIBLE);
                    holder.lblClientePreferencial.setText("⭐ PREFERENCIAL");
                } else {
                    holder.lblClientePreferencial.setVisibility(View.GONE);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error configurando info pedidos: " + e.getMessage(), e);
        }
    }

    // CORRECCIÓN EN EL MÉTODO configurarEventos del ClientesSellerAdapter
    private void configurarEventos(ViewHolderClientesSeller holder, Client cliente, int position) {
        try {
            // Botón Editar - Siempre habilitado para los clientes del seller
            if (holder.btnEditarCliente != null) {
                holder.btnEditarCliente.setEnabled(true);
                holder.btnEditarCliente.setAlpha(1.0f);

                holder.btnEditarCliente.setOnClickListener(v -> {
                    try {
                        // CORRECCIÓN: Pasar TODOS los campos necesarios incluyendo createdBy
                        ClientesDialog dialog = ClientesDialog.newInstanceForEdit(cliente);
                        dialog.setClienteDialogListener(new ClientesDialog.ClienteDialogListener() {
                            @Override
                            public void onClienteCreado() {}

                            @Override
                            public void onClienteEditado() {
                                if (listener != null) {
                                    listener.onClienteEliminado(); // Recargar lista
                                }
                            }
                        });
                        dialog.show(fragmentManager, "editarCliente");
                    } catch (Exception e) {
                        Toast.makeText(context, "Error al editar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            configurarBotonEliminar(holder, cliente, position);
        } catch (Exception e) {
            Log.e(TAG, "Error configurando eventos: " + e.getMessage(), e);
        }
    }

    /**
     * NUEVO MÉTODO: Crear una copia completa del cliente con todos los campos necesarios
     */
    private Client crearCopiaCompletaCliente(Client clienteOriginal) {
        try {
            Client copia = new Client();

            // Campos básicos obligatorios
            copia.setId(clienteOriginal.getId());
            copia.setName(clienteOriginal.getName());
            copia.setPhone(clienteOriginal.getPhone());
            copia.setAddress(clienteOriginal.getAddress());
            copia.setStatus(clienteOriginal.getStatus());

            // CRÍTICO: Campos de auditoría
            copia.setCreatedBy(clienteOriginal.getCreatedBy());
            copia.setCreatedAt(clienteOriginal.getCreatedAt());
            copia.setUpdatedAt(clienteOriginal.getUpdatedAt());

            // Campos opcionales de estadísticas
            copia.setTotalOrders(clienteOriginal.getTotalOrders());
            copia.setTotalSpent(clienteOriginal.getTotalSpent());
            copia.setLastOrderDate(clienteOriginal.getLastOrderDate());
            copia.setPreferredClient(clienteOriginal.isPreferredClient());

            Log.d(TAG, "Copia de cliente creada - ID: " + copia.getId() + ", CreatedBy: " + copia.getCreatedBy());

            return copia;

        } catch (Exception e) {
            Log.e(TAG, "Error creando copia del cliente: " + e.getMessage(), e);
            // Retornar el cliente original si hay error
            return clienteOriginal;
        }
    }

    /**
     * NUEVO MÉTODO: Validar que el cliente tenga todos los datos necesarios para edición
     */
    private boolean validarClienteParaEdicion(Client cliente) {
        if (cliente == null) {
            Log.e(TAG, "Cliente es null");
            return false;
        }

        if (cliente.getId() == null || cliente.getId().trim().isEmpty()) {
            Log.e(TAG, "Cliente sin ID: " + cliente.getName());
            return false;
        }

        if (cliente.getCreatedBy() == null || cliente.getCreatedBy().trim().isEmpty()) {
            Log.e(TAG, "Cliente sin createdBy: " + cliente.getName() + " (ID: " + cliente.getId() + ")");
            return false;
        }

        if (cliente.getName() == null || cliente.getName().trim().isEmpty()) {
            Log.e(TAG, "Cliente sin nombre válido (ID: " + cliente.getId() + ")");
            return false;
        }

        if (cliente.getPhone() == null || cliente.getPhone().trim().isEmpty()) {
            Log.e(TAG, "Cliente sin teléfono válido: " + cliente.getName());
            return false;
        }

        Log.d(TAG, "Cliente válido para edición: " + cliente.getName() + " (ID: " + cliente.getId() + ", CreatedBy: " + cliente.getCreatedBy() + ")");
        return true;
    }


    private void configurarBotonEliminar(ViewHolderClientesSeller holder, Client cliente, int position) {
        try {
            if (holder.btnEliminarCliente == null) return;

            boolean esInactivo = Client.STATUS_INACTIVE.equals(cliente.getStatus());

            if (esInactivo) {
                // Cliente inactivo - permitir eliminación
                holder.btnEliminarCliente.setEnabled(true);
                holder.btnEliminarCliente.setAlpha(1.0f);
                holder.btnEliminarCliente.setOnClickListener(v -> mostrarDialogoEliminar(cliente, position));
            } else {
                // Cliente activo o bloqueado - no permitir eliminación
                holder.btnEliminarCliente.setEnabled(false);
                holder.btnEliminarCliente.setAlpha(0.5f);
                holder.btnEliminarCliente.setOnClickListener(v -> mostrarMensajeNoSePuedeEliminar(cliente));
            }

        } catch (Exception e) {
            Log.e(TAG, "Error configurando botón eliminar: " + e.getMessage(), e);
        }
    }

    private void mostrarMensajeNoSePuedeEliminar(Client cliente) {
        try {
            String mensaje;
            if (Client.STATUS_ACTIVE.equals(cliente.getStatus())) {
                mensaje = "No se puede eliminar un cliente activo.\nCámbialo a 'Inactivo' primero.";
            } else if (Client.STATUS_BLOCKED.equals(cliente.getStatus())) {
                mensaje = "No se puede eliminar un cliente bloqueado.\nCámbialo a 'Inactivo' primero.";
            } else {
                mensaje = "Solo se pueden eliminar clientes inactivos.";
            }

            Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "Error mostrando mensaje: " + e.getMessage(), e);
        }
    }

    private void mostrarDialogoEliminar(Client cliente, int position) {
        new AlertDialog.Builder(context)
                .setTitle("¿Eliminar Cliente?")
                .setMessage("¿Estás seguro de que deseas eliminar a " + cliente.getName() + "?\n\n" +
                        "Esta acción no se puede deshacer.")
                .setPositiveButton("Sí, Eliminar", (dialog, which) -> {
                    eliminarCliente(cliente, position);
                })
                .setNegativeButton("Cancelar", null)
                .setIcon(R.drawable.ic_eliminar)
                .show();
    }

    private void eliminarCliente(Client cliente, int position) {
        // Mostrar mensaje de carga
        Toast.makeText(context, "Eliminando cliente...", Toast.LENGTH_SHORT).show();

        databaseHelper.deleteClient(cliente.getId(), new DatabaseHelper.DatabaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (context != null) {
                    // Eliminar de la lista local
                    listaClientes.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, listaClientes.size());

                    Toast.makeText(context, "Cliente eliminado exitosamente", Toast.LENGTH_SHORT).show();

                    if (listener != null) {
                        listener.onClienteEliminado();
                    }
                }
            }

            @Override
            public void onError(String error) {
                if (context != null) {
                    Toast.makeText(context, "Error eliminando cliente: " + error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaClientes.size();
    }

    public class ViewHolderClientesSeller extends RecyclerView.ViewHolder {
        // Variables basadas en el layout item_cliente_seller.xml
        public ImageView imgClienteSeller;
        public TextView lblNombreCliente, lblTelefonoCliente, lblDireccionCliente;
        public TextView lblEstadoCliente;
        public TextView lblTotalPedidos, lblTotalGastado, lblUltimoPedido;
        public TextView lblClientePreferencial;
        public ImageButton btnEditarCliente, btnEliminarCliente;

        public ViewHolderClientesSeller(@NonNull View itemView) {
            super(itemView);


            imgClienteSeller = itemView.findViewById(R.id.imgClienteSeller);
            lblNombreCliente = itemView.findViewById(R.id.lblNombreCompleto);
            lblTelefonoCliente = itemView.findViewById(R.id.lblTelefonoCliente);
            lblDireccionCliente = itemView.findViewById(R.id.lblDireccionCliente);
            lblEstadoCliente = itemView.findViewById(R.id.lblEstadoCliente);
            lblTotalPedidos = itemView.findViewById(R.id.lblTotalPedidos);
            lblTotalGastado = itemView.findViewById(R.id.lblTotalGastado);
            lblUltimoPedido = itemView.findViewById(R.id.lblUltimoPedido);
            lblClientePreferencial = itemView.findViewById(R.id.lblClientePreferencial);
            btnEditarCliente = itemView.findViewById(R.id.btnEditarCliente);
            btnEliminarCliente = itemView.findViewById(R.id.btnEliminarCliente);
        }
    }
}