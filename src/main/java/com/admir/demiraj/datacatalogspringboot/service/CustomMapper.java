package com.admir.demiraj.datacatalogspringboot.service;

import com.admir.demiraj.datacatalogspringboot.dao.HospitalDAO;
import com.admir.demiraj.datacatalogspringboot.dao.VariableDAO;
import com.admir.demiraj.datacatalogspringboot.dao.VersionDAO;
import com.admir.demiraj.datacatalogspringboot.resources.Hospitals;
import com.admir.demiraj.datacatalogspringboot.resources.Variables;
import com.admir.demiraj.datacatalogspringboot.resources.Versions;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CustomMapper {

    @Autowired
    HospitalDAO hospitalDAO;

    @Autowired
    VersionDAO versionDAO;

    @Autowired
    UploadVariables uploadVariables;

    @Autowired
    private VariablesXLSX_JSON variablesXLSX_json;

    @Autowired
    private VariableDAO variableDAO;







    public void mapVersion(JSONArray jr) {
        String hospitalName = jr.getString(0);
        String versionName = jr.getString(1);
        JSONObject version = jr.getJSONObject(2);

        Hospitals currentHospital = hospitalDAO.getHospitalByName(hospitalName);
        //The hospital exists
        if (currentHospital != null) {
            System.out.println("The hospital exists");
            //The version is present at hospital
            if (versionDAO.isVersionNameInHospital(versionName, hospitalName)) {
                // NOTE !! PERHAPS WE WANT TO UPDATE A VERSION THAT ALREADY EXISTS
                System.out.println("The version : " + versionName + " is already present at : " + hospitalName + " and won't be saved");
                //The version isn't present at hospital
            } else {
                createVersion(versionName, currentHospital, version);
            }


            //The hospital doesn't exist
        } else {
            System.out.println("The hospital was not found!");

        }
       // System.out.println("Received new mappingfunctions" + mappingArray);
       // System.out.println("Jo IS: " + version.getJSONArray("variables").getJSONObject(0).get("code"));
    }

    public void createVersion(String versionName, Hospitals currentHospital, JSONObject versionObject) {
        //generateConceptPathFromMapping(filePath);
        Versions version = new Versions(versionName);
        System.out.println("Saving Version");
        List<Variables> allVar = new ArrayList<>();
        allVar = customMappings(version, currentHospital, versionObject);
        List<Variables> allVar2 = new ArrayList<>();
        System.out.println("Outsiede allVar: ");
        for (Variables var : allVar) {
            System.out.println("The variable returned is: "+var.getCode()+" out of: "+allVar.size());
            if (var.getHospital() != null && var.getCode() != null) {
                allVar2.add(var);
            }
        }
        VariablesXLSX_JSON.Node testTree = variablesXLSX_json.createTree(allVar);
        version.setJsonString(variablesXLSX_json.createJSONMetadataWithCDEs(allVar).toString());
        version.setJsonStringVisualizable(variablesXLSX_json.createJSONVisualization(testTree).toString());
        version.setVariables(allVar2);
        versionDAO.saveVersion(version);

    }

    public List<Variables> customMappings(Versions version, Hospitals currentHospital, JSONObject versionObject) {
        List<Variables> xlsxVars = new ArrayList<>();
        //String mapFunction = null;
        System.out.println("Inside custom mapping");

        JSONArray variablesJsonArray = versionObject.getJSONArray("variables");
        for (int i = 0; i < variablesJsonArray.length(); i++) {
            Variables newVar = new Variables();
            JSONObject variableJsonObject = variablesJsonArray.getJSONObject(i);
            System.out.println("Adding name to variable: " + variableJsonObject.toString());
            newVar.setName(variableJsonObject.get("name").toString());
            newVar.setCsvFile(variableJsonObject.get("csvFile").toString());
            newVar.setName(variableJsonObject.get("name").toString());
            newVar.setCode(variableJsonObject.get("code").toString());
            newVar.setType(variableJsonObject.get("type").toString());
            newVar.setValues(variableJsonObject.get("values").toString());
            newVar.setUnit(variableJsonObject.get("unit").toString());
            newVar.setCanBeNull(variableJsonObject.get("canBeNull").toString());
            newVar.setDescription(variableJsonObject.get("description").toString());
            newVar.setComments(variableJsonObject.get("comments").toString());
            newVar.setConceptPath(variableJsonObject.get("conceptPath").toString());
            newVar.setMethodology(variableJsonObject.get("methodology").toString());


            if (variableJsonObject.get("mapCDE").toString() != "") {
                if (variableJsonObject.get("mapCDE").toString().contains(",")) {

                    newVar = uploadVariables.mappingToMultipleCdes(variableJsonObject.get("mapCDE").toString(), variableJsonObject.get("mapFunction").toString(), newVar, version, currentHospital);

                } else {

                    newVar = uploadVariables.mappingToSingleCde(variableJsonObject.get("mapCDE").toString(), variableJsonObject.get("mapFunction").toString(), newVar, version, currentHospital);
                    System.out.println("newVar after mapping to single: "+newVar.getCode());
                }
            } else {//cell is empty
                variableDAO.saveVersionToVariable(newVar, version);
                List<Variables> hospVar = currentHospital.getVariables();
                hospVar.add(newVar);
                currentHospital.setVariables(hospVar);
                hospitalDAO.save(currentHospital);
                newVar.setHospital(currentHospital);
                variableDAO.save(newVar);
            }

            xlsxVars.add(newVar);
        }
        for (Variables v : xlsxVars){
            System.out.println("xlsxVars contains:"+v.getCode());
        }

        return xlsxVars;

    }
}