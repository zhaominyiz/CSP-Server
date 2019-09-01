package com.njust.csa.reg.util;

import com.njust.csa.reg.repository.docker.TableInfoRepo;
import com.njust.csa.reg.repository.docker.TableStructureRepo;
import com.njust.csa.reg.repository.docker.UserRepo;
import com.njust.csa.reg.repository.entities.TableInfoEntity;
import com.njust.csa.reg.repository.entities.TableStructureEntity;
import com.njust.csa.reg.repository.entities.UserEntity;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.List;

@Component
public class ActivityUtil {

    private final TableStructureRepo tableStructureRepo;
    private final TableInfoRepo tableInfoRepo;
    private final UserRepo userRepo;

    @Autowired
    public ActivityUtil(TableStructureRepo tableStructureRepo, UserRepo userRepo,TableInfoRepo tableInfoRepo){
        this.tableStructureRepo = tableStructureRepo;
        this.userRepo = userRepo;
        this.tableInfoRepo = tableInfoRepo;
    }

    public JSONArray generateActivityStructure(long id, boolean isAdmin){
        JSONArray result = new JSONArray();
        if (id <= 0) {
            return result;
        }
        List<TableStructureEntity> tableStructures =
                tableStructureRepo.findAllByTableIdAndBelongsToOrderByIndexNumber(id, null);
        for (TableStructureEntity tableStructure : tableStructures) {
            result.put(generateTableStructure(tableStructure, isAdmin));
        }
        return result;
    }


    //生成一个报名的JSON信息
    private JSONObject generateTableStructure(TableStructureEntity tableStructure, boolean isAdmin) {
        JSONObject structureJson = new JSONObject();
        if (tableStructure.getType().equals("group")) {
            JSONArray groupItemsJson = new JSONArray();
            List<TableStructureEntity> groupItems =
                    tableStructureRepo.findAllByBelongsToOrderByIndexNumber(tableStructure.getId());
            for (TableStructureEntity groupItem : groupItems) {
                    groupItemsJson.put(generateTableStructure(groupItem, isAdmin));
            }
            structureJson.put("subItem", groupItemsJson);
        }


        structureJson.put("name", tableStructure.getTitle());
        structureJson.put("type", tableStructure.getType());
        if(isAdmin){
            structureJson.put("extension", tableStructure.getExtension());
            structureJson.put("unique", tableStructure.getIsUnique() != (byte) 0);
        }
        structureJson.put("description", tableStructure.getDescription());
        structureJson.put("tip", tableStructure.getTips());

        if(!tableStructure.getDefaultValue().equals(""))
            structureJson.put("defaultValue", tableStructure.getDefaultValue());

        structureJson.put("require", tableStructure.getIsRequired() == (byte)1);

        if(!tableStructure.getCases().equals("")){
            JSONArray casesJson = new JSONArray();
            String[] cases = tableStructure.getCases().split(",");
            for (String aCase : cases) {
                casesJson.put(aCase);
            }
            structureJson.put("case", casesJson);
        }

        if(!tableStructure.getRanges().equals("")){
            JSONArray rangeJson = new JSONArray();
            String[] ranges = tableStructure.getRanges().split(",");
            for (String range : ranges) {
                rangeJson.put(range);
            }
            structureJson.put("range", rangeJson);
        }

        return structureJson;
    }

    //获取所有活动
    public JSONArray getActivities(boolean isAdmin){
        JSONArray result = new JSONArray();
        Iterable<TableInfoEntity> tables;
        if(isAdmin) tables = tableInfoRepo.findAll();
        else tables = tableInfoRepo.findAllByStatus((byte)1);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (TableInfoEntity table : tables) {
            JSONObject tableJson = new JSONObject();
            if(isAdmin) tableJson.put("status", table.getStatus());
            tableJson.put("id", Long.toString(table.getId()));
            tableJson.put("name", table.getTitle());

            UserEntity publisher = userRepo.findById(table.getPublisher()).orElse(null);
            tableJson.put("publisher", publisher == null ? "匿名" : publisher.getRealName());

            if(table.getStartTime() != null) tableJson.put("startTime", dateFormat.format(table.getStartTime()));
            if(table.getEndTime() != null) tableJson.put("endTime", dateFormat.format(table.getEndTime()));
            result.put(tableJson);
        }

        return result;
    }
}
