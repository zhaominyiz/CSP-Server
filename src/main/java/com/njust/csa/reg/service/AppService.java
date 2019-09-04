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
import com.njust.csa.reg.util.DocxUtil;
import com.njust.csa.reg.util.FailureBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.Table;
import javax.transaction.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;

@Service
public class AppService {

    private final TableStructureRepo tableStructureRepo;
    private final ApplicantInfoRepo applicantInfoRepo;
    private final TableInfoRepo tableInfoRepo;
    private final ActivityUtil activityUtil;

    @Autowired
    public AppService( TableStructureRepo tableStructureRepo,
                      ApplicantInfoRepo applicantInfoRepo, ActivityUtil activityUtil,
                       TableInfoRepo tableInfoRepo) {

        this.tableStructureRepo = tableStructureRepo;
        this.applicantInfoRepo = applicantInfoRepo;
        this.activityUtil = activityUtil;
        this.tableInfoRepo = tableInfoRepo;
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
        TableInfoEntity table = tableInfoRepo.getTableInfoEntityById(tableId);
        if(!table.getTempleName().equals("")){
            generateFileFromTable(table.getTempleName(),applicantInfo,newApplicantNum,table.getId());
        }
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

    private void generateFileFromTable(String templeName, JSONObject applicantInfo,int num,long tableid) {
        String path = "CSP/temple/";
        String target = "CSP/data/";
        File datapath = new File((target));
        if(!datapath.exists())datapath.mkdirs();
        String suffix =templeName.substring(templeName.lastIndexOf(".") + 1);
        String targetFileName = String.valueOf(num)+"."+suffix;
        target+=String.valueOf(tableid)+"/";
        File dir = new File(target);
        if(!dir.exists()){
            dir.mkdirs();
        }
        try {
//            System.out.println(path+templeName+"??");
//            copyFile(path + templeName, target,targetFileName);
            Map<String,String> mapp = new HashMap<>();
            for(String str:applicantInfo.keySet()){
                mapp.put(str,applicantInfo.getString(str));
            }
            new DocxUtil().changWord(path+templeName,target+targetFileName,mapp);
        }catch (Exception ex){
            ex.printStackTrace();
        }

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
//    拷贝文件
    public static void copyFile(String srcPath, String targetPath,String fileName) throws Exception {
        File srcFile = new File(srcPath);
        File target = new File(targetPath);
        if (!srcFile.exists()) {
            throw new Exception("文件不存在！");
        }
        if (!srcFile.isFile()) {
            throw new Exception("不是文件！");
        }
        //判断目标路径是否是目录
        if (!target.isDirectory()) {
            throw new Exception("文件路径不存在！");
        }

        String newFileName = targetPath + fileName;
        File targetFile = new File(newFileName);
        if(targetFile.exists()){
            targetFile.delete();
        }
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            in = new FileInputStream(srcFile);
            out = new FileOutputStream(targetFile);
            //从in中批量读取字节，放入到buf这个字节数组中，
            // 从第0个位置开始放，最多放buf.length个 返回的是读到的字节的个数
            byte[] buf = new byte[8 * 1024];
            int len = 0;
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
                out.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try{
                if(in != null){
                    in.close();
                }
            }catch(Exception e){
                System.out.println("关闭输入流错误！");
            }
            try{
                if(out != null){
                    out.close();
                }
            }catch(Exception e){
                System.out.println("关闭输出流错误！");
            }
        }

    }

}
