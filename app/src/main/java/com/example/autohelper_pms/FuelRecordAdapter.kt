import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.autohelper_pms.DeleteConfirmationDialog
import com.example.autohelper_pms.R

class FuelRecordAdapter(
    private val fuelRecords: MutableList<FuelRecord>,
    private val fragmentManager: FragmentManager,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<FuelRecordAdapter.FuelRecordViewHolder>() {

    class FuelRecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val mileageTextView: TextView = itemView.findViewById(R.id.mileageTextView)
        val litersTextView: TextView = itemView.findViewById(R.id.litersTextView)
        val consumptionTextView: TextView = itemView.findViewById(R.id.consumptionTextView)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FuelRecordViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fuel_record, parent, false)
        return FuelRecordViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FuelRecordViewHolder, position: Int) {
        val currentRecord = fuelRecords[position]
        holder.dateTextView.text = currentRecord.date
        holder.mileageTextView.text = currentRecord.mileage.toString()
        holder.litersTextView.text = currentRecord.liters.toString()
        holder.consumptionTextView.text = currentRecord.consumption.toString()
        holder.deleteButton.setOnClickListener {
            DeleteConfirmationDialog {
                onDeleteClick(position)
            }.show(fragmentManager, "DeleteConfirmationDialog")
        }
    }

    override fun getItemCount() = fuelRecords.size
}