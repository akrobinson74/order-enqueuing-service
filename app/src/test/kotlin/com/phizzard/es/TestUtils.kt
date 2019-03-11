package com.phizzard.es

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.phizzard.es.models.Address

const val SAMPLE_ADDRESS = """
{
  "id": 123467890,
  "city": "Berlin",
  "federal_state": "Berlin",
  "latitude": 52.431181,
  "longitude": 13.538744,
  "street": "Am Studio",
  "street_number": "2a",
  "zip_code": 12489
}
"""
const val SAMPLE_ORDER = """
{
  "id": 123467890,
  "buyer_contact": {
    "address": {
      "id": 123467890,
      "city": "Berlin",
      "federal_state": "Berlin",
      "latitude": 52.431181,
      "longitude": 13.538744,
      "street": "Am Studio",
      "street_number": "2a",
      "zip_code": 12489
    },
    "email": "michael.mente@phizzard.com",
    "first_name": "Michael",
    "last_name": "Mente",
    "phone_number": 49123456789910
  },
  "delivery_contact": {
    "address": {
      "id": 123467890,
      "city": "Berlin",
      "federal_state": "Berlin",
      "latitude": 52.431181,
      "longitude": 13.538744,
      "street": "Am Studio",
      "street_number": "2a",
      "zip_code": 12489
    },
    "email": "michael.mente@phizzard.com",
    "first_name": "Michael",
    "last_name": "Mente",
    "phone_number": 49123456789910
  },
  "invoice_contact": {
    "address": {
      "id": 123467890,
      "city": "Berlin",
      "federal_state": "Berlin",
      "latitude": 52.431181,
      "longitude": 13.538744,
      "street": "Am Studio",
      "street_number": "2a",
      "zip_code": 12489
    },
    "email": "michael.mente@phizzard.com",
    "first_name": "Michael",
    "last_name": "Mente",
    "phone_number": 49123456789910
  },
  "order_items": [
    {
      "id": 123467890,
      "brand_name": "string",
      "product_details": {
        "id": 123467890,
        "product_attributes": [
          {
            "group": "DIMENSIONS",
            "name": "COLOR",
            "value": "peuce"
          }
        ]
      },
      "product_variant_details": {
        "currency": "EUR",
        "price_type": "sale",
        "product_variant_id": 123467890,
        "product_variant_attributes": [
          {
            "group": "DIMENSIONS",
            "name": "COLOR",
            "value": "peuce"
          }
        ],
        "store_details": {
          "contact_info": {
            "address": {
              "id": 123467890,
              "city": "Berlin",
              "federal_state": "Berlin",
              "latitude": 52.431181,
              "longitude": 13.538744,
              "street": "Am Studio",
              "street_number": "2a",
              "zip_code": 12489
            },
            "email": "michael.mente@phizzard.com",
            "first_name": "Michael",
            "last_name": "Mente",
            "phone_number": 49123456789910
          },
          "id": 123467890,
          "name": "FootLocker",
          "type": "Sporting Goods"
        }
      },
      "quantity": 2
    }
  ],
  "status": "PROCESSING",
  "store_details": {
    "contact_info": {
      "address": {
        "id": 123467890,
        "city": "Berlin",
        "federal_state": "Berlin",
        "latitude": 52.431181,
        "longitude": 13.538744,
        "street": "Am Studio",
        "street_number": "2a",
        "zip_code": 12489
      },
      "email": "michael.mente@phizzard.com",
      "first_name": "Michael",
      "last_name": "Mente",
      "phone_number": 49123456789910
    },
    "id": 123467890,
    "name": "FootLocker",
    "type": "Sporting Goods"
  },
  "uuid": "AF504EC2-4005-11E9-B16F-2436AB45578C"
}
"""

val snakeCaseMapper = ObjectMapper()
    .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
    .registerKotlinModule()
val sampleOrderJson = snakeCaseMapper.readValue<Address>(SAMPLE_ORDER)