package com.translateai.constant.classconstant;

import java.util.ArrayList;
import java.util.List;

public final class CorsConstant {

    public static final List<String> LIST_DOMAIN_ACCEPT =
            new ArrayList<>(List.of("http://localhost:3000", "http://localhost:3001", "http://localhost:8081",
                    "https://mentormatch.vn", "https://manager.mentormatch.vn"));

    public static final List<String> LIST_METHOD_ACCEPT =
            new ArrayList<>(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

    public static final List<String> LIST_HEADER_ACCEPT =
            new ArrayList<>(List.of("Authorization", "Cache-Control", "Content-Type"));

    public static final List<String> LIST_EXPOSED_ACCEPT =
            new ArrayList<>(List.of("Authorization"));

    public static final String[] ARRAY_DOMAIN_ACCEPT = {
            "http://localhost:3000",
            "https://mentormatch.vn"
    };

}
