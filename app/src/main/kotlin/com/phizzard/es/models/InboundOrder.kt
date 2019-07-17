package com.phizzard.es.models

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY
import java.math.BigInteger
import java.time.ZonedDateTime

data class Address(
    val id: BigInteger,
    val city: String,
    val country: String,
    val federalState: String,
    val street: String,
    val streetNumber: String,
    val zipCode: String
)

data class Contact(
    val address: Address,
    val email: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String
)

data class InboundOrder(
    val id: BigInteger,
    val approvalCode: String,
    val buyerContact: Contact,
    val deliveryContact: Contact? = null,
    val deliverToStore: Boolean = false,
    val invoiceContact: Contact? = null,
    val orderItems: List<OrderItem>,
    val status: String,
    val store: StoreDetails,
    val transactionDate: ZonedDateTime = ZonedDateTime.now()
)

data class OrderItem(
    val articleDescription: String,
    val currency: String,
    val grossPrice: Double,
    val gtin: String,
    val netPrice: Double? = null,
    val quantity: BigInteger,
    val supplierName: String
)

data class StoreDetails(
    val contactInfo: Contact? = null,
    val gln: String? = null,
    val storeId: String? = null,
    val name: String
)

data class ErrorBody(
    val errors: List<String>,
    @JsonInclude(NON_EMPTY) val msg: String = ""
)