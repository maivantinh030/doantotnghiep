import com.park.entities.MomoTransaction
import kotlinx.serialization.Serializable

@Serializable
data class MomoTransactionDTO(
    val orderId: String,
    val userId: String,
    val amount: String,
    val status: String,
    val description: String,
    val momoTransId: String?,
    val qrData: String?,
    val qrCodeUrl: String?,
    val createdAt: String,
    val completedAt: String?
) {
    companion object {
        fun fromEntity(entity: MomoTransaction, qrCodeUrl: String? = null): MomoTransactionDTO {
            return MomoTransactionDTO(
                orderId = entity.orderId,
                userId = entity.userId,
                amount = entity.amount.toString(),
                status = entity.status,
                description = entity.description,
                momoTransId = entity.momoTransId,
                qrData = entity.qrData,
                qrCodeUrl = qrCodeUrl,
                createdAt = entity.createdAt.toString(),
                completedAt = entity.completedAt?.toString()
            )
        }
    }
}