package org.onboard

import groovy.xml.MarkupBuilder
import groovy.xml.XmlUtil
import org.onboard.Model.Department

class CsvToXml {

    static void main(String[] args) {
        //Load CSV File from Resource
        def resource = CsvToXml.class.classLoader.getResource("departments.csv")
        def filePath = resource ? resource.toURI().path : null

        BufferedReader br = null;

        //Read CSV File and Handle Exception
        try {
            br = new BufferedReader(new FileReader(filePath))
            List<String> departmentData = br.readLines()

            List<List<String>> deptList = departmentData.findAll { !it.startsWith("#") }.collect { eachLine ->

                if (!eachLine.isEmpty()) {
                    eachLine.split(",")[1..-1]
                } else {
                    []
                }

            }.findAll { it.size() > 0 }

            List<String> fileHeaders = deptList.remove(0)

            List<List<Department>> hierarchyDeptList = generateHierarchyDept(deptList)

            String xmlString = generateXML(hierarchyDeptList)

            def formattedXml = XmlUtil.serialize(xmlString)

            String resultPath = "src/main/resources/departments.xml"

            def file = new File(resultPath)
            file.parentFile.mkdirs()
            file.write(formattedXml)

        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            if (br != null) {
                br.close()
            }
        }

    }

    //Generates and returns Hierarchy department list
    static List<List<Department>> generateHierarchyDept(List<List<String>> deptList) {
        def departmentMap = [:]

        deptList.forEach { eachDept ->
            {
                def deptId = eachDept[0]
                def departmentName = eachDept[1]
                def parentDeptId = eachDept[2] ?: ""

                departmentMap[deptId] =  new Department(deptId, departmentName, parentDeptId, "admin", "Corporate", departmentName, "DE", [])
            }
        }

        departmentMap.forEach { _, eachDept ->
            {
                def parentDeptId = eachDept.parentDeptId
                if (parentDeptId) {
                    departmentMap[parentDeptId].children.add(eachDept)
                }

            }
        }

        return departmentMap.values().findAll { it.parentDeptId == '' } as List<List<Department>>
    }

    //Generates XML from Hierarchy list
    static String generateXML(List<List<Map<String, Object>>> deptNodeList) {
        def writer = new StringWriter()
        MarkupBuilder xml = new MarkupBuilder(writer)

        xml.NikuDataBus('xmlns:xsi': 'http://www.w3.org/2001/XMLSchema-instance',
                'xsi:noNamespaceSchemaLocation': '../xsd/nikuxog_department.xsd') {

            Header(action: 'write', externalSource: 'NIKU', objectType: 'department', version: '15.9')

            Departments {

                deptNodeList.forEach { eachDept -> generateDepartment(xml, eachDept) }

            }
        }

        return writer.toString()
    }

    //Recursively generates department xml
    static void generateDepartment(xml, dept) {
        xml.Department(department_code: dept.id,
                dept_manager_code: dept.deptManagerCode,
                entity: dept.entity,
                short_description: dept.shortDescription) {
            Description(dept.shortDescription)

            if (dept.locationCode) {
                LocationAssociations {
                    LocationAssociation(locationcode: dept.locationCode)
                }
            }

            if (dept.children && dept.children.size() > 0) {
                dept.children.forEach { eachChild ->
                    {
                        generateDepartment(xml, eachChild)
                    }
                }
            }
        }
    }
}