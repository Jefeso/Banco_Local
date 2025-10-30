package br.edu.fatecpg.bancolocal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.edu.fatecpg.bancolocal.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DespesaAdapter(
    private var despesas: List<Despesa>,
    private val onItemClick: (Despesa) -> Unit
) : RecyclerView.Adapter<DespesaAdapter.DespesaViewHolder>() {

    class DespesaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val descricao: TextView = itemView.findViewById(R.id.tvItemDescricao)
        val valor: TextView = itemView.findViewById(R.id.tvItemValor)
        val data: TextView = itemView.findViewById(R.id.tvItemData)

        fun bind(despesa: Despesa, onItemClick: (Despesa) -> Unit) {
            descricao.text = despesa.descricao
            valor.text = String.format(Locale("pt", "BR"), "R$ %.2f", despesa.valor)
            data.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(despesa.data))

            itemView.setOnClickListener {
                onItemClick(despesa)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DespesaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_despesa, parent, false)
        return DespesaViewHolder(view)
    }

    override fun onBindViewHolder(holder: DespesaViewHolder, position: Int) {
        val despesa = despesas[position]
        holder.bind(despesa, onItemClick)
    }

    override fun getItemCount() = despesas.size


    fun atualizarLista(novaLista: List<Despesa>) {
        despesas = novaLista
        notifyDataSetChanged()
    }
}