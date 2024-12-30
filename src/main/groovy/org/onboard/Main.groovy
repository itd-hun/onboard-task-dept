package org.onboard

import groovy.xml.MarkupBuilder
import groovy.xml.XmlUtil
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.onboard.Model.Department


class CsvToXml {

    private static final Logger logger = LogManager.getLogger(CsvToXml)

    static void main(String[] args) {

        logger.info("Application started")

        //Load CSV File from Resource
        def resource = CsvToXml.class.classLoader.getResource("departments.csv")
        def filePath = resource ? resource.toURI().path : null

        if (!filePath) {
            logger.fatal("Required resource file is missing. Can't process further")

            throw new RuntimeException("Critical error: 'departments.csv' is required but not found.")
        }

        logger.info("Required file found, processing further.")

        BufferedReader br = null;

        //Read CSV File and Handle Exception
        try {
            logger.debug("Started reading the file")
            br = new BufferedReader(new FileReader(filePath))
            List<String> departmentData = br.readLines()

            List<List<String>> deptList = departmentData.findAll { !it.startsWith("#") }.collect { eachLine ->

                if (!eachLine.isEmpty()) {

                    def line = eachLine.split(",").findAll { it?.trim() }
                    return line
//
                } else {
                    []
                }

            }.findAll { it.size() > 0 }

            List<String> fileHeaders = deptList.remove(0)

            logger.info("Successfully converted into base list")

            List<List<Department>> hierarchyDeptList = generateHierarchyDept(deptList)

            logger.info("Successfully converted into hierarchy list")

            String xmlString = generateXML(hierarchyDeptList)

            def formattedXml = XmlUtil.serialize(xmlString)

            String resultPath = "src/main/resources/departments.xml"

            def file = new File(resultPath)
            file.parentFile.mkdirs()
            file.write(formattedXml)

            logger.debug("Successfully generated XML file from CSV")

        } catch (FileNotFoundException e) {
            logger.error("File not found: ${e.message}")
        } catch (IOException e) {
            logger.error("I/O Error: ${e.message}")
        } catch (SecurityException e) {
            logger.error("Security exception: ${e.message}")
        } catch (Exception e) {
            logger.error("Unexpected error: ${e.message}")
        } finally {
            if (br != null) {
                br.close()
                logger.info("Closing the file reader")
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

                departmentMap[deptId] = new Department(deptId, departmentName, parentDeptId, "admin", "Corporate", departmentName, "DE", [])
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

        logger.info("Started writing XML")

        xml.NikuDataBus('xmlns:xsi': 'http://www.w3.org/2001/XMLSchema-instance',
                'xsi:noNamespaceSchemaLocation': '../xsd/nikuxog_department.xsd') {

            Header(action: 'write', externalSource: 'NIKU', objectType: 'department', version: '15.9')

            Departments {

                deptNodeList.forEach { eachDept -> generateDepartment(xml, eachDept) }

            }
        }

        return writer.toString()
    }

    //Recursively generates child department xml
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