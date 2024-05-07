package com.example.katzen.DataBaseFirebase

import com.example.katzen.Model.ClienteModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

interface OnCompleteListener {
    fun onComplete(success: Boolean, message: String)
}
class FirebaseClienteUtil {
    companion object {
        private const val CLIENTES_PATH = "Katzen/Cliente"
        private const val CLIENTES_IMAGES_PATH = "Clientes"
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val referenciaCliente: DatabaseReference = database.getReference(CLIENTES_PATH)
        fun obtenerListaClientes(listener: ValueEventListener) {
            referenciaCliente.addValueEventListener(listener)
        }
        suspend fun obtenerClientePorId(idCliente: String): ClienteModel? {
            return suspendCancellableCoroutine { continuation ->
                referenciaCliente.child(idCliente).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val cliente = snapshot.getValue(ClienteModel::class.java)
                        continuation.resume(cliente)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        println("Error al obtener el cliente: ${error.message}")
                        continuation.resume(null)
                    }
                })
            }
        }
        fun eliminarCliente(clienteId: String, listener: OnCompleteListener) {
            val clienteReference = referenciaCliente.child(clienteId)

            clienteReference.removeValue { error, _ ->
                if (error == null) {
                    // Si no hay errores, la eliminación fue exitosa
                    listener.onComplete(true, "Cliente eliminado correctamente")
                } else {
                    // Si hay errores, la eliminación falló
                    listener.onComplete(false, "Error al eliminar el cliente: ${error.message}")
                }
            }
        }
        suspend fun editarCliente(clienteId: String, nuevoCliente: ClienteModel): Result<String> {
            return withContext(Dispatchers.IO) {
                suspendCoroutine { continuation ->
                    val clienteReference = referenciaCliente.child(clienteId)

                    clienteReference.setValue(nuevoCliente) { error, _ ->
                        if (error == null) {
                            // Si no hay errores, la edición fue exitosa
                            continuation.resume(Result.success("Cliente editado correctamente"))
                        } else {
                            // Si hay errores, la edición falló
                            continuation.resumeWithException(error.toException())
                        }
                    }
                }
            }
        }
        fun eliminarCliente(clienteId: String, clienteReference: DatabaseReference, listener: OnCompleteListener<Void>) {
            clienteReference.child(clienteId).removeValue()
                .addOnCompleteListener(listener)
        }

        // Eliminar los pacientes de un cliente por su ID de cliente
        fun eliminarPacientesDeCliente(idCliente: String, referenciaMascota: DatabaseReference, listener: OnCompleteListener<Void>) {
            referenciaMascota.orderByChild("idCliente").equalTo(idCliente)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // Recorrer los pacientes y eliminarlos
                        for (pacienteSnapshot in snapshot.children) {
                            pacienteSnapshot.ref.removeValue()
                        }
                        listener.onComplete(null) // Indicar que la eliminación de pacientes ha sido completada
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Manejar el error en caso de que ocurra
                        listener.onComplete(error.toException())
                    }
                })
        }
    }
}