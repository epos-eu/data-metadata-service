package org.epos.handler.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.epos.handler.beans.Facets;
import org.epos.handler.dbapi.dbapiimplementation.EPOSDataModelDBAPI;
import org.epos.handler.support.GsonByteArrayToBase64;
import org.epos.handler.support.GsonLocalDateTime;
import org.epos.handler.support.MessageEDMWrap;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.util.List;

public class FacetsBuilderTest {

    public static void main(String[] argv) throws IOException {

        System.out.println(Facets.getInstance().getFacetsFromDatabase());

    }
}