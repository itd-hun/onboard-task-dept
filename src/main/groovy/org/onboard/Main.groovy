package org.onboard

static void main(String[] args) {

    def resource = getClass().getClassLoader().getResource("departments.csv")
    def filePath = resource ? resource.toURI().path : null

    String line = ""
    BufferedReader br = null;
    def department = [:]

    //file read
    try {
        br = new BufferedReader(new FileReader(filePath))
        List<List<String>> deptList = new ArrayList<>();
        while((line = br.readLine()) != null){
            List<String> values = line.split(",")
            if(values[0] != "#"){
                values.remove(0)
                deptList.add(values)
            }
        }
        deptList.remove(0)
        println(deptList)
    }catch (Exception e){
        e.printStackTrace()
    }finally {
        if(br != null){
            br.close()
        }
    }

}