package org.epos.handler.test;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.epos.handler.constants.EnvironmentVariables;
import org.epos.handler.dbapi.dbapiimplementation.DataProductDBAPI;
import org.epos.handler.dbapi.dbapiimplementation.OrganizationDBAPI;
import org.epos.eposdatamodel.*;
import org.epos.handler.main.App;
import org.epos.handler.operations.monitoring.ZabbixExecutor;

import com.google.gson.JsonElement;

public class CMCTest {


	public static void main(String[] args) throws IOException, InterruptedException {
		ZabbixExecutor.getInstance();

		Thread.sleep(2000);
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		if(EnvironmentVariables.MONITORING.equals("true")) {
			System.out.println(ZabbixExecutor.getInstance().getHostResults());
			for(JsonElement item : ZabbixExecutor.getInstance().getHostResults()) {
				System.out.println(item.toString());

				if(item.getAsJsonObject().get("lastvalue").getAsString().equals("")) {
					System.out.println("1");
				}
				else {
					System.out.println("2");
					System.out.println(item.getAsJsonObject().get("lastclock").getAsString());
					System.out.println(df.format(new Date(item.getAsJsonObject().get("lastclock").getAsLong()*1000)).replace(" ", "T")+"Z");
				}
			}

			/*
				discoveryList.forEach(dlitem->{
					if(item.getAsJsonObject().get("name").getAsString().contains(dlitem.getSha256id())) {
						if(item.getAsJsonObject().get("lastvalue").getAsString().equals("")) {
							dlitem.setStatus(1);
						}
						else dlitem.setStatus(2);
					}
				});*/

		}
	}

}
