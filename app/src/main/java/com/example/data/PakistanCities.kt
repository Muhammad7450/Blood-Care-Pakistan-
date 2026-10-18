package com.example.data

import kotlin.math.*

object PakistanCities {
    data class CityInfo(val name: String, val province: String, val lat: Double, val lon: Double)

    val list = listOf(
        CityInfo("Lahore", "Punjab", 31.5204, 74.3587),
        CityInfo("Karachi", "Sindh", 24.8607, 67.0011),
        CityInfo("Islamabad", "ICT", 33.6844, 73.0479),
        CityInfo("Rawalpindi", "Punjab", 33.5651, 73.0169),
        CityInfo("Faisalabad", "Punjab", 31.4504, 73.1350),
        CityInfo("Multan", "Punjab", 30.1575, 71.5249),
        CityInfo("Peshawar", "KPK", 34.0151, 71.5249),
        CityInfo("Quetta", "Balochistan", 30.1798, 66.9750),
        CityInfo("Gujranwala", "Punjab", 32.1877, 74.1945),
        CityInfo("Sialkot", "Punjab", 32.4945, 74.5229),
        CityInfo("Hyderabad", "Sindh", 25.3960, 68.3578),
        CityInfo("Bahawalpur", "Punjab", 29.3544, 71.6911),
        CityInfo("Sargodha", "Punjab", 32.0836, 72.6711),
        CityInfo("Abbottabad", "KPK", 34.1688, 73.2215),
        CityInfo("Sukkur", "Sindh", 27.7052, 68.8574),
        CityInfo("Sheikhupura", "Punjab", 31.7131, 73.9783)
    )

    fun getCityNames(): List<String> = list.map { it.name }

    fun calculateDistanceKm(city1: String, city2: String): Double {
        if (city1.equals(city2, ignoreCase = true)) {
            return 2.5 // Inside same city default hospital distance
        }
        val c1 = list.find { it.name.equals(city1, ignoreCase = true) } ?: return 12.0
        val c2 = list.find { it.name.equals(city2, ignoreCase = true) } ?: return 12.0

        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(c2.lat - c1.lat)
        val dLon = Math.toRadians(c2.lon - c1.lon)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(c1.lat)) * cos(Math.toRadians(c2.lat)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val dist = r * c
        return (dist * 10).roundToInt() / 10.0
    }
}
