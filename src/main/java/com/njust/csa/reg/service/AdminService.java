package com.njust.csa.reg.service;

import com.njust.csa.reg.repository.docker.*;
import com.njust.csa.reg.repository.entities.*;
import com.njust.csa.reg.util.ActivityUtil;
import com.njust.csa.reg.util.DocxUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class AdminService {
    private final UserRepo userRepo;
    private final TableStructureRepo tableStructureRepo;
    private final TableInfoRepo tableInfoRepo;
    private final ApplicantInfoViewRepo applicantInfoViewRepo;
    private final ApplicantInfoRepo applicantInfoRepo;
    private final ActivityUtil activityUtil;
    private final ScoreRepo scoreRepo;
    @Autowired
    public AdminService(UserRepo userRepo, TableStructureRepo tableStructureRepo,
                        TableInfoRepo tableInfoRepo, ApplicantInfoViewRepo applicantInfoViewRepo,
                        ApplicantInfoRepo applicantInfoRepo,
                        ActivityUtil activityUtil,ScoreRepo scoreRepo) {
        this.userRepo = userRepo;
        this.tableStructureRepo = tableStructureRepo;
        this.tableInfoRepo = tableInfoRepo;
        this.applicantInfoViewRepo = applicantInfoViewRepo;
        this.applicantInfoRepo = applicantInfoRepo;
        this.activityUtil = activityUtil;
        this.scoreRepo=scoreRepo;
    }

    public boolean login(String username, String password) {
//        System.out.println("!!!"+password+ DigestUtils.md5Hex("NJUST" + password + "CSA"));
        return userRepo.existsByNameAndPassword(username, DigestUtils.md5Hex("NJUST" + password + "CSA"));
    }

    @Transactional
    public long postActivity(String activityName, String publisherName, Timestamp startTime, Timestamp endTime,
                             JSONArray items,String templename) {
        long activityId;
        TableInfoEntity tableInfo = new TableInfoEntity();
        tableInfo.setTitle(activityName);
        Optional<UserEntity> publisherEntity = userRepo.findByName(publisherName);
        if (!publisherEntity.isPresent()) return -1;
        tableInfo.setPublisher(publisherEntity.get().getId());
        tableInfo.setStartTime(startTime);
        tableInfo.setEndTime(endTime);
        tableInfo.setStatus((byte) 0);
        tableInfo.setTempleName(templename);
//        if(startTime == null || startTime.before(new Timestamp(System.currentTimeMillis()))){
//            tableInfo.setStatus("open");
//        }
//        else{
//            tableInfo.setStatus("close");
//        }

        tableInfoRepo.save(tableInfo);
        activityId = tableInfo.getId();

        List<TableStructureEntity> entityList = createActivityStructure(activityId, items, -1);

        for (TableStructureEntity entity : entityList) {
            try {
                tableStructureRepo.save(entity);
            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }
        }
        return activityId;
    }

    //获取所有用户的基本信息
    public String getUser() {
        JSONArray response = new JSONArray();
        Iterable<UserEntity> userEntityIterator = userRepo.findAll();
        for (UserEntity userEntity : userEntityIterator) {
            JSONObject user = new JSONObject();
            user.put("id", userEntity.getId());
            user.put("name", userEntity.getName());
            response.put(user);
        }
        return response.toString();
    }

    public String getActivities() {
        return activityUtil.getActivities(true).toString();
    }

    public boolean setActivityStatus(long id, byte status) {
        Optional<TableInfoEntity> table = tableInfoRepo.findById(id);
        if (table.isPresent()) {
            TableInfoEntity tableInfoEntity = table.get();
            tableInfoEntity.setStatus(status);
            if (status == (byte) 3 && tableInfoEntity.getEndTime() == null) {
                tableInfoEntity.setEndTime(new Timestamp(System.currentTimeMillis()));
            }

            tableInfoRepo.save(tableInfoEntity);
            return true;
        }
        return false;
    }

    public String getActivityStructure(long id) {
        return activityUtil.generateActivityStructure(id, true).toString();
    }

    //删除相关活动
    public String deleteActivity(long id) {
        JSONObject responseJson = new JSONObject();
        Optional<TableInfoEntity> table = tableInfoRepo.findById(id);
        if (!table.isPresent()) {
            responseJson.put("reason", "未找到ID对应的报名！");
            return responseJson.toString();
        }

        TableInfoEntity tableInfoEntity = table.get();

        if (tableInfoEntity.getStatus() == (byte) 3 || tableInfoEntity.getStatus() == (byte) 0) {
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            LocalDateTime currentDate = currentTime.toLocalDateTime();
            if (tableInfoEntity.getStatus() == 3 &&
                    !currentDate.minusDays(30).isAfter(tableInfoEntity.getEndTime().toLocalDateTime())) {
                responseJson.put("reason", "报名结束后不超过三十天，不允许删除！");
                return responseJson.toString();
            }
        } else {
            responseJson.put("reason", "报名尚未结束，不允许删除！");
            return responseJson.toString();
        }


        tableInfoRepo.delete(tableInfoEntity);
        responseJson.put("reason", "");
        return responseJson.toString();
    }

    //获取单个活动的所有报名者
    public String getActivityApplicants(long tableId) {
        Optional tableInfoEntity = tableInfoRepo.findById(tableId);
        if (!tableInfoEntity.isPresent()) return "未找到ID对应的报名！";

        JSONArray responseJson = new JSONArray();

        List<ApplicantInfoViewEntity> applicantInfoEntities = applicantInfoViewRepo.findAllByTableId(tableId);

        Map<Integer, Map<Long, String>> applicantMap = new HashMap<>();
        for (ApplicantInfoViewEntity applicantInfoEntity : applicantInfoEntities) {
            if (!applicantMap.containsKey(applicantInfoEntity.getApplicantNumber())) {
                applicantMap.put(applicantInfoEntity.getApplicantNumber(), new HashMap<>());
            }
            applicantMap.get(applicantInfoEntity.getApplicantNumber())
                    .put(applicantInfoEntity.getStructureId(), applicantInfoEntity.getValue());
        }

        List<TableStructureEntity> mainItemEntities =
                tableStructureRepo.findAllByTableIdAndBelongsToOrderByIndexNumber(tableId, null);

        //TODO 此处查询可能会有空指针异常
        TableStructureEntity uniqueEntity = tableStructureRepo.findTopByTableIdAndIsUnique(tableId, (byte) 1);

        applicantMap.forEach((key, value) -> {
            JSONObject applicantJson = new JSONObject();
            applicantJson.put("id", key);
            applicantJson.put("unique", value.get(uniqueEntity.getId()));
            applicantJson.put("data", generateApplicantInfoJson(applicantMap, key, mainItemEntities));
            responseJson.put(applicantJson);
        });

        return responseJson.toString();
    }

    @Transactional
    public String deleteApplicantInfo(long tableId, int applicantNumber) {
        JSONObject responseJson = new JSONObject();

        List<ApplicantInfoViewEntity> applicantInfoViewEntities =
                applicantInfoViewRepo.findAllByTableIdAndApplicantNumber(tableId, applicantNumber);
        if (applicantInfoViewEntities.size() == 0) {
            responseJson.put("reason", "不存在此ID的报名信息！");
            return responseJson.toString();
        }
        List<Long> applicantInfoId = new ArrayList<>();
        for (ApplicantInfoViewEntity applicantInfoViewEntity : applicantInfoViewEntities) {
            applicantInfoId.add(applicantInfoViewEntity.getId());
        }

        applicantInfoRepo.deleteAllByIdIn(applicantInfoId);

        responseJson.put("reason", "");
        return responseJson.toString();
    }

    @Transactional
    public boolean alterActivityStructure(long tableId, JSONObject data) {

        Optional<TableInfoEntity> tableInfo = tableInfoRepo.findById(tableId);
        if (!tableInfo.isPresent()) return false;

        TableInfoEntity tableInfoEntity = tableInfo.get();
        if (tableInfoEntity.getStatus() == (byte) 3) return false;

        List<TableStructureEntity> oldStructure =
                tableStructureRepo.findAllByTableIdAndBelongsToOrderByIndexNumber(tableId, null);

        JSONArray items = data.getJSONArray("items");

        List<TableStructureEntity> entities = createActivityStructure(tableId, items, -1);

        for (TableStructureEntity entity : entities) {
            try {
                tableStructureRepo.save(entity);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }


        tableStructureRepo.deleteAll(oldStructure);


        return true;
    }

    /* ======内部方法====== */

    // 构造新报名结构
    // 由于使用到事务，基于SpringAOP，故只能为public
    // 不允许外部调用
    @Transactional
    public List<TableStructureEntity> createActivityStructure(long activityId, JSONArray items, long belongsTo) {
        byte index = 0;
        List<TableStructureEntity> entityList = new ArrayList<>();
        Iterator objects = items.iterator();
        for (; objects.hasNext(); ) {
            JSONObject item = (JSONObject) objects.next();
            TableStructureEntity structureEntity = new TableStructureEntity();
            structureEntity.setTableId(activityId);
            structureEntity.setTitle(item.getString("name"));
            structureEntity.setExtension(item.getString("extension"));
            structureEntity.setType(item.getString("type"));
            structureEntity.setIsUnique(item.getBoolean("unique") ? (byte) 1 : (byte) 0);
            structureEntity.setIsRequired(item.getBoolean("require") ? (byte) 1 : (byte) 0);
            structureEntity.setDefaultValue(item.isNull("defaultValue") ? "" : item.getString("defaultValue"));
            structureEntity.setDescription(item.getString("description"));
            structureEntity.setTips(item.getString("tip"));
            structureEntity.setIndexNumber(index);

            if (!item.isNull("case")) {
                Iterator cases = item.getJSONArray("case").iterator();
                StringBuilder casesString = new StringBuilder();
                for (; cases.hasNext(); ) {
                    casesString.append(cases.next());
                    if (cases.hasNext()) casesString.append(",");
                }

                structureEntity.setCases(casesString.toString());
            }

            if (!item.isNull("range")) {
                JSONArray range = item.getJSONArray("range");
                structureEntity.setRanges(range.get(0) + "," + range.get(1));
            }

            if (belongsTo != -1) {
                structureEntity.setBelongsTo(belongsTo);
            }

            index++;

            entityList.add(structureEntity);

            if (structureEntity.getType().equals("group")) {
                for (TableStructureEntity tableStructureEntity : entityList) {
                    tableStructureRepo.save(tableStructureEntity);
                }
                entityList.clear();
                entityList.addAll(createActivityStructure(activityId, item.getJSONArray("subItem"), structureEntity.getId()));
            }
        }
        return entityList;
    }

    private JSONObject generateApplicantInfoJson(Map<Integer, Map<Long, String>> applicantMap, int applicantNumber,
                                                 List<TableStructureEntity> tableStructureEntities) {
        JSONObject result = new JSONObject();
        for (TableStructureEntity tableStructureEntity : tableStructureEntities) {
            if (tableStructureEntity.getType().equals("group")) {
                List<TableStructureEntity> subItemEntities = tableStructureRepo.
                        findAllByTableIdAndBelongsToOrderByIndexNumber(
                                tableStructureEntity.getTableId(), tableStructureEntity.getId());
                result.put(tableStructureEntity.getTitle(),
                        generateApplicantInfoJson(applicantMap, applicantNumber, subItemEntities));
            } else {
                result.put(tableStructureEntity.getTitle(),
                        applicantMap.get(applicantNumber).get(tableStructureEntity.getId()));
            }
        }
        return result;
    }

//    此函数用于插入CSP成绩
    public String doinsertCSPInfo (MultipartHttpServletRequest request){
        JSONObject responseJson = new JSONObject();
        MultipartFile table =request.getFile("file");
        String filePath = "/../CSP/tmp/";
        File path =new File(filePath);
        if(!path.exists()) {
            path.mkdirs();
//            System.out.println("EGG! CAO!");
        }
        String fileName = table.getOriginalFilename();
        String suffix =fileName.substring(fileName.lastIndexOf(".") + 1);
        long id = Long.parseLong(request.getParameter("id"));
        fileName="tmp."+suffix;
        try {
            FileOutputStream out = new FileOutputStream(filePath + fileName);
            out.write(table.getBytes());
            out.flush();
            out.close();
            List<List<String>>lists=new ArrayList<>();
            if(readExcel(filePath+fileName,lists)){
//                防止将第一行写入数据库
                int cnt=0;
                for(List<String>list:lists){
                    if(cnt==0){
                        cnt++;
                        continue;
                    }
                    ScoreEntity score=new ScoreEntity();
                    score.setActid(id);
                    score.setScore(list.get(2));
                    score.setName(list.get(1));
                    score.setStuid(list.get(0));

                    List<ScoreEntity> queryresult= scoreRepo.findAllByActidAndStuid(id,score.getStuid());
                    if(queryresult==null || queryresult.size()==0)
                        scoreRepo.save(score);
                    else{
                        score=queryresult.get(0);
                        score.setScore(list.get(2));
                        score.setName(list.get(1));
                        scoreRepo.save(score);
                    }
                }
            }
            responseJson.put("reason", "");
            return responseJson.toString();
        }catch (Exception ex){
            ex.printStackTrace();
            responseJson.put("reason", "不支持的文件格式，请使用xls");
            return  responseJson.toString();
        }
    }

    private boolean readExcel(String s,List<List<String>> lists) throws Exception{
        Workbook wb = null;
        String currentpath=new File("").getAbsolutePath()+"\\";
        InputStream stream = new FileInputStream(currentpath+s);
        String fileType =s.substring(s.lastIndexOf(".") + 1);
        if (fileType.equals("xls")) {
            wb = new HSSFWorkbook(stream);
        }
        else {
            throw new Exception("Unsupported file");
        }
        Sheet sheet1 = wb.getSheetAt(0);
        for (Row row : sheet1) {
            List<String> tmplist=new ArrayList<>();
            for (Cell cell : row) {
                cell.setCellType(CellType.STRING);
                tmplist.add(cell.getStringCellValue());
            }
            lists.add((tmplist));
        }
        return true;
    }

//    解析doc文件的占位符，生成报名表信息
    public JSONArray getTableFromFile(MultipartFile file,String randName){
        JSONArray ans = new JSONArray();
        String filePath = "CSP/temple/";
        File path = new File(filePath);
        if(!path.exists())path.mkdirs();
        String fileName = file.getOriginalFilename();
        String suffix =fileName.substring(fileName.lastIndexOf(".") + 1);
        fileName=randName;
        try {
            FileOutputStream out = new FileOutputStream(filePath + fileName);
            out.write(file.getBytes());
            out.flush();
            out.close();
            String result = new DocxUtil().ReadDocx(filePath+fileName);
            Pattern p = Pattern.compile("\\$\\{(.+?)\\}");
//            Pattern p2 = Pattern.compile("<(.+?)>");
            Matcher m = p.matcher(result);
            List<String> matchresult = new ArrayList<>();
            while(m.find()){
                matchresult.add(m.group().substring(2,m.group().length()-1));
            }
            for(String str:matchresult){
                String tmp ="";
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name",str);
                jsonObject.put("extension",str);
                jsonObject.put("type","text");
                jsonObject.put("unique",false);
                jsonObject.put("require",false);
                jsonObject.put("description",tmp);
                jsonObject.put("tip",str);
                ans.put(jsonObject);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return ans;
    }

    public String randomName() {
        String table = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        int l=20;
        String ans="";
        while(l-->0){
            ans+=table.charAt((int)(Math.random() * table.length()));
        }
        return ans;
    }

    public String generateZipFile(long id){
        JSONObject object = new JSONObject();
        try {
            TableInfoEntity table = tableInfoRepo.getTableInfoEntityById(id);
            if(table.getTempleName().equals("")){
                object.put("reason","没有模板文件");
                return object.toString();
            }
            String pp = "CSP/data/";
            File tmppp = new File(pp);
            if(!tmppp.exists())tmppp.mkdirs();
            String path = "CSP/data/"+String.valueOf(id)+"/";
            File dic = new File(path);
            if(!dic.exists()){
                System.out.println("目录异常");
                object.put("reason","目录异常");
                return object.toString();
            }
            String strZipName = path+"ALLDATA.zip";
            File zipfile =new File(strZipName);
            if(zipfile.exists())
                zipfile.delete();
            File files[]=dic.listFiles();
            FileInputStream nFileInputStream = null;
            ZipOutputStream nZipOutputStream = null;
//            System.out.println("GO!");
            nZipOutputStream = new ZipOutputStream(new FileOutputStream(strZipName));
            for (File file : files) {
                nFileInputStream = new FileInputStream(file);
                nZipOutputStream.putNextEntry(new ZipEntry(file.getName()));
                int len;
                while ((len = nFileInputStream.read()) != -1) {
                    nZipOutputStream.write(len);
                    nZipOutputStream.flush();
                }
            }
            nZipOutputStream.close();
            nFileInputStream.close();
            object.put("url","http://47.100.16.60:8080/"+String.valueOf(id)+"/ALLDATA.zip");
            object.put("reason","");
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return object.toString();
    }
}
