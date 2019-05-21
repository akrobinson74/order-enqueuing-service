package com.phizzard.es.models

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY
import java.math.BigDecimal
import java.math.BigInteger
import java.time.ZonedDateTime

enum class AttributeGroup {
    APPEARANCE, DIMENSIONS, FLAGS, MISCELLANEOUS;
}

enum class AttributeName {
    ARM_LENGTH,
    CALF_HEIGHT,
    CALF_WIDTH,
    COLLAR,
    COLOR,
    COMFORT_WIDTH,
    CUT,
    DESIGN,
    HEEL,
    INNER_MATERIAL,
    IS_NEW,
    IS_PROMO,
    MATERIAL,
    ON_SALE,
    PRODUCT_CATEGORY,
    PRODUCT_LINE,
    REPLACEABLE_FOOTBED,
    SEASON,
    SEX,
    SHAPE,
    SIGNATURE,
    SIZE,
    SIZE_RANGE,
    SOLE_MATERIAL,
    TOECAP,
    TRANSLATION_KEY,
    WATERPROOF,
    WIDTH;
}

data class Address(
    val id: BigInteger,
    val city: String,
    val country: String,
    val federalState: String,
    val latitude: BigDecimal? = null,
    val longitude: BigDecimal? = null,
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
    val invoiceContact: Contact? = null,
    val customerDelivery: Boolean = true,
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