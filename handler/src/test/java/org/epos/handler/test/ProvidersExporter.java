package org.epos.handler.test;

import static org.epos.handler.support.Utils.gson;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.epos.eposdatamodel.ContactPoint;
import org.epos.eposdatamodel.DataProduct;
import org.epos.eposdatamodel.Distribution;
import org.epos.eposdatamodel.Identifier;
import org.epos.eposdatamodel.LinkedEntity;
import org.epos.eposdatamodel.Parameter;
import org.epos.eposdatamodel.SoftwareApplication;
import org.epos.eposdatamodel.State;
import org.epos.eposdatamodel.WebService;
import org.epos.eposdatamodel.Parameter.ActionEnum;
import org.epos.handler.HeaderParser;
import org.epos.handler.beans.MonitoringBean;
import org.epos.handler.dbapi.DBAPIClient;
import org.epos.handler.dbapi.dbapiimplementation.ContactPointDBAPI;
import org.epos.handler.dbapi.dbapiimplementation.DataProductDBAPI;
import org.epos.handler.dbapi.dbapiimplementation.DistributionDBAPI;
import org.epos.handler.dbapi.dbapiimplementation.SoftwareApplicationDBAPI;
import org.epos.handler.enums.DataOriginType;
import org.epos.handler.operations.common.DetailsItemGenerationJPA;
import org.epos.handler.operations.monitoring.URLGeneration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ProvidersExporter {

	public static void main(String[] args) { 

		DBAPIClient dbapi = new DBAPIClient();

		List<SoftwareApplication> softwareApplicationList = new SoftwareApplicationDBAPI().getAllByState(State.PUBLISHED);
		List<MonitoringBean> monitoringList = new ArrayList<>();
		List<DataProduct> datasetList = new DataProductDBAPI().getAllByState(State.PUBLISHED);
		List<Distribution> distributionList = new DistributionDBAPI().metadataMode(false).getAllByState(State.PUBLISHED);
		//List<Person> personList = new PersonDBAPI().getAllByState(State.PUBLISHED);
		List<ContactPoint> contactList = new ContactPointDBAPI().getAllByState(State.PUBLISHED);

		HashMap<String, List<ProviderObject>> groups = new HashMap<String, List<ProviderObject>>();


		for(Distribution dx : distributionList) {

			//DDSS
			for(DataProduct d : datasetList) {
				ArrayList<String> distrs = new ArrayList<String>();
				d.getDistribution().forEach(dist->{
					distrs.add(dist.getMetaId());
				});
				if(distrs.contains(dx.getMetaId())) {
					String ddss = null;

					for (Identifier i : d.getIdentifier()) {
						if(i.getType().equals("DDSS-ID")){
							ddss = i.getIdentifier();
						}
					}
					if(ddss == null) continue;

					String tcsGroup = null;
					if(ddss.toLowerCase().contains("wp08")) tcsGroup = "Seismology";
					else if(ddss.toLowerCase().contains("wp09")) tcsGroup = "Near Fault Observatories";
					else if(ddss.toLowerCase().contains("wp10")) tcsGroup = "GNSS Data and Products";
					else if(ddss.toLowerCase().contains("wp11")) tcsGroup = "Volcano Observations";
					else if(ddss.toLowerCase().contains("wp12")) tcsGroup = "Satellite Data";
					else if(ddss.toLowerCase().contains("wp13")) tcsGroup = "Geomagnetic Observations";
					else if(ddss.toLowerCase().contains("wp14")) tcsGroup = "Anthropogenic Hazards";
					else if(ddss.toLowerCase().contains("wp15")) tcsGroup = "Geological Information and Modeling";
					else if(ddss.toLowerCase().contains("wp16")) tcsGroup = "Multi-Scale Laboratories";
					else if(ddss.toLowerCase().contains("wp18")) tcsGroup = "Tsunami";
					else tcsGroup = "Undefined";

					List<ProviderObject> providerObjects = null;

					if(groups.containsKey(tcsGroup)) {
						providerObjects = groups.get(tcsGroup);
					}else {
						providerObjects  = new ArrayList<ProviderObject>();
						groups.put(tcsGroup, providerObjects);
					}

					ProviderObject po = new ProviderObject();
					po.setServiceName(dx.getTitle().get(0));
					po.setServiceproviders(new ArrayList<ContactPoint>());
					po.setDataproviders(new ArrayList<ContactPoint>());
					
					d.getContactPoint().forEach(le -> {
						dbapi.retrieve(ContactPoint.class, new DBAPIClient.GetQuery().instanceId(le.getInstanceId()))
						.forEach(contact-> po.getDataproviders().add(contact));
					});

					if(dx.getAccessService()!=null) {
						for(WebService ws : dbapi.retrieve(WebService.class, new DBAPIClient.GetQuery().instanceId(dx.getAccessService().getInstanceId()))) {
							for(LinkedEntity le : ws.getContactPoint()) {
								dbapi.retrieve(ContactPoint.class, new DBAPIClient.GetQuery().instanceId(le.getInstanceId()))
								.forEach(contact-> po.getServiceproviders().add(contact));
							}	
						}
					}
					
					groups.get(tcsGroup).add(po);
				}
			}
		}
		
		try (Writer writer = new FileWriter("providers.csv")) {
			writer.append("TCS")
			  .append(';')
			  .append("Service Name")
			  .append(';')
			  .append("Data Providers emails")
			  .append(';')
			  .append("Service Providers emails")
			  .append("\n");
		  for (Entry<String, List<ProviderObject>> entry : groups.entrySet()) {
			  entry.getValue().forEach(e->{
				  try {
					writer.append(entry.getKey())
					  .append(';')
					  .append(e.getServiceName())
					  .append(';')
					  .append(e.getDataproviders().stream().map(ContactPoint::getEmail).collect(Collectors.toList()).toString())
					  .append(';')
					  .append(e.getServiceproviders().stream().map(ContactPoint::getEmail).collect(Collectors.toList()).toString())
					  .append("\n");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			  });
		   
		  }
		} catch (IOException ex) {
		  ex.printStackTrace(System.err);
		}
	}
}
