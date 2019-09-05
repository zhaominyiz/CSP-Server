package com.njust.csa.reg.controller;

import com.njust.csa.reg.service.AdminService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Controller
public class AdminController {
    private static Map<String, String> sessionList = new HashMap<>();

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService){
        this.adminService = adminService;
    }

    //获取以往报名情况和成绩
    @RequestMapping(value = "/student/score", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getWeekMessage(HttpServletRequest request) {
        String studentid = request.getParameter("studentid");
        return adminService.getScore(studentid);
    }

    //重定向后的请求处理
    @RequestMapping(value = "/page/login", method = RequestMethod.GET)
    public String getIndex(HttpServletRequest request){
        request.getSession().setAttribute("isRedirect", true); //添加session信息，避免拦截器重复拦截
        return "/page/index.html";
    }

    //处理管理端的所有静态请求
    @RequestMapping(value = "/page/activity/*", method = RequestMethod.GET)
    public String checkIndex(HttpSession session){
        //如果登录，则跳回主页面
        if(checkUser(session)){
            return "/page/index.html";
        }
        //如果访问静态请求时没有登录，则重定向
        return "redirect:/page/login";
    }

    //管理端登录
    @ResponseBody
    @RequestMapping(value = "/login", method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    public ResponseEntity adminLogin(@RequestBody String jsonString, HttpSession session){
        JSONObject json = new JSONObject(jsonString);
        String username = json.getString("username");
        String password = json.getString("password");
        if(adminService.login(username, password)){
            sessionList.merge(username, session.getId(), (key, value) -> value = session.getId());
            session.setAttribute("username", username);
            System.out.println("SUCCESS");
            return new ResponseEntity(HttpStatus.OK);
        }
        else{
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
    }

    //管理端登出
    @ResponseBody
    @RequestMapping(value = "/logout", method = RequestMethod.GET,
            produces = "application/json;charset=UTF-8")
    public ResponseEntity adminLogout(HttpSession session){
        if(session.getAttribute("username") != null){
            sessionList.remove(session.getAttribute("username").toString());
            session.removeAttribute("username");
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    // 创建报名
    @ResponseBody
    @RequestMapping(value = "/admin/activity", method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    public ResponseEntity<String> postActivity(@RequestBody String jsonString, HttpSession session){

        if(checkUser(session)){
            JSONObject json = new JSONObject(jsonString);
            String activityName;
            Timestamp startTime;
            Timestamp endTime;
            JSONArray items;
            try{
                activityName = json.getString("name");
                startTime = json.isNull("startTime") ? null : Timestamp.valueOf(json.getString("startTime"));
                endTime = json.isNull("endTime") ? null : Timestamp.valueOf(json.getString("endTime"));
                items = json.getJSONArray("items");

                long activityId = adminService.postActivity(activityName,
                        session.getAttribute("username").toString(), startTime, endTime, items,"");
                if(activityId != -1){
                    JSONObject response = new JSONObject();
                    response.put("id", activityId);
                    return new ResponseEntity<>(response.toString(), HttpStatus.OK);
                }
                else{
                    return new ResponseEntity<>("", HttpStatus.NOT_ACCEPTABLE);
                }
            } catch (Exception e){
                e.printStackTrace();
                return new ResponseEntity<>("", HttpStatus.NOT_ACCEPTABLE);
            }
        }
        else{
            return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);
        }
    }

    //获取用户列表，用于登录
    @ResponseBody
    @RequestMapping(value = "/user", method = RequestMethod.GET,
            produces = "application/json;charset=UTF-8")
    public ResponseEntity<String> getUsers(){
        return new ResponseEntity<>(adminService.getUser(), HttpStatus.OK);
    }

    //获取所有活动
    @ResponseBody
    @RequestMapping(value = "/admin/activity", method = RequestMethod.GET,
            produces = "application/json;charset=UTF-8")
    public ResponseEntity<String> adminGetActivity(HttpSession session){
        if(checkUser(session)){
            return new ResponseEntity<>(adminService.getActivities(), HttpStatus.OK);
        }
        else return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);
    }

    //设置活动状态
    @ResponseBody
    @RequestMapping(value = "/admin/activity/{id}/status", method = RequestMethod.PUT,
            produces = "application/json;charset=UTF-8")
    public ResponseEntity setActivityStatus(@PathVariable long id, @RequestBody String jsonString, HttpSession session){
        if(checkUser(session)){
            byte status = (byte)new JSONObject(jsonString).getInt("status");
            if(adminService.setActivityStatus(id, status)){
                return new ResponseEntity(HttpStatus.ACCEPTED);
            }
            else return new ResponseEntity(HttpStatus.NOT_ACCEPTABLE);
        }
        else return new ResponseEntity(HttpStatus.UNAUTHORIZED);
    }

    //作为管理员获取一个活动的结构
    @ResponseBody
    @RequestMapping(value = "/admin/activity/{id}", method = RequestMethod.GET,
            produces = "application/json;charset=UTF-8")
    public ResponseEntity<String> getActivityStructure(@PathVariable long id, HttpSession session){
        if(checkUser(session)){
            return new ResponseEntity<>(adminService.getActivityStructure(id), HttpStatus.OK);
        }
        else return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);
    }

    //删除一个报名
    @ResponseBody
    @RequestMapping(value = "/admin/activity/{id}", method = RequestMethod.DELETE,
            produces = "application/json;charset=UTF-8")
    public ResponseEntity<String> deleteActivity(@PathVariable long id, HttpSession session){
        if(checkUser(session)){
            return new ResponseEntity<>(adminService.deleteActivity(id), HttpStatus.OK);
        }
        else return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);
    }

    //获取一个报名内的报名信息
    @ResponseBody
    @RequestMapping(value = "/admin/activity/{id}/applicant", method = RequestMethod.GET,
            produces = "application/json;charset=UTF-8")
    public ResponseEntity<String> getActivityApplicants(@PathVariable long id, HttpSession session){
        if(checkUser(session)){
            return new ResponseEntity<>(adminService.getActivityApplicants(id), HttpStatus.OK);
        }
        else return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);
    }

    //删除指定的报名信息
    @ResponseBody
    @RequestMapping(value = "/admin/activity/{aid}/applicant/{iid}", method = RequestMethod.DELETE,
            produces = "application/json;charset=UTF-8")
    public ResponseEntity<String> deleteApplicantInfo(@PathVariable long aid, @PathVariable int iid, HttpSession session){
        if(checkUser(session)){
            return new ResponseEntity<>(adminService.deleteApplicantInfo(aid, iid), HttpStatus.OK);
        }
        else return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);
    }

    //更新报名信息
    @ResponseBody
    @RequestMapping(value = "/admin/activity/{id}", method = RequestMethod.PUT,
            produces = "application/json;charset=UTF-8")
    public ResponseEntity<String> alterActivityStructure(@PathVariable long id, HttpSession session,
                                                         @RequestBody String jsonString){
        if(checkUser(session) ){
            if(adminService.alterActivityStructure(id, new JSONObject(jsonString)))
                return new ResponseEntity<>("", HttpStatus.OK);
            else
                return new ResponseEntity<>("", HttpStatus.NOT_ACCEPTABLE);
        }
        else return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);
    }

    // 检查当前用户连接是否已经登陆
    private boolean checkUser(HttpSession session){
        if(session == null) return false;
        String userName = session.getAttribute("username") == null ?
                null : session.getAttribute("username").toString();
        return userName != null && sessionList.get(userName).equals(session.getId());
    }

    //  提交CSP成绩文件
    @ResponseBody
    @RequestMapping(value = "/admin/submitCSP", method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    public ResponseEntity<String> addScore(MultipartHttpServletRequest request ,HttpSession session){
        if(checkUser(session)) {
            return new ResponseEntity<>(adminService.doinsertCSPInfo(request), HttpStatus.OK);
        }else{
            return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);
        }
    }

    //    通过Doc表明表去生成表单
    @ResponseBody
    @RequestMapping(value = "/admin/activitybyfile", method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    public ResponseEntity<String> postActivityByFile(MultipartHttpServletRequest request, HttpSession session){
        if(checkUser(session)){
            String activityName;
            Timestamp startTime;
            Timestamp endTime;
            try{
                activityName = request.getParameter("name");
                startTime =  Timestamp.valueOf(request.getParameter("startTime"));
                endTime =  Timestamp.valueOf(request.getParameter("endTime"));
                MultipartFile file = request.getFile("file");
                String fileName = adminService.randomName()+"."+file.getOriginalFilename().substring(
                        file.getOriginalFilename().lastIndexOf(".") + 1);;
                JSONArray items = adminService.getTableFromFile(file,fileName);
                long activityId = adminService.postActivity(activityName,
                        session.getAttribute("username").toString(), startTime, endTime, items,fileName);
                if(activityId == -1){
                    return new ResponseEntity<>("", HttpStatus.NOT_ACCEPTABLE);
                }
                else{
                    JSONObject res = new JSONObject();
                    res.put("id", activityId);
                    return new ResponseEntity<>(res.toString(), HttpStatus.OK);
                }
            } catch (Exception e){
                e.printStackTrace();
                return new ResponseEntity<>("", HttpStatus.NOT_ACCEPTABLE);
            }
        }
        else{
            return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);
        }
    }

//    批量下载
@ResponseBody
@RequestMapping(value = "/admin/activity/download/{id}", method = RequestMethod.GET,
        produces = "application/json;charset=UTF-8")
public ResponseEntity<String> downloadAllData(@PathVariable long id, HttpSession session){
    if(checkUser(session) ){
        System.out.println("Download！"+id);
        return new ResponseEntity<>(adminService.generateZipFile(id), HttpStatus.OK);
    }
    else return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);
}
}
