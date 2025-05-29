package com.example.pasteleriapdm.Adapters;

import android.app.AlertDialog;
import android.content.Context;
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

public class GestionarClientesAdapter extends RecyclerView.Adapter<GestionarClientesAdapter.ViewHolderGestionarClientesAdapter> {
    private Context context;
    private FragmentManager fragmentManager;
    private List<Client> listaClientes;
    private DatabaseHelper databaseHelper;
    private SimpleDateFormat dateFormat;

    // Interface para comunicar cambios al fragment
    public interface ClienteAdapterListener {
        void onClienteEliminado();
    }

    private ClienteAdapterListener listener;

    public GestionarClientesAdapter(Context context, FragmentManager fragmentManager) {
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.listaClientes = new ArrayList<>();
        this.databaseHelper = DatabaseHelper.getInstance();
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    public void setClienteAdapterListener(ClienteAdapterListener listener) {
        this.listener = listener;
    }

    public void actualizarLista(List<Client> nuevaLista) {
        this.listaClientes.clear();
        if (nuevaLista != null) {
            this.listaClientes.addAll(nuevaLista);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolderGestionarClientesAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_clientes, parent, false);
        return new ViewHolderGestionarClientesAdapter(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderGestionarClientesAdapter holder, int position) {
        if (position < listaClientes.size()) {
            Client cliente = listaClientes.get(position);

            // Configurar datos del cliente
            holder.lblNombreCompleto.setText(cliente.getName());
            holder.lblTelefono.setText("Tel: " + cliente.getPhone());

            // Direccion (mostrar texto por defecto si esta vacia)
            if (cliente.getAddress() != null && !cliente.getAddress().trim().isEmpty()) {
                holder.tvDireccion.setText(cliente.getAddress());
                holder.tvDireccion.setVisibility(View.VISIBLE);
            } else {
                holder.tvDireccion.setText("Sin direccion registrada");
                holder.tvDireccion.setVisibility(View.VISIBLE);
            }

            // Estado del cliente
            configurarEstado(holder, cliente);

            // Fechas
            configurarFechas(holder, cliente);

            // Estadisticas
            configurarEstadisticas(holder, cliente);

            // Cliente preferencial
            if (cliente.isPreferredClient()) {
                holder.tvClientePreferencial.setVisibility(View.VISIBLE);
                holder.tvClientePreferencial.setText("⭐ PREFERENCIAL");
            } else {
                holder.tvClientePreferencial.setVisibility(View.GONE);
            }

            // Obtener nombre del creador
            obtenerNombreCreador(holder, cliente);

            // Configurar eventos de los botones
            configurarEventos(holder, cliente, position);
        }
    }

    private void obtenerNombreCreador(ViewHolderGestionarClientesAdapter holder, Client cliente) {
        if (cliente.getCreatedBy() != null && !cliente.getCreatedBy().isEmpty()) {
            // Obtener el nombre del usuario que creo el cliente
            databaseHelper.getUserById(cliente.getCreatedBy(), new DatabaseHelper.DatabaseCallback<com.example.pasteleriapdm.Models.User>() {
                @Override
                public void onSuccess(com.example.pasteleriapdm.Models.User user) {
                    if (user != null && holder.lblCreadoPor != null) {
                        holder.lblCreadoPor.setText("Creado por: " + user.getName());
                    } else {
                        holder.lblCreadoPor.setText("Creado por: Usuario eliminado");
                    }
                }

                @Override
                public void onError(String error) {
                    if (holder.lblCreadoPor != null) {
                        holder.lblCreadoPor.setText("Creado por: No disponible");
                    }
                }
            });
        } else {
            holder.lblCreadoPor.setText("Creado por: No disponible");
        }
    }

    private void configurarEstado(ViewHolderGestionarClientesAdapter holder, Client cliente) {
        String estado = cliente.getStatus();
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

        holder.tvEstado.setText(estadoTexto);
        if (context != null) {
            holder.tvEstado.setTextColor(context.getResources().getColor(colorEstado));
        }
    }

    private void configurarFechas(ViewHolderGestionarClientesAdapter holder, Client cliente) {
        // Fecha de creacion
        if (cliente.getCreatedAt() > 0) {
            String fechaCreacion = dateFormat.format(new Date(cliente.getCreatedAt()));
            holder.lblFechaCreacion.setText("Creado: " + fechaCreacion);
        } else {
            holder.lblFechaCreacion.setText("Creado: No disponible");
        }

        // Fecha de actualizacion
        if (cliente.getUpdatedAt() > 0) {
            String fechaActualizacion = dateFormat.format(new Date(cliente.getUpdatedAt()));
            holder.lblUltimaActualizacion.setText("Actualizado: " + fechaActualizacion);
        } else {
            holder.lblUltimaActualizacion.setText("Actualizado: No disponible");
        }

        // Ultimo pedido
        if (cliente.getLastOrderDate() > 0) {
            String ultimoPedido = dateFormat.format(new Date(cliente.getLastOrderDate()));
            holder.lblUltimoPedido.setText("Ultimo pedido: " + ultimoPedido);
        } else {
            holder.lblUltimoPedido.setText("Ultimo pedido: Sin pedidos");
        }
    }

    private void configurarEstadisticas(ViewHolderGestionarClientesAdapter holder, Client cliente) {
        // Total de pedidos
        holder.tvTotalPedidos.setText(cliente.getTotalOrders() + " pedidos");

        // Total gastado
        holder.tvTotalGastado.setText(cliente.getFormattedTotalSpent());
    }

    private void configurarEventos(ViewHolderGestionarClientesAdapter holder, Client cliente, int position) {
        // Boton Editar
        holder.btnEditarCliente.setOnClickListener(v -> {
            ClientesDialog dialog = ClientesDialog.newInstanceForEdit(cliente);
            dialog.setClienteDialogListener(new ClientesDialog.ClienteDialogListener() {
                @Override
                public void onClienteCreado() {
                    // No aplica para edicion
                }

                @Override
                public void onClienteEditado() {
                    if (listener != null) {
                        listener.onClienteEliminado(); // Reutilizamos para refrescar la lista
                    }
                }
            });
            dialog.show(fragmentManager, "editarCliente");
        });

        // Configurar boton eliminar segun el estado del cliente
        configurarBotonEliminar(holder, cliente, position);
    }

    private void configurarBotonEliminar(ViewHolderGestionarClientesAdapter holder, Client cliente, int position) {
        boolean esInactivo = Client.STATUS_INACTIVE.equals(cliente.getStatus());

        if (esInactivo) {
            // Cliente inactivo - permitir eliminacion
            holder.btnEliminarCliente.setEnabled(true);
            holder.btnEliminarCliente.setAlpha(1.0f);
            holder.btnEliminarCliente.setOnClickListener(v -> mostrarDialogoEliminar(cliente, position));
        } else {
            // Cliente activo o bloqueado - no permitir eliminacion
            holder.btnEliminarCliente.setEnabled(false);
            holder.btnEliminarCliente.setAlpha(0.5f);
            holder.btnEliminarCliente.setOnClickListener(v -> mostrarMensajeNoSePuedeEliminar(cliente));
        }
    }

    private void mostrarMensajeNoSePuedeEliminar(Client cliente) {
        String mensaje;
        if (Client.STATUS_ACTIVE.equals(cliente.getStatus())) {
            mensaje = "No se puede eliminar un cliente activo. \nCambia su estado a 'Inactivo' primero.";
        } else if (Client.STATUS_BLOCKED.equals(cliente.getStatus())) {
            mensaje = "No se puede eliminar un cliente bloqueado. \nCambia su estado a 'Inactivo' primero.";
        } else {
            mensaje = "Solo se pueden eliminar clientes inactivos.";
        }

        Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show();
    }

    private void mostrarDialogoEliminar(Client cliente, int position) {
        new AlertDialog.Builder(context)
                .setTitle("¿Eliminar Cliente?")
                .setMessage("¿Estas seguro de que deseas eliminar a " + cliente.getName() + "?\n\n" +
                        "Esta accion no se puede deshacer.")
                .setPositiveButton("Si, Eliminar", (dialog, which) -> {
                    eliminarCliente(cliente, position);
                })
                .setNegativeButton("Cancelar", null)
                .setIcon(R.drawable.ic_eliminar)
                .show();
    }

    private void eliminarCliente(Client cliente, int position) {
        // Mostrar mensaje de carga
        Toast.makeText(context, "Eliminando cliente...", Toast.LENGTH_SHORT).show();

        // Implementacion real con Firebase
        databaseHelper.deleteClient(cliente.getId(), new DatabaseHelper.DatabaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (context != null) {
                    // Eliminar de la lista local
                    listaClientes.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, listaClientes.size());

                    Toast.makeText(context, "Cliente " + cliente.getName() + " eliminado exitosamente", Toast.LENGTH_SHORT).show();

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

    public class ViewHolderGestionarClientesAdapter extends RecyclerView.ViewHolder {
        public ImageView imgCliente;
        public TextView lblNombreCompleto, lblTelefono;
        public TextView tvDireccion;
        public TextView tvEstado;
        public TextView lblFechaCreacion, lblUltimaActualizacion, lblUltimoPedido;
        public TextView tvTotalPedidos, tvTotalGastado;
        public TextView lblCreadoPor;
        public TextView tvClientePreferencial;
        public ImageButton btnEditarCliente, btnEliminarCliente;

        public ViewHolderGestionarClientesAdapter(@NonNull View itemView) {
            super(itemView);

            imgCliente = itemView.findViewById(R.id.imgCliente);
            lblNombreCompleto = itemView.findViewById(R.id.lblNombreCompleto);
            lblTelefono = itemView.findViewById(R.id.lblTelefono);
            tvDireccion = itemView.findViewById(R.id.tvDireccion);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            lblFechaCreacion = itemView.findViewById(R.id.lblFechaCreacion);
            lblUltimaActualizacion = itemView.findViewById(R.id.lblUltimaActualizacion);
            lblUltimoPedido = itemView.findViewById(R.id.lblUltimoPedido);
            tvTotalPedidos = itemView.findViewById(R.id.tvTotalPedidos);
            tvTotalGastado = itemView.findViewById(R.id.tvTotalGastado);
            lblCreadoPor = itemView.findViewById(R.id.lblCreadoPor);
            tvClientePreferencial = itemView.findViewById(R.id.tvClientePreferencial);
            btnEditarCliente = itemView.findViewById(R.id.btnEditarCliente);
            btnEliminarCliente = itemView.findViewById(R.id.btnEliminarCliente);
        }
    }
}