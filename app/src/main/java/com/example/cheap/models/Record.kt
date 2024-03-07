package com.example.cheap.models

import kotlinx.serialization.Serializable

@Serializable
data class Record(
    val id: String?,
    val userId: String?,
    val location: Location?,
    val item: Item?
)

@Serializable
data class Location(
    val id: String?,
    val recordId: String?,
    var latitude: Double?,
    val longitude: Double?,
    val locationName: String
)

@Serializable
data class Item(
    val id: String?,
    val recordId: String?,
    val name: String,
    val description: String,
    val unitPrice: Double,
    val cost: Double,
    val quantity: Double,
    val barcode: String,
    val imageData: String?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Item

        if (id != other.id) return false
        if (recordId != other.recordId) return false
        if (name != other.name) return false
        if (description != other.description) return false
        if (unitPrice != other.unitPrice) return false
        if (cost != other.cost) return false
        if (quantity != other.quantity) return false
        if (barcode != other.barcode) return false
        return imageData.contentEquals(other.imageData)
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + recordId.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + unitPrice.hashCode()
        result = 31 * result + cost.hashCode()
        result = 31 * result + quantity.hashCode()
        result = 31 * result + barcode.hashCode()
        result = 31 * result + imageData.hashCode()
        return result
    }
}