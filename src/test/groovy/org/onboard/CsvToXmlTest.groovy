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

    @Test
    void TestGenerateXmlWithData() {
        def deptNodeList = [
                [id: 'C', deptManagerCode: 'admin', entity: 'Corporate', shortDescription: 'Corporate', locationCode: 'DE', children: [
                        [id: 'CCA', deptManagerCode: 'admin', entity: 'Corporate', shortDescription: 'Corporate Compliance & Audit', locationCode: 'DE', children: []]
                ]],
                [id: 'F', deptManagerCode: 'admin', entity: 'Corporate', shortDescription: 'Finance', locationCode: 'DE', children: []
                ]
        ]

        // When: The generateXML method is called
        def result = CsvToXml.generateXML(deptNodeList)

        def expectedXml = '''<NikuDataBus xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:noNamespaceSchemaLocation='../xsd/nikuxog_department.xsd'>
  <Header action='write' externalSource='NIKU' objectType='department' version='15.9' />
  <Departments>
    <Department department_code='C' dept_manager_code='admin' entity='Corporate' short_description='Corporate'>
      <Description>Corporate</Description>
      <LocationAssociations>
        <LocationAssociation locationcode='DE' />
      </LocationAssociations>
      <Department department_code='CCA' dept_manager_code='admin' entity='Corporate' short_description='Corporate Compliance &amp; Audit'>
        <Description>Corporate Compliance &amp; Audit</Description>
        <LocationAssociations>
          <LocationAssociation locationcode='DE' />
        </LocationAssociations>
      </Department>
    </Department>
    <Department department_code='F' dept_manager_code='admin' entity='Corporate' short_description='Finance'>
      <Description>Finance</Description>
      <LocationAssociations>
        <LocationAssociation locationcode='DE' />
      </LocationAssociations>
    </Department>
  </Departments>
</NikuDataBus>'''

        assertEquals(expectedXml, result)

    }

}