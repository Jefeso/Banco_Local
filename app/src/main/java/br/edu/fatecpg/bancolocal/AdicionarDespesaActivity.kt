package br.edu.fatecpg.bancolocal // Verifique se este é o seu pacote

import AppDatabase
import Despesa
import DespesaDao
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

class AdicionarDespesaActivity : AppCompatActivity() {

    // Declaração dos componentes da UI
    private lateinit var etDescricao: EditText
    private lateinit var etValor: EditText
    private lateinit var spinnerCategoria: Spinner
    private lateinit var btnData: Button
    private lateinit var btnSalvar: Button
    // private lateinit var btnExcluir: Button // Deixaremos isso para a parte de "Editar"

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
        // btnExcluir = findViewById(R.id.btnExcluir) // Fica para depois

        // Configura os componentes
        setupSpinner()
        setupDatePicker()

        // Configura o clique do botão Salvar
        btnSalvar.setOnClickListener {
            salvarDespesa()
        }

        // Atualiza o texto do botão de data com a data atual (hoje)
        atualizarTextoBotaoData()
    }

    private fun setupSpinner() {
        // Cria um adapter usando o array de categorias que definimos no strings.xml
        ArrayAdapter.createFromResource(
            this,
            R.array.categorias_array, // Nosso array de strings
            android.R.layout.simple_spinner_item // Layout padrão do spinner
        ).also { adapter ->
            // Layout padrão para o dropdown
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

            // Cria o pop-up (diálogo) do seletor de data
            val datePickerDialog = DatePickerDialog(
                this,
                { _, anoSelecionado, mesSelecionado, diaSelecionado ->
                    // Quando o usuário seleciona uma data
                    val calendarioSelecionado = Calendar.getInstance()
                    calendarioSelecionado.set(anoSelecionado, mesSelecionado, diaSelecionado)

                    // Salva a data como um Long (timestamp)
                    dataSelecionada = calendarioSelecionado.timeInMillis

                    // Atualiza o texto do botão
                    atualizarTextoBotaoData()
                },
                ano, mes, dia
            )
            datePickerDialog.show()
        }
    }

    private fun salvarDespesa() {
        // Pega os valores dos campos
        val descricao = etDescricao.text.toString()
        val valorStr = etValor.text.toString()
        val categoria = spinnerCategoria.selectedItem.toString()

        // Validação dos campos obrigatórios (requisito do PDF)
        if (descricao.isBlank()) {
            Toast.makeText(this, "A descrição é obrigatória", Toast.LENGTH_SHORT).show()
            etDescricao.error = "Campo obrigatório"
            return // Para a execução
        }
        if (valorStr.isBlank()) {
            Toast.makeText(this, "O valor é obrigatório", Toast.LENGTH_SHORT).show()
            etValor.error = "Campo obrigatório"
            return // Para a execução
        }

        val valor = valorStr.toDoubleOrNull()
        if (valor == null || valor <= 0) {
            Toast.makeText(this, "O valor é inválido", Toast.LENGTH_SHORT).show()
            etValor.error = "Valor inválido"
            return
        }

        // Cria o objeto Despesa
        val novaDespesa = Despesa(
            descricao = descricao,
            valor = valor,
            categoria = categoria,
            data = dataSelecionada
        )

        // Salva no banco de dados usando Coroutines (fora da thread principal)
        CoroutineScope(Dispatchers.IO).launch {
            despesaDao.salvar(novaDespesa) // O 'salvar' é 'suspend'

            // Após salvar, fecha a tela e volta para a MainActivity
            finish() // Isso deve ser chamado na thread principal, mas 'finish()' é seguro.
        }
    }

    private fun atualizarTextoBotaoData() {
        // Formata o Long (timestamp) para uma data legível (dd/MM/yyyy)
        val formatador = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        btnData.text = formatador.format(dataSelecionada)
    }
}