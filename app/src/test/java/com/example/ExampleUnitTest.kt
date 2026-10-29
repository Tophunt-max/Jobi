package com.example

import com.example.model.AppLanguage
import com.example.model.CasteCategory
import com.example.model.LanguageRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testLanguageRegistryMultiLingualCoverage() {
        // Test key translations across supported Indian languages
        assertEquals("Sarkari Jobs", LanguageRegistry.getString("app_title", AppLanguage.ENGLISH))
        assertEquals("सरकारी नौकरी", LanguageRegistry.getString("app_title", AppLanguage.HINDI))
        assertEquals("ସରକାରୀ ଚାକିରି", LanguageRegistry.getString("app_title", AppLanguage.ODIA))
        assertEquals("সরকারি চাকরি", LanguageRegistry.getString("app_title", AppLanguage.BENGALI))
        assertEquals("அரசு வேலைகள்", LanguageRegistry.getString("app_title", AppLanguage.TAMIL))
        assertEquals("ప్రభుత్వ ఉద్యోగాలు", LanguageRegistry.getString("app_title", AppLanguage.TELUGU))
        assertEquals("સરકારી નોકરી", LanguageRegistry.getString("app_title", AppLanguage.GUJARATI))
        assertEquals("ਸਰਕਾਰੀ ਨੌਕਰੀਆਂ", LanguageRegistry.getString("app_title", AppLanguage.PUNJABI))
        assertEquals("سرکاری نوکریاں", LanguageRegistry.getString("app_title", AppLanguage.URDU))
        assertEquals("ಸರ್ಕಾರಿ ಉದ್ಯೋಗಗಳು", LanguageRegistry.getString("app_title", AppLanguage.KANNADA))
        assertEquals("സർക്കാർ ജോലികൾ", LanguageRegistry.getString("app_title", AppLanguage.MALAYALAM))
        assertEquals("চৰকাৰী চাকৰি", LanguageRegistry.getString("app_title", AppLanguage.ASSAMESE))
    }

    @Test
    fun testCasteCategoryAgeRelaxations() {
        assertEquals(0, CasteCategory.GENERAL.standardRelaxationYears)
        assertEquals(0, CasteCategory.EWS.standardRelaxationYears)
        assertEquals(3, CasteCategory.OBC_NCL.standardRelaxationYears)
        assertEquals(5, CasteCategory.SC.standardRelaxationYears)
        assertEquals(5, CasteCategory.ST.standardRelaxationYears)
        assertEquals(10, CasteCategory.PWD_GENERAL.standardRelaxationYears)
        assertEquals(13, CasteCategory.PWD_OBC.standardRelaxationYears)
        assertEquals(15, CasteCategory.PWD_SC_ST.standardRelaxationYears)
    }

    @Test
    fun testLanguageRegistryCodes() {
        assertEquals(13, AppLanguage.entries.size)
        assertTrue(AppLanguage.URDU.isRtl)
        assertEquals(AppLanguage.HINDI, AppLanguage.fromCode("hi"))
        assertEquals(AppLanguage.ODIA, AppLanguage.fromCode("or"))
    }

    @Test
    fun testGovernmentJobListingsDataFields() {
        val seedJobs = com.example.data.sample.InitialDataSeed.initialJobs
        assertTrue("Seed jobs should not be empty", seedJobs.isNotEmpty())
        for (job in seedJobs) {
            assertTrue("Job title must not be blank", job.title.isNotBlank())
            assertTrue("Organization must not be blank", job.organization.isNotBlank())
            assertTrue("Application deadline (closing date) must not be blank", job.applicationClosingDate.isNotBlank())
            assertTrue("Total vacancies must be positive", job.totalVacancies > 0)
        }
    }

    @Test
    fun testFilterJobsByOrganization() {
        val seedJobs = com.example.data.sample.InitialDataSeed.initialJobs
        val upscJobs = seedJobs.filter { it.organization.contains("UPSC", ignoreCase = true) }
        assertTrue("Should contain UPSC recruitment", upscJobs.isNotEmpty())
        assertEquals("UPSC", upscJobs.first().category)

        val sscJobs = seedJobs.filter { it.organization.contains("SSC", ignoreCase = true) }
        assertTrue("Should contain SSC recruitment", sscJobs.isNotEmpty())

        val railwayJobs = seedJobs.filter { it.organization.contains("Railway", ignoreCase = true) }
        assertTrue("Should contain Railway recruitment", railwayJobs.isNotEmpty())
    }

    @Test
    fun testFilterByCategoryEntities() {
        val seedJobs = com.example.data.sample.InitialDataSeed.initialJobs
        val admitCards = com.example.data.sample.InitialDataSeed.initialAdmitCards
        val results = com.example.data.sample.InitialDataSeed.initialResults

        assertTrue("New jobs exist", seedJobs.isNotEmpty())
        assertTrue("Admit cards exist", admitCards.isNotEmpty())
        assertTrue("Results exist", results.isNotEmpty())

        // Validate admit card release & exam dates
        val firstCard = admitCards.first()
        assertTrue(firstCard.examDate.isNotBlank())
        assertTrue(firstCard.releaseDate.isNotBlank())

        // Validate results cut-off summary
        val firstResult = results.first()
        assertTrue(firstResult.cutOffSummary.isNotBlank())
        assertTrue(firstResult.publicationDate.isNotBlank())
    }

    @Test
    fun testFilterJobsByAdmitCardAndResultCategory() {
        val allJobs = com.example.data.sample.InitialDataSeed.initialJobs

        val admitCardJobs = allJobs.filter {
            it.category.equals("Admit Card", ignoreCase = true) ||
                it.title.contains("Admit Card", ignoreCase = true)
        }
        assertTrue("Admit Card jobs must be present", admitCardJobs.isNotEmpty())
        for (job in admitCardJobs) {
            assertTrue(
                "Job must be Admit Card category or contain Admit Card in title",
                job.category.equals("Admit Card", ignoreCase = true) || job.title.contains("Admit Card", ignoreCase = true)
            )
        }

        val resultJobs = allJobs.filter {
            it.category.equals("Result", ignoreCase = true) ||
                it.title.contains("Result", ignoreCase = true)
        }
        assertTrue("Result jobs must be present", resultJobs.isNotEmpty())
        for (job in resultJobs) {
            assertTrue(
                "Job must be Result category or contain Result in title",
                job.category.equals("Result", ignoreCase = true) || job.title.contains("Result", ignoreCase = true)
            )
        }
    }
}


