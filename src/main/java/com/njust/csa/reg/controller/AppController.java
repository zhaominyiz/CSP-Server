package com.njust.csa.reg.controller;

import com.njust.csa.reg.service.AppService;
import com.njust.csa.reg.util.FailureBuilder;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
public class AppController {
    private final AppService appService;

    @Autowired
    public AppController(AppService appService){
        this.appService = appService;
    }

    //获取所有已开启的报名信息
    @RequestMapping(value = "/activity", method = RequestMethod.GET,
            produces = "application/json;charset=UTF-8")
    public String getActivities(){
        return appService.getActivities();
    }

    //获取指定报名信息的结构
    @RequestMapping(value = "/activity/{id}", method = RequestMethod.GET,
            produces = "application/json;charset=UTF-8")
    public String getActivityStructure(@PathVariable long id){
        return appService.getActivityStructure(id);
    }

    //提交指定报名信息
    @RequestMapping(value = "/activity/{id}", method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    public String putApplicantInfo(@PathVariable long id, @RequestBody String jsonString){
        if(jsonString.equals("")){
            return FailureBuilder.buildFailureMessage("报名信息为空！请检查信息");
        }
        JSONObject json = new JSONObject(jsonString);
        return appService.putApplicantInfo(id, json);
    }
}
