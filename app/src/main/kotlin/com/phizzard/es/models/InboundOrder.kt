package com.phizzard.es.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY
import com.phizzard.models.OrderStatus
import com.phizzard.models.PartnerPlatform
import org.bson.types.ObjectId
import java.math.BigInteger
import java.time.Instant

@JsonIgnoreProperties(ignoreUnknown = true)
data class Address(
    val city: String,
    val country: String,
    val street: String,
    val streetNumber: String,
    val zipCode: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Contact(
    val address: Address? = null,
    val companyName: String? = null,
    val email: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val phoneNumber: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class InboundOrder(
    val id: BigInteger,
    val approvalCode: String,
    val buyerContact: Contact,
    val deliveryContact: Contact? = null,
    val deliverToStore: Boolean = false,
    val invoiceContact: Contact? = null,
    val orderItems: List<OrderItem>,
    val status: OrderStatus = OrderStatus.INITIAL,
    val store: StoreDetails,
    val transactionDate: Instant = Instant.now()
)

data class StatusUpdate(
    val status: OrderStatus
)
@JsonIgnoreProperties(ignoreUnknown = true)
data class StoredOrder(
    val _id: ObjectId? = null,
    val id: BigInteger? = BigInteger.valueOf(Instant.now().epochSecond),
    val approvalCode: String,
    val buyerContact: Contact,
    val deliveryContact: Contact? = null,
    val deliverToStore: Boolean = false,
    val invoiceContact: Contact? = null,
    val orderItems: List<OrderItem>,
    val status: OrderStatus = OrderStatus.INITIAL,
    val store: StoreDetails,
    val transactionDate: Instant = Instant.now(),
    val userId: PartnerPlatform? = PartnerPlatform.QB
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrderItem(
    val articleDescription: String,
    val currency: String,
    val grossPrice: Double,
    val gtin: String,
    val netPrice: Double? = null,
    val quantity: BigInteger,
    val supplierName: String
)

data class OrderItemPatch(
    val action: OrderItemAction = OrderItemAction.CANCEL,
    val item: OrderItem?,
    val itemIndex: Int?
)

enum class OrderItemAction {
    CANCEL, RETURN
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class StoreDetails(
    val contactInfo: Contact? = null,
    val gln: String? = null,
    val storeId: String? = null,
    val name: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ErrorBody(
    val errors: List<String>,
    @JsonInclude(NON_EMPTY) val msg: String = ""
)