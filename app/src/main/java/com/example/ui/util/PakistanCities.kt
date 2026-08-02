package com.example.ui.util

object PakistanCities {
    val ALL_CITIES = listOf(
        "Lahore",
        "Karachi",
        "Islamabad",
        "Rawalpindi",
        "Peshawar",
        "Quetta",
        "Multan",
        "Faisalabad",
        "Gujranwala",
        "Sialkot",
        "Hyderabad",
        "Sukkur",
        "Bahawalpur",
        "Sargodha",
        "Abbottabad",
        "Mardan",
        "Mirpur (AJK)",
        "Muzaffarabad",
        "Gilgit",
        "Gwadar",
        "Rahim Yar Khan",
        "Sahiwal",
        "Okara",
        "Larkana",
        "Chiniot",
        "Jhelum",
        "Sheikhupura",
        "Dera Ghazi Khan",
        "Kasur",
        "Nawabshah"
    )

    private val CITY_COORDS = mapOf(
        "Lahore" to Pair(31.5204, 74.3587),
        "Karachi" to Pair(24.8607, 67.0011),
        "Islamabad" to Pair(33.6844, 73.0479),
        "Rawalpindi" to Pair(33.5989, 73.0441),
        "Peshawar" to Pair(34.0151, 71.5249),
        "Quetta" to Pair(30.1798, 66.9750),
        "Multan" to Pair(30.1575, 71.5249),
        "Faisalabad" to Pair(31.4504, 73.1350),
        "Gujranwala" to Pair(32.1877, 74.1945),
        "Sialkot" to Pair(32.4945, 74.5229),
        "Hyderabad" to Pair(25.3960, 68.3578),
        "Sukkur" to Pair(27.7244, 68.8228),
        "Bahawalpur" to Pair(29.3544, 71.6911),
        "Sargodha" to Pair(32.0836, 72.6711),
        "Abbottabad" to Pair(34.1688, 73.2215),
        "Mardan" to Pair(34.1986, 72.0404),
        "Mirpur (AJK)" to Pair(33.1484, 73.7519),
        "Muzaffarabad" to Pair(34.3700, 73.4711),
        "Gilgit" to Pair(35.9208, 74.3089),
        "Gwadar" to Pair(25.1264, 62.3225),
        "Rahim Yar Khan" to Pair(28.4212, 70.2989),
        "Sahiwal" to Pair(30.6682, 73.1114),
        "Okara" to Pair(30.8100, 73.4500),
        "Larkana" to Pair(27.5589, 68.2120),
        "Chiniot" to Pair(31.7200, 72.9800),
        "Jhelum" to Pair(32.9405, 73.7276),
        "Sheikhupura" to Pair(31.7131, 73.9783),
        "Dera Ghazi Khan" to Pair(30.0561, 70.6348),
        "Kasur" to Pair(31.1167, 74.4500),
        "Nawabshah" to Pair(26.2442, 68.4100)
    )

    fun getCityCoords(city: String): Pair<Double, Double> {
        return CITY_COORDS[city] ?: Pair(31.5204, 74.3587)
    }

    fun calculateDistanceKm(city1: String, city2: String): Double {
        if (city1.equals(city2, ignoreCase = true)) {
            return 1.8 // Local city area distance
        }
        val c1 = CITY_COORDS[city1] ?: Pair(31.5204, 74.3587)
        val c2 = CITY_COORDS[city2] ?: Pair(31.5204, 74.3587)
        return calculateHaversineKm(c1.first, c1.second, c2.first, c2.second)
    }

    fun calculateHaversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val dist = r * c
        return Math.round(dist * 10.0) / 10.0
    }
}
