package org.epos.handler.database;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.epos.eposdatamodel.EPOSDataModelEntity;
import org.epos.eposdatamodel.State;
import org.epos.handler.support.GsonLocalDateTimeSingleton;
import org.epos.handler.support.MessageEDMWrap;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.epos.handler.dbapi.dbapiimplementation.EPOSDataModelDBAPI;

@SuppressWarnings("unchecked")
public class DatabaseConnection {

    private static final Gson gson = GsonLocalDateTimeSingleton.getInstance();

    private static final String eposDataModel = "org.epos.eposdatamodel.";

    public static JsonObject ingestorPersist(String payload){


        List<MessageEDMWrap> arrayJsonObject = gson.fromJson(payload, new TypeToken<List<MessageEDMWrap>>(){}.getType());

        HashMap<String, String> map = new HashMap<>();


        for (MessageEDMWrap messageEDMWrap : arrayJsonObject) {


            String className = messageEDMWrap.getObjectName();
            String objectString = messageEDMWrap.getObject();

            try {
                Class<? extends EPOSDataModelEntity> classObject = (Class<? extends EPOSDataModelEntity>) Class.forName(eposDataModel + className);
                EPOSDataModelEntity eposDataModelObject = gson.fromJson(objectString, classObject);
                eposDataModelObject.setEditorId("ingestor");
                eposDataModelObject.setState(State.PUBLISHED);

                EPOSDataModelDBAPI.save(eposDataModelObject);
            } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
                System.err.println(e);
            }
        }

        if(!map.containsKey("status")) map.put("status", "ok");

        return gson.toJsonTree(map).getAsJsonObject();
    }

    public static void save(String payload, String className){

        try {
            Class<? extends EPOSDataModelEntity> classObject = (Class<? extends EPOSDataModelEntity>) Class.forName(eposDataModel + className);

            EPOSDataModelEntity eposDataModelObject = gson.fromJson(payload, classObject);

            EPOSDataModelDBAPI.save(eposDataModelObject);
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            System.err.println(e);
        }
    }

    public static String getById(String id, String className) {

        try {
            Class<? extends EPOSDataModelEntity> classObject = (Class<? extends EPOSDataModelEntity>) Class.forName(eposDataModel + className);


            if(id.isBlank()){
                return gson.toJson(EPOSDataModelDBAPI.getAll(classObject));
            } else {
                List<Object> result = new LinkedList<>();
                Object o = EPOSDataModelDBAPI.get(id, classObject);
                if(o != null) result.add(o);

                return gson.toJson(result);
            }

        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            System.err.println(e);
            return "";
        }
    }

    public static void delete(String id, String className) {
        try {
            Class<? extends EPOSDataModelEntity> classObject = (Class<? extends EPOSDataModelEntity>) Class.forName(eposDataModel + className);


            EPOSDataModelDBAPI.delete(id, classObject);
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            System.err.println(e);
        }
    }

    public static void update(String id, String payload, String className) {
        try {
            Class<? extends EPOSDataModelEntity> classObject = (Class<? extends EPOSDataModelEntity>) Class.forName(eposDataModel + className);
            Object eposDataModelObject = gson.fromJson(payload, classObject);

            EPOSDataModelDBAPI.update(id, eposDataModelObject);
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            System.err.println(e);
        }
    }
}
