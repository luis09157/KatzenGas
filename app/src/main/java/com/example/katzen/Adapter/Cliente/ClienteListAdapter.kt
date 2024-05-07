package com.example.katzen.Adapter.Cliente

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.katzen.DataBaseFirebase.FirebaseClienteUtil
import com.example.katzen.DataBaseFirebase.FirebasePacienteUtil
import com.example.katzen.DataBaseFirebase.OnCompleteListener
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.Model.ClienteModel
import com.example.katzen.R
import com.google.firebase.database.DatabaseReference
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ClienteListAdapter (
    activity: Activity,
    private var clienteList: List<ClienteModel>
) : ArrayAdapter<ClienteModel>(activity, R.layout.view_list_paciente, clienteList) {

    private var originalList: List<ClienteModel> = clienteList.toList()
    var activity : Activity = activity
    var TAG : String = "ClienteListAdapter"
    private lateinit var clienteReference: DatabaseReference
    private lateinit var pacienteReference: DatabaseReference

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        val holder: ViewHolder

        if (itemView == null) {
            itemView = LayoutInflater.from(activity).inflate(R.layout.view_list_paciente, parent, false)
            holder = ViewHolder()
            holder.imgPerfil = itemView.findViewById(R.id.imgPerfil)
            holder.nombreCliente = itemView.findViewById(R.id.text_nombre)
            holder.descripcion = itemView.findViewById(R.id.text_descripcion)
            holder.btnEliminar = itemView.findViewById(R.id.btnEliminar)
            itemView.tag = holder
        } else {
            holder = itemView.tag as ViewHolder
        }
        val paciente = clienteList[position]

        holder.nombreCliente?.text = ""
        holder.descripcion?.text = ""
        holder.imgPerfil?.setImageResource(R.drawable.ic_perfil)

        if (paciente.imageUrl.isNotEmpty()) {
            Picasso.get()
                .load(paciente.imageUrl)
                .placeholder(R.drawable.ic_perfil) // Establecer la imagen predeterminada
                .error(R.drawable.no_disponible_rosa) // Opcional: establecer una imagen en caso de error al cargar
                .into(holder.imgPerfil)
        } else {
            holder.imgPerfil?.setImageResource(R.drawable.no_disponible_rosa)
        }

        holder.nombreCliente?.text = "${paciente.nombre}"
        holder.descripcion?.text = "Tel: ${paciente.telefono}"

        holder.descripcion!!.setOnClickListener {
            UtilHelper.enviarMensajeWhatsApp(activity, paciente.telefono)
        }

        holder.btnEliminar?.setOnClickListener {
            activity.runOnUiThread {
                DialogMaterialHelper.mostrarConfirmDialog(activity, "¿Estás seguro de que deseas eliminar este Cliente?") { confirmed ->
                    if (confirmed) {
                        CoroutineScope(Dispatchers.IO).launch {
                            eliminarClienteYClientes()
                        }
                    } else {
                        // El usuario canceló la operación
                    }
                }
            }
        }



        return itemView!!
    }
    override fun getCount(): Int {
        return clienteList.size
    }
    override fun getItem(position: Int): ClienteModel? {
        return clienteList[position]
    }
    fun updateList(newList: List<ClienteModel>) {
        clienteList = newList
        originalList = newList.toList()
        notifyDataSetChanged()
    }
    private class ViewHolder {
        var imgPerfil: ImageView? = null
        var nombreCliente: TextView? = null
        var descripcion: TextView? = null
        var btnEliminar: LinearLayout? = null

    }
    private fun eliminarClienteYClientes(cliente : ClienteModel) {
        // Eliminar el cliente y luego sus pacientes
        FirebaseClienteUtil.eliminarCliente(cliente.id, clienteReference, OnCompleteListener { clienteTask ->
            if (clienteTask.isSuccessful) {
                // Cliente eliminado exitosamente, ahora eliminar los pacientes
                FirebaseClienteUtil.eliminarPacientesDeCliente(clienteId, pacienteReference, OnCompleteListener { pacientesTask ->
                    if (pacientesTask.isSuccessful) {
                        // Todos los pacientes del cliente fueron eliminados
                        // Puedes mostrar un mensaje de éxito o realizar alguna otra acción aquí
                    } else {
                        // Error al eliminar los pacientes
                        val error = pacientesTask.exception?.message ?: "Error desconocido al eliminar los pacientes"
                        // Manejar el error
                    }
                })
            } else {
                // Error al eliminar el cliente
                val error = clienteTask.exception?.message ?: "Error desconocido al eliminar el cliente"
                // Manejar el error
            }
        })
    }
}
