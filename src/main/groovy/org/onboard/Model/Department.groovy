package org.onboard.Model

class Department {
    String id
    String name
    String parentDeptId
    String deptManagerCode
    String entity
    String shortDescription
    String locationCode
    List children

    Department(String id, String name, String parentDeptId, String deptManagerCode, String entity, String shortDescription, String locationCode, List children = []) {
        this.id = id
        this.name = name
        this.parentDeptId = parentDeptId
        this.deptManagerCode = deptManagerCode
        this.entity = entity
        this.shortDescription = shortDescription
        this.locationCode = locationCode
        this.children = children
    }

}



