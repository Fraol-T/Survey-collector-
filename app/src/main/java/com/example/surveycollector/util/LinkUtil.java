package com.example.surveycollector.util;

import java.util.UUID;

public class LinkUtil {
    public static String generateToken() {
        return UUID.randomUUID().toString();
    }

    public static String buildUrl(String token) {
        return "https://survaycollector.web.app/survey.html?token=" + token;
    }
}
