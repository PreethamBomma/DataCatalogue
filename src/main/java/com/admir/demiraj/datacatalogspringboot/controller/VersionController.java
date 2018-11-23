package com.admir.demiraj.datacatalogspringboot.controller;

import com.admir.demiraj.datacatalogspringboot.dao.HospitalDAO;
import com.admir.demiraj.datacatalogspringboot.dao.VersionDAO;
import com.admir.demiraj.datacatalogspringboot.resources.Versions;
import jdk.nashorn.internal.runtime.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
//@CrossOrigin(origins = "http://195.251.252.222:2442")
//@CrossOrigin(origins = "http://172.16.10.138:4200")
//@CrossOrigin
@RequestMapping("/versions")
public class VersionController {

    @Autowired
    private VersionDAO versionDAO;

    @Autowired
    private HospitalDAO hospitalDAO;

    @GetMapping("/allVersions")
    public List<Versions>  getAllVerions(){return versionDAO.getAllVersions();}

    @GetMapping("/allVersions/{version_id}")
    public Versions  getVersionById(@PathVariable("version_id") Long versionId){
        BigInteger verId = BigInteger.valueOf(versionId);
        return versionDAO.getOne(verId);}

    @GetMapping("/jsonStringByVersionId/{version_id}")
    public String  getJsonStringByVersionId(@PathVariable(value="version_id") Long version_id){return versionDAO.getJsonStringByVersionId(version_id);}


    @GetMapping("/allVersionsPerHospital")
    public Map<String, List<Versions>>  getAllVersionsPerHospital(){
        List<BigInteger> allHospitalIds = hospitalDAO.getAllHospitalIds();
        Map<String, List<Versions>> versionsPerHospital = new HashMap<>();
        for(BigInteger hospId : allHospitalIds){
            List<BigInteger> allVersionIds = versionDAO.getAllVersionIdsByHospitalId(hospId);
            List<Versions> versions = new ArrayList<>();
            for(BigInteger versionId : allVersionIds){
                versions.add(versionDAO.getVersionById(versionId));
            }

            String hospName = hospitalDAO.getHospitalNameById(hospId);
            versionsPerHospital.put(hospName, versions);

        }
        return versionsPerHospital;
    }

    @GetMapping("/allVersionsPerHospital/{hospital_id}")
    public List<Versions>  getAllVersionsPerHospital(@PathVariable(value = "hospital_id") Long hospitalId){


        BigInteger hospId = BigInteger.valueOf(hospitalId);
            List<BigInteger> allVersionIds = versionDAO.getAllVersionIdsByHospitalId(hospId);
            List<Versions> versions = new ArrayList<>();
            for(BigInteger versionId : allVersionIds){
                versions.add(versionDAO.getVersionById(versionId));
            }
        return versions;
    }
}
