package org.onboard

import groovy.xml.XmlSlurper
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

class CsvToXmlTest {
    @Test
    void TestGenerateHierarchyDeptWithEmptyList() {
        def testList = []
        def result = CsvToXml.generateHierarchyDept(testList)
        assertTrue(result.isEmpty())
    }

    @Test
    void TestGenerateHierarchyDept() {

        def testList = [
                ['1', 'HR', ''],
                ['2', 'Finance', ''],
                ['3', 'Payroll', '1']
        ]

        def result = CsvToXml.generateHierarchyDept(testList)

        assertEquals(2, result.size())

        def hrDept = result[0]

        assertTrue(hrDept.children.any { it.name == 'Payroll' })

    }

    @Test
    void TestGenerateXmlWithEmptyList() {

        def deptList = []

        def expectedXML = '''<?xml version='1.0' encoding='UTF-8'?>
<NikuDataBus xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="../xsd/nikuxog_department.xsd">
  <Header action="write" externalSource="NIKU" objectType="department" version="15.9"/>
  <Departments/>
</NikuDataBus>
'''
        def result = CsvToXml.generateXML(deptList)

        def expectedParsed = new XmlSlurper().parseText(expectedXML)
        def actualParsed = new XmlSlurper().parseText(result)


        def expectedNormalized = expectedParsed.toString()
        def actualNormalized = actualParsed.toString()

        assertEquals(expectedNormalized, actualNormalized)


    }

}

