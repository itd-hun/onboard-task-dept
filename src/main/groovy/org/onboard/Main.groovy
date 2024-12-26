package org.onboard
import groovy.xml.MarkupBuilder

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

            List<List<String>> deptList = departmentData.findAll { !it.startsWith("#") }.collect { it.split(",")[1..-1] }
            List<String> fileHeaders = deptList.remove(0)

            List<Map<String, String>> deptMap = []

            def hierarchyDeptList = buildHierarchyDept(deptList)

            println("Initial Hierarchy Data")
            println(hierarchyDeptList)

            String xmlString = generateXML(hierarchyDeptList)

        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            if (br != null) {
                br.close()
            }
        }

    }

    static List<List<Map<String, Object>>> buildHierarchyDept(List<List<String>> deptList) {
        def departmentMap = [:]

        deptList.forEach { eachDept ->
            {
                def deptId = eachDept[0]
                def departmentName = eachDept[1]
                def parentDeptId = eachDept[2] ?: ""

                departmentMap[deptId] = [id: deptId, name: departmentName, parentDeptId: parentDeptId, deptManagerCode: "admin", entity: "Corporate", shortDescription: departmentName, locationCode: "DE", children: []]
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

        return departmentMap.values().findAll { it.parentDeptId == '' } as List<List<Map<String, Object>>>
    }

    static String generateXML(List<List<Map<String, Object>>> deptNodeList){
        def writer = new StringWriter()
        MarkupBuilder xml = new MarkupBuilder(writer)
        return ""
    }
}