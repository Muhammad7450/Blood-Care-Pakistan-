package com.example.data.model

import kotlin.math.*

object CityUtils {
    // Controlled standardized city list in Pakistan
    val PAKISTAN_CITIES = listOf(
        "Nawabshah",
        "Karachi",
        "Lahore",
        "Islamabad",
        "Rawalpindi",
        "Faisalabad",
        "Multan",
        "Peshawar",
        "Quetta",
        "Hyderabad",
        "Sukkur",
        "Larkana",
        "Mirpur Khas",
        "Gujranwala",
        "Sialkot",
        "Bahawalpur",
        "Sargodha",
        "Abbottabad",
        "Mardan",
        "Muzaffarabad",
        "Gilgit"
    )

    val CITY_COORDINATES: Map<String, Pair<Double, Double>> = mapOf(
        "nawabshah" to Pair(26.2483, 68.4096),
        "karachi" to Pair(24.8607, 67.0011),
        "lahore" to Pair(31.5204, 74.3587),
        "islamabad" to Pair(33.6844, 73.0479),
        "rawalpindi" to Pair(33.5651, 73.0169),
        "faisalabad" to Pair(31.4504, 73.1350),
        "multan" to Pair(30.1575, 71.5249),
        "hyderabad" to Pair(25.3960, 68.3578),
        "sukkur" to Pair(27.7052, 68.8574),
        "larkana" to Pair(27.5598, 68.2120),
        "quetta" to Pair(30.1798, 66.9750),
        "peshawar" to Pair(34.0151, 71.5249),
        "gujranwala" to Pair(32.1877, 74.1945),
        "sialkot" to Pair(32.4945, 74.5229),
        "bahawalpur" to Pair(29.3544, 71.6911),
        "sargodha" to Pair(32.0836, 72.6711),
        "abbottabad" to Pair(34.1688, 73.2215),
        "mirpur khas" to Pair(25.5276, 69.0159),
        "mardan" to Pair(34.1989, 72.0450),
        "muzaffarabad" to Pair(34.3700, 73.4711),
        "gilgit" to Pair(35.9208, 74.3089)
    )

    val BLOOD_GROUPS = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

    fun getCityCoordinates(city: String): Pair<Double, Double> {
        val norm = normalizeCity(city)
        return CITY_COORDINATES[norm] ?: Pair(26.2483, 68.4096)
    }

    fun calculateHaversineDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        if (lat1 == 0.0 || lon1 == 0.0 || lat2 == 0.0 || lon2 == 0.0) return 0.0
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    /**
     * Normalizes a city name for consistent indexing and exact case-insensitive matching.
     * Trims whitespace and converts to lowercase.
     */
    fun normalizeCity(city: String): String {
        return city.trim().lowercase()
    }

    /**
     * Checks clinical blood compatibility (e.g. for donor eligibility to patient request).
     * O- is universal donor. AB+ is universal recipient.
     */
    fun isBloodCompatible(donorGroup: String, recipientGroup: String): Boolean {
        if (donorGroup == "UNKNOWN" || recipientGroup == "UNKNOWN") return true
        if (donorGroup == recipientGroup) return true
        if (donorGroup == "O-") return true // Universal donor
        return when (recipientGroup) {
            "O+" -> donorGroup == "O-"
            "A+" -> donorGroup in listOf("A-", "O+", "O-")
            "A-" -> donorGroup == "O-"
            "B+" -> donorGroup in listOf("B-", "O+", "O-")
            "B-" -> donorGroup == "O-"
            "AB+" -> true // Universal recipient
            "AB-" -> donorGroup in listOf("A-", "B-", "O-")
            else -> false
        }
    }
}
