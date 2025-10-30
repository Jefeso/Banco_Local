package br.edu.fatecpg.bancolocal

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    // Declaração das variáveis principais
    private lateinit var db: AppDatabase
    private lateinit var despesaDao: DespesaDao
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DespesaAdapter
    private lateinit var fab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Inicializa o Banco de Dados e o DAO
        db = AppDatabase.getDatabase(this)
        despesaDao = db.despesaDao()

        // 2. Encontra os componentes da UI no layout XML
        recyclerView = findViewById(R.id.recyclerViewDespesas)
        fab = findViewById(R.id.fabAdicionar)

// 3. Configura o Adapter e o RecyclerView
        adapter = DespesaAdapter(emptyList()) { despesaClicada ->
            // Ação de clique para cada item da lista
            val intent = Intent(this, AdicionarDespesaActivity::class.java)
            intent.putExtra("DESPESA_ID", despesaClicada.id)
            startActivity(intent)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)


        fab.setOnClickListener {

            val intent = Intent(this, AdicionarDespesaActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Carrega (ou recarrega) as despesas toda vez que a tela principal fica visível
        carregarDespesas()
    }

    private fun carregarDespesas() {
        // Coroutines: Operações de banco NÃO podem rodar na thread principal
        CoroutineScope(Dispatchers.IO).launch {
            // Busca os dados no banco (em background)
            val listaDespesas = despesaDao.getTodasDespesas()

            // Volta para a thread principal para atualizar a UI
            withContext(Dispatchers.Main) {
                adapter.atualizarLista(listaDespesas)
            }
        }
    }
}