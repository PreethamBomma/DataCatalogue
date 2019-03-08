/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.admir.demiraj.datacatalogspringboot.dao;

import com.admir.demiraj.datacatalogspringboot.repository.HospitalsRepository;
import com.admir.demiraj.datacatalogspringboot.repository.VersionsRepository;
import com.admir.demiraj.datacatalogspringboot.resources.Variables;
import com.admir.demiraj.datacatalogspringboot.resources.Versions;
import jdk.nashorn.internal.runtime.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;

import javax.management.Query;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author root
 */
@Service
public class VersionDAO {

    @Autowired
    private VersionsRepository versionsRepository;

    @Autowired
    HospitalDAO hospitalDAO;

    public List<Versions> getAllCdeVersions(){

        List<Versions> allversions = versionsRepository.findAll();
        List<Versions> allCdeVersions = new ArrayList<>();
        for (Versions version : allversions){
            if(!version.getCdevariables().isEmpty() && version.getVariables().isEmpty()){
                allCdeVersions.add(version);
            }
        }
        return allCdeVersions;

    }

    public Versions getLastCdeVersion(){
        List<Versions> allCdeVersions = getAllCdeVersions();
        if (allCdeVersions.isEmpty())
            return null;
        Versions lastOne = allCdeVersions.get(0);
        for (int i=0; i<allCdeVersions.size(); i++)
        {
            if (allCdeVersions.get(i).getVersion_id().compareTo(lastOne.getVersion_id()) == 1)
                lastOne = allCdeVersions.get(i);
        }
        return lastOne;
    }

    public String getJsonStringByVersionId(Long versionId){
        List<Versions> allversions = versionsRepository.findAll();
        for(Versions ver : allversions){
            if(ver.getVersion_id() == BigInteger.valueOf(versionId)) {
                return ver.getJsonString();
            }
        }
        return  null;
    }

    public String getJsonStringVisualizableByVersionId(Long versionId){
        List<Versions> allversions = versionsRepository.findAll();
        for(Versions ver : allversions){
            if(ver.getVersion_id() == BigInteger.valueOf(versionId)) {
                return ver.getJsonStringVisualizable();
            }
        }
        return  null;
    }

    public Versions saveVersion(Versions ver) {
        return versionsRepository.save(ver);
    }


    public List<Versions> getAllVersions() {
        return versionsRepository.findAll();
    }

    public Versions getOne(BigInteger verId){
        List<Versions> allVersions = versionsRepository.findAll();
        for(Versions version:allVersions){
            if(version.getVersion_id() == verId){
                return version;
            }
        }
        return null;

    }


    public void saveVariablesToVersion(Versions version, List<Variables> variables){
        version.setVariables(variables);
    }


    public List<Versions> getAllVersionsByVariableId(BigInteger variableId) {
        return versionsRepository.getAllVersionByVariableId(variableId);
    }

    public List<BigInteger> getAllVersionIdsByHospitalId(BigInteger hospitalId) {
        List<Versions> allVersions = versionsRepository.findAll();
        List<BigInteger> versionIdsByHospitalId = new ArrayList<>();
        for(Versions version : allVersions){
            if(!version.getVariables().isEmpty()){
                Variables randomVar = new Variables();
                for(Variables var : version.getVariables()) {
                    randomVar =var;
                    break;
                }
            if(randomVar.getHospital().getHospital_id() == hospitalId){
                versionIdsByHospitalId.add(version.getVersion_id());
            }}
        }
        return versionIdsByHospitalId;
    }

    public Versions getLatestVersionByHospitalId(BigInteger hospitalId) {
        List<Versions> allVersions = versionsRepository.findAll();
        Versions latestVersionByHospitalId = new Versions();
       // Date now = new Date();
        for(Versions version : allVersions){
            if(!version.getVariables().isEmpty()){
                if(version.getVariables().get(version.getVariables().size()-1).getHospital().getHospital_id() == hospitalId){
                    latestVersionByHospitalId = version;
                    //now = version.getCreatedAt();
                }}
        }
       return latestVersionByHospitalId;


    }

    public List<Versions> getAllVersionByHospitalName(String hospitalName) {
        List<Versions> allVersions = versionsRepository.findAll();
        List<Versions> versionsByHospitalName = new ArrayList<>();
        BigInteger hospitalId = hospitalDAO.getHospitalIdByName(hospitalName);
        for(Versions version : allVersions){
            if(!version.getVariables().isEmpty()){
                if(version.getVariables().get(0).getHospital().getHospital_id() == hospitalId){
                    versionsByHospitalName.add(version);
                }}
        }
        return versionsByHospitalName;
    }


    public boolean isVersionNameInHospital(String versionName, String hospitalName) {
        List<String> allVersionNames = versionsRepository.getAllVersionNamesByHospitalName(hospitalName);
        for (String ver : allVersionNames) {
            if (ver.equals(versionName)) {
                return true;
            }
        }
        return false;
    }

    public Versions getVersionById(BigInteger verId) {
        return versionsRepository.getOne(verId);
    }

}
