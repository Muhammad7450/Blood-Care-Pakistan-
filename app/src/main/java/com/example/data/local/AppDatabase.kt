package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.BloodCampEntity
import com.example.data.model.BloodRequestEntity
import com.example.data.model.DonationRecordEntity
import com.example.data.model.DonorEntity
import com.example.data.model.HospitalEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        DonorEntity::class,
        BloodRequestEntity::class,
        HospitalEntity::class,
        DonationRecordEntity::class,
        BloodCampEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun donorDao(): DonorDao
    abstract fun bloodRequestDao(): BloodRequestDao
    abstract fun hospitalDao(): HospitalDao
    abstract fun donationRecordDao(): DonationRecordDao
    abstract fun bloodCampDao(): BloodCampDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bloodcare_pakistan_db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }

            suspend fun populateInitialData(database: AppDatabase) {
                // Initial Donors across major Pakistani cities
                val donorDao = database.donorDao()
                val donors = listOf(
                    DonorEntity(
                        fullName = "Muhammad Ali Khan",
                        phone = "+92 300 1234567",
                        bloodGroup = "O+",
                        age = 28,
                        gender = "Male",
                        city = "Lahore",
                        locationName = "Gulberg III, Lahore",
                        latitude = 31.5204,
                        longitude = 74.3587,
                        isAvailable = true,
                        lastDonationDate = "2026-04-15",
                        totalDonations = 8,
                        isVerified = true,
                        badges = "Hero Donor,Life Saver,Golden Heart"
                    ),
                    DonorEntity(
                        fullName = "Zainab Bibi",
                        phone = "+92 321 9876543",
                        bloodGroup = "B+",
                        age = 25,
                        gender = "Female",
                        city = "Karachi",
                        locationName = "Clifton Block 5, Karachi",
                        latitude = 24.8607,
                        longitude = 67.0011,
                        isAvailable = true,
                        lastDonationDate = "2026-03-20",
                        totalDonations = 5,
                        isVerified = true,
                        badges = "Hero Donor,Thalassemia Guardian"
                    ),
                    DonorEntity(
                        fullName = "Usman Tariq",
                        phone = "+92 333 4567890",
                        bloodGroup = "O-",
                        age = 32,
                        gender = "Male",
                        city = "Islamabad",
                        locationName = "F-7 Markaz, Islamabad",
                        latitude = 33.6844,
                        longitude = 73.0479,
                        isAvailable = true,
                        lastDonationDate = "2026-05-10",
                        totalDonations = 12,
                        isVerified = true,
                        badges = "Universal Hero,Life Saver,Golden Heart"
                    ),
                    DonorEntity(
                        fullName = "Ayesha Malik",
                        phone = "+92 302 5551234",
                        bloodGroup = "A+",
                        age = 29,
                        gender = "Female",
                        city = "Rawalpindi",
                        locationName = "Saddar, Rawalpindi",
                        latitude = 33.5989,
                        longitude = 73.0441,
                        isAvailable = true,
                        lastDonationDate = "2026-02-14",
                        totalDonations = 6,
                        isVerified = true,
                        badges = "Hero Donor"
                    ),
                    DonorEntity(
                        fullName = "Hamza Abbasi",
                        phone = "+92 345 7778899",
                        bloodGroup = "AB+",
                        age = 26,
                        gender = "Male",
                        city = "Peshawar",
                        locationName = "University Town, Peshawar",
                        latitude = 34.0151,
                        longitude = 71.5249,
                        isAvailable = true,
                        lastDonationDate = "2026-01-05",
                        totalDonations = 4,
                        isVerified = true,
                        badges = "Life Saver"
                    ),
                    DonorEntity(
                        fullName = "Fatima Shah",
                        phone = "+92 313 1112233",
                        bloodGroup = "A-",
                        age = 24,
                        gender = "Female",
                        city = "Faisalabad",
                        locationName = "Peoples Colony, Faisalabad",
                        latitude = 31.4504,
                        longitude = 73.1350,
                        isAvailable = false,
                        lastDonationDate = "2026-06-25",
                        totalDonations = 3,
                        isVerified = true,
                        badges = "Hero Donor"
                    ),
                    DonorEntity(
                        fullName = "Bilal Ahmed",
                        phone = "+92 301 8889900",
                        bloodGroup = "B-",
                        age = 30,
                        gender = "Male",
                        city = "Multan",
                        locationName = "Cantt, Multan",
                        latitude = 30.1575,
                        longitude = 71.5249,
                        isAvailable = true,
                        lastDonationDate = "2026-03-01",
                        totalDonations = 9,
                        isVerified = true,
                        badges = "Life Saver,Hero Donor"
                    ),
                    DonorEntity(
                        fullName = "Sana Rehman",
                        phone = "+92 306 4443322",
                        bloodGroup = "AB-",
                        age = 27,
                        gender = "Female",
                        city = "Quetta",
                        locationName = "Jinnah Road, Quetta",
                        latitude = 30.1798,
                        longitude = 66.9750,
                        isAvailable = true,
                        lastDonationDate = "2025-11-12",
                        totalDonations = 2,
                        isVerified = true,
                        badges = "Hero Donor"
                    )
                )
                donors.forEach { donorDao.insertDonor(it) }

                // Initial Blood Requests
                val requestDao = database.bloodRequestDao()
                val requests = listOf(
                    BloodRequestEntity(
                        patientName = "Hassan Raza",
                        bloodGroup = "O-",
                        units = 2,
                        hospitalName = "Services Hospital Lahore",
                        city = "Lahore",
                        locationName = "Jail Road, Lahore",
                        emergencyLevel = "Critical",
                        requiredDateTime = "Today, 10:00 PM",
                        contactPhone = "+92 300 9988776",
                        notes = "Urgent road traffic accident surgery. Universal donor O negative needed immediately.",
                        status = "Pending",
                        isThalassemia = false
                    ),
                    BloodRequestEntity(
                        patientName = "Little Ahmed (6 yrs)",
                        bloodGroup = "B+",
                        units = 1,
                        hospitalName = "Indus Hospital Karachi",
                        city = "Karachi",
                        locationName = "Korangi, Karachi",
                        emergencyLevel = "Urgent",
                        requiredDateTime = "Tomorrow, 9:00 AM",
                        contactPhone = "+92 321 4433221",
                        notes = "Monthly routine blood transfusion required for Thalassemia child warrior.",
                        status = "Pending",
                        isThalassemia = true
                    ),
                    BloodRequestEntity(
                        patientName = "Mariam Sheikh",
                        bloodGroup = "A+",
                        units = 3,
                        hospitalName = "PIMS Hospital Islamabad",
                        city = "Islamabad",
                        locationName = "G-8/3, Islamabad",
                        emergencyLevel = "Urgent",
                        requiredDateTime = "Today, 6:00 PM",
                        contactPhone = "+92 333 1122334",
                        notes = "Required for cardiac bypass operation.",
                        status = "Pending",
                        isThalassemia = false
                    ),
                    BloodRequestEntity(
                        patientName = "Sohail Anwar",
                        bloodGroup = "AB+",
                        units = 1,
                        hospitalName = "Lady Reading Hospital Peshawar",
                        city = "Peshawar",
                        locationName = "Soekarno Rd, Peshawar",
                        emergencyLevel = "Normal",
                        requiredDateTime = "Aug 2, 2026",
                        contactPhone = "+92 345 6677889",
                        notes = "Scheduled orthopedic procedure.",
                        status = "Matched",
                        isThalassemia = false
                    )
                )
                requests.forEach { requestDao.insertRequest(it) }

                // Initial Hospitals / Blood Banks
                val hospitalDao = database.hospitalDao()
                val hospitals = listOf(
                    HospitalEntity(
                        name = "Shaukat Khanum Memorial Hospital",
                        city = "Lahore",
                        address = "Johar Town, Lahore",
                        phone = "+92 42 35905000",
                        isVerified = true,
                        aPosUnits = 15, aNegUnits = 3, bPosUnits = 20, bNegUnits = 2,
                        abPosUnits = 6, abNegUnits = 1, oPosUnits = 30, oNegUnits = 4
                    ),
                    HospitalEntity(
                        name = "Aga Khan University Hospital",
                        city = "Karachi",
                        address = "Stadium Road, Karachi",
                        phone = "+92 21 34930051",
                        isVerified = true,
                        aPosUnits = 22, aNegUnits = 5, bPosUnits = 28, bNegUnits = 4,
                        abPosUnits = 10, abNegUnits = 3, oPosUnits = 40, oNegUnits = 8
                    ),
                    HospitalEntity(
                        name = "PIMS Hospital & Blood Bank",
                        city = "Islamabad",
                        address = "Sector G-8/3, Islamabad",
                        phone = "+92 51 9261170",
                        isVerified = true,
                        aPosUnits = 10, aNegUnits = 2, bPosUnits = 14, bNegUnits = 1,
                        abPosUnits = 5, abNegUnits = 1, oPosUnits = 18, oNegUnits = 3
                    ),
                    HospitalEntity(
                        name = "Fatimid Foundation Blood Bank",
                        city = "Peshawar",
                        address = "Peshawar Cantt",
                        phone = "+92 91 5278899",
                        isVerified = true,
                        aPosUnits = 18, aNegUnits = 4, bPosUnits = 25, bNegUnits = 6,
                        abPosUnits = 8, abNegUnits = 2, oPosUnits = 35, oNegUnits = 5
                    )
                )
                hospitals.forEach { hospitalDao.insertHospital(it) }

                // Initial Blood Camps / Events
                val campDao = database.bloodCampDao()
                val camps = listOf(
                    BloodCampEntity(
                        title = "Mega Independence Day Blood Drive",
                        organizer = "Pakistan Red Crescent Society",
                        city = "Lahore",
                        venue = "Liberty Roundabout Park, Lahore",
                        date = "Aug 14, 2026",
                        time = "10:00 AM - 6:00 PM",
                        contact = "+92 300 8877665",
                        registeredCount = 42
                    ),
                    BloodCampEntity(
                        title = "Thalassemia Awareness & Donation Camp",
                        organizer = "Sundas Foundation",
                        city = "Islamabad",
                        venue = "Centaurus Mall Ground Floor, Islamabad",
                        date = "Aug 8, 2026",
                        time = "11:00 AM - 8:00 PM",
                        contact = "+92 51 5544332",
                        registeredCount = 29
                    )
                )
                camps.forEach { campDao.insertCamp(it) }

                // Initial Donation History
                val recordDao = database.donationRecordDao()
                val records = listOf(
                    DonationRecordEntity(
                        donorId = 1,
                        donorName = "Muhammad Ali Khan",
                        patientName = "Saima Bibi",
                        hospitalName = "Services Hospital Lahore",
                        bloodGroup = "O+",
                        unitsDonated = 1,
                        donationDate = "2026-04-15",
                        certificateCode = "BCP-2026-9812"
                    ),
                    DonationRecordEntity(
                        donorId = 1,
                        donorName = "Muhammad Ali Khan",
                        patientName = "Thalassemia Child Unit",
                        hospitalName = "Mayo Hospital Lahore",
                        bloodGroup = "O+",
                        unitsDonated = 1,
                        donationDate = "2026-01-10",
                        certificateCode = "BCP-2026-3419"
                    )
                )
                records.forEach { recordDao.insertRecord(it) }
            }
        }
    }
}
