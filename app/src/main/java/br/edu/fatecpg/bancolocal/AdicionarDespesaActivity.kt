package br.edu.fatecpg.bancolocal


import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
// Imports que VAMOS precisar para os próximos passos
import android.view.View
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.withContext

class AdicionarDespesaActivity : AppCompatActivity() {

    // Declaração dos componentes da UI
    private lateinit var btnExcluir: Button
    private var despesaId: Int = -1
    private lateinit var etDescricao: EditText
    private lateinit var etValor: EditText
    private lateinit var spinnerCategoria: Spinner
    private lateinit var btnData: Button
    private lateinit var btnSalvar: Button

    // Declaração do banco
    private lateinit var db: AppDatabase
    private lateinit var despesaDao: DespesaDao

    // Variável para armazenar a data selecionada em milissegundos (Long)
    private var dataSelecionada: Long = Calendar.getInstance().timeInMillis

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adicionar_despesa)

        // Inicializa o banco e o DAO
        db = AppDatabase.getDatabase(this)
        despesaDao = db.despesaDao()

        // Linka as variáveis com os IDs do XML
        etDescricao = findViewById(R.id.etDescricao)
        etValor = findViewById(R.id.etValor)
        spinnerCategoria = findViewById(R.id.spinnerCategoria)
        btnData = findViewById(R.id.btnData)
        btnSalvar = findViewById(R.id.btnSalvar)
        btnExcluir = findViewById(R.id.btnExcluir)

        // Verifica se recebemos um ID da MainActivity
        despesaId = intent.getIntExtra("DESPESA_ID", -1)

        if (despesaId != -1) {
            setTitle("Editar Despesa")
            carregarDadosDespesa(despesaId)
        } else {
            // Estamos no modo ADIÇÃO
            setTitle("Adicionar Despesa")
            atualizarTextoBotaoData()
        }

        setupSpinner()
        setupDatePicker()

        btnSalvar.setOnClickListener {
            salvarDespesa()
        }

        btnExcluir.setOnClickListener {
            excluirDespesa()
        }
    }

    private fun setupSpinner() {
        ArrayAdapter.createFromResource(
            this, R.array.categorias_array, android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCategoria.adapter = adapter
        }
    }
    private fun setupDatePicker() {
        btnData.setOnClickListener {
            val calendario = Calendar.getInstance()
            val ano = calendario.get(Calendar.YEAR)
            val mes = calendario.get(Calendar.MONTH)
            val dia = calendario.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, anoSelecionado, mesSelecionado, diaSelecionado ->
                    val calendarioSelecionado = Calendar.getInstance()
                    calendarioSelecionado.set(anoSelecionado, mesSelecionado, diaSelecionado)
                    dataSelecionada = calendarioSelecionado.timeInMillis
                    atualizarTextoBotaoData()
                },
                ano, mes, dia
            )
            datePickerDialog.show()
        }
    }
    private fun atualizarTextoBotaoData() {
        val formatador = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        btnData.text = formatador.format(dataSelecionada)
    }

    private fun salvarDespesa() {
        val descricao = etDescricao.text.toString()
        val valorStr = etValor.text.toString()
        val categoria = spinnerCategoria.selectedItem.toString()

        // Validações
        if (descricao.isBlank()) {
            etDescricao.error = "Campo obrigatório"; return
        }
        if (valorStr.isBlank()) {
            etValor.error = "Campo obrigatório"; return
        }
        val valor = valorStr.toDoubleOrNull()
        if (valor == null || valor <= 0) {
            etValor.error = "Valor inválido"; return
        }

        val despesaParaSalvar = Despesa(
            id = if (despesaId == -1) 0 else despesaId,
            descricao = descricao,
            valor = valor,
            categoria = categoria,
            data = dataSelecionada
        )
        // ---- FIM DA LÓGICA ----

        CoroutineScope(Dispatchers.IO).launch {
            despesaDao.salvar(despesaParaSalvar)
            finish()
        }
    }

    private fun carregarDadosDespesa(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val despesa = despesaDao.getDespesaPorId(id)

            withContext(Dispatchers.Main) {
                despesa?.let {
                    etDescricao.setText(it.descricao)
                    etValor.setText(it.valor.toString())
                    dataSelecionada = it.data
                    atualizarTextoBotaoData()

                    val categorias = resources.getStringArray(R.array.categorias_array)
                    val posicao = categorias.indexOf(it.categoria)
                    if (posicao >= 0) {
                        spinnerCategoria.setSelection(posicao)
                    }

                    btnExcluir.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun excluirDespesa() {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Exclusão")
            .setMessage("Tem certeza que deseja excluir esta despesa?")
            .setPositiveButton("Excluir") { _, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                    val despesaParaExcluir = Despesa(id = despesaId, descricao = "", valor = 0.0, categoria = "", data = 0L)
                    despesaDao.excluir(despesaParaExcluir)
                    finish()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

}

private fun AppDatabase.despesaDao() {
    TODO("Not yet implemented")
}
