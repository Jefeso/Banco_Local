import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface DespesaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvar(despesa: Despesa)

    @Delete
    suspend fun excluir(despesa: Despesa)

    @Query("SELECT * FROM despesas WHERE id = :id")
    suspend fun getDespesaPorId(id: Int): Despesa?

    @Query("SELECT * FROM despesas ORDER BY data DESC")
    suspend fun getTodasDespesas(): List<Despesa>
}