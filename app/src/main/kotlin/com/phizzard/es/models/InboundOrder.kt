package com.phizzard.es.models

import java.math.BigInteger

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
    val federalState: String,
    val latitude: String,
    val longitude: String,
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
    val buyerContact: Contact,
    val deliverContact: Contact,
    val invoiceContact: Contact,
    val orderItems: List<OrderItem>,
    val status: String,
    val storeDetails: StoreDetails,
    val uuid: String
)

data class OrderItem(
    val id: BigInteger,
    val brandName: String,
    val productDetails: List<ProductDetails>,
    val productVariantDetails: List<ProductVariantDetails>,
    val quantity: BigInteger
)

data class ProductAttribute(
    val group: String,
    val name: String,
    val value: String
)

data class ProductDetails(
    val id: BigInteger,
    val productAttributes: List<ProductAttribute>
)

data class ProductVariantDetails(
    val currency: String,
    val priceType: String,
    val productVariantId: BigInteger,
    val productVariantAttributes: List<ProductAttribute>,
    val storeDetails: StoreDetails
)

data class StoreDetails(
    val contactInfo: Contact,
    val name: String,
    val type: String
)