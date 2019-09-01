package com.njust.csa.reg.util;

import org.json.JSONObject;

public class FailureBuilder {
    public static String buildFailureMessage(String reason){
        JSONObject json = new JSONObject();
        json.put("success", false);
        json.put("reason", reason);
        return json.toString();
    }
}
