package com.njust.csa.reg.service;

import com.njust.csa.reg.repository.docker.ApplicantInfoRepo;
import com.njust.csa.reg.repository.docker.TableInfoRepo;
import com.njust.csa.reg.repository.docker.TableStructureRepo;
import com.njust.csa.reg.repository.docker.UserRepo;
import com.njust.csa.reg.repository.entities.ApplicantInfoEntity;
import com.njust.csa.reg.repository.entities.TableInfoEntity;
import com.njust.csa.reg.repository.entities.TableStructureEntity;
import com.njust.csa.reg.repository.entities.UserEntity;
import com.njust.csa.reg.util.ActivityUtil;
import com.njust.csa.reg.util.FailureBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class AppService {

    private final TableStructureRepo tableStructureRepo;
    private final ApplicantInfoRepo applicantInfoRepo;

    private final ActivityUtil activityUtil;

    @Autowired
    public AppService( TableStructureRepo tableStructureRepo,
                      ApplicantInfoRepo applicantInfoRepo, ActivityUtil activityUtil) {

        this.tableStructureRepo = tableStructureRepo;
        this.applicantInfoRepo = applicantInfoRepo;
        this.activityUtil = activityUtil;
    }

    //获取所有报名信息
    public String getActivities() {
        return activityUtil.getActivities(false).toString();
    }

    public String getActivityStructure(long id) {
        return activityUtil.generateActivityStructure(id, false).toString();
    }

    @Transactional
    public String putApplicantInfo(long tableId, JSONObject applicantInfo){
        List<ApplicantInfoEntity> applicantInfoEntities = new ArrayList<>();
        List<TableStructureEntity> tableStructures =
                tableStructureRepo.findAllByTableIdAndBelongsToOrderByIndexNumber(tableId, null);
        int newApplicantNum = applicantInfoRepo.countByBelongsToStructureId(tableStructures.get(0).getId()) + 1;
        for (TableStructureEntity structure : tableStructures) {
            try{
                applicantInfoEntities.addAll(generateApplicantInfo(structure, newApplicantNum, applicantInfo));
            }catch (IllegalArgumentException e){
                System.out.println(e.getMessage());
                return FailureBuilder.buildFailureMessage("数据库存储错误：" + e.getMessage());
            }
        }

        for (ApplicantInfoEntity applicantInfoEntity : applicantInfoEntities) {
            try{
                applicantInfoRepo.save(applicantInfoEntity);
            } catch (Exception e){
                return FailureBuilder.buildFailureMessage("数据库存储错误，请联系管理员！");
            }
        }
        return new JSONObject().put("success", true).toString();
    }

    private List<ApplicantInfoEntity> generateApplicantInfo(TableStructureEntity tableStructure, int applicantNum, JSONObject value)
        throws IllegalArgumentException{
        List<ApplicantInfoEntity> applicantInfoEntities = new ArrayList<>();

        if(tableStructure.getType().equals("group")){
            List<TableStructureEntity> groupItems =
                    tableStructureRepo.findAllByTableIdAndBelongsToOrderByIndexNumber(tableStructure.getTableId(),
                            tableStructure.getId());
            JSONObject groupJson = value.isNull(tableStructure.getTitle()) ?
                    null : value.getJSONObject(tableStructure.getTitle());

            if(groupJson == null){
                if(tableStructure.getIsRequired() == (byte)1){
                    throw new IllegalArgumentException("获取不到字段：" + tableStructure.getTitle());
                }
            }
            else{

                for (TableStructureEntity groupItem : groupItems) {
                    applicantInfoEntities.addAll(generateApplicantInfo(groupItem, applicantNum, groupJson));
                }
            }
        }
        else{
            ApplicantInfoEntity newInfo = new ApplicantInfoEntity();
            newInfo.setApplicantNumber(applicantNum);
            newInfo.setBelongsToStructureId(tableStructure.getId());

            String info = value.isNull(tableStructure.getTitle()) ? "" : value.getString(tableStructure.getTitle());

            if(info == null){
                if(tableStructure.getIsRequired() == (byte)1){
                    throw new IllegalArgumentException("获取不到字段：" + tableStructure.getTitle());
                }
            }

            else if(tableStructure.getIsUnique() == (byte)1){
                if(applicantInfoRepo.existsByBelongsToStructureIdAndValue(tableStructure.getId(), info)){
                    throw new IllegalArgumentException("字段重复：" + tableStructure.getTitle());
                }
            }

            newInfo.setValue(info);
            applicantInfoEntities.add(newInfo);
        }
        return applicantInfoEntities;
    }
}
