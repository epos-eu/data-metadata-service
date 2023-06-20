package org.epos.handler.support;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;

public class GsonLocalDateTimeSingleton {

    private static Gson instance;

    public GsonLocalDateTimeSingleton() {
    }

    public static synchronized Gson getInstance() {
        if (instance == null) {
            GsonBuilder builder = new GsonBuilder();
            builder.excludeFieldsWithModifiers(Modifier.STATIC, Modifier.TRANSIENT, Modifier.VOLATILE)
                    .registerTypeHierarchyAdapter(byte[].class, new GsonByteArrayToBase64())
                    .registerTypeAdapter(LocalDateTime.class, new GsonLocalDateTime());
            instance = builder.create();
        }
        return instance;
    }
}
