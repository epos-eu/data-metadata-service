package org.epos.handler.operations.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.epos.eposdatamodel.Distribution;
import org.epos.eposdatamodel.LinkedEntity;
import org.epos.eposdatamodel.Operation;
import org.epos.eposdatamodel.Parameter;
import org.epos.eposdatamodel.SoftwareApplication;
import org.epos.handler.beans.AvailableFormat;
import org.epos.handler.beans.AvailableFormat.AvailableFormatBuilder;
import org.epos.handler.constants.EnvironmentVariables;
import org.epos.handler.dbapi.dbapiimplementation.OperationDBAPI;
import org.epos.handler.dbapi.dbapiimplementation.SoftwareApplicationDBAPI;
import org.epos.handler.dbapi.model.*;
import org.epos.handler.enums.AvailableFormatType;

import com.google.gson.JsonObject;

public class AvailableFormatsGeneration {

	private static final String API_PATH_EXECUTE  = EnvironmentVariables.API_CONTEXT+"/execute?id=";
	private static final String API_PATH_EXECUTE_OGC  = EnvironmentVariables.API_CONTEXT+"/ogcexecute/";
	private static final String API_FORMAT = "&format=";

	public static List<AvailableFormat> generate(EDMDistribution distribution) {

		List<AvailableFormat> formats = new ArrayList<>();
		//available formats
		EDMWebservice webserviceByAccessService = distribution.getWebserviceByAccessService();
		if(webserviceByAccessService !=null && webserviceByAccessService.getSupportedOperationByInstanceId() != null) {
			for( EDMOperation op : webserviceByAccessService.getSupportedOperationByInstanceId().stream().map(EDMSupportedOperation::getOperationByInstanceOperationId).collect(Collectors.toList())){

				if (op.getUid() != null && distribution.getAccessURLByInstanceId() != null && distribution.getAccessURLByInstanceId().stream().map(EDMDistributionAccessURL::getInstanceOperationId).collect(Collectors.toList()).contains(op.getInstanceId()) ) {
					
					boolean isWMS = false;
					
					for (EDMMapping map : op.getMappingsByInstanceId() != null ? op.getMappingsByInstanceId() : new ArrayList<EDMMapping>()) {
						if (map.getProperty() != null && map.getProperty().contains("encodingFormat")) {
							for (String pv : map.getMappingParamvaluesById().stream().map(EDMMappingParamvalue::getParamvalue).collect(Collectors.toList())) {
								if (pv.equals("image/png")) {
									isWMS = true;
								}
							}
						}
						
						
						if (map.getProperty() != null && map.getProperty().contains("encodingFormat")) {
							for (String pv : map.getMappingParamvaluesById().stream().map(EDMMappingParamvalue::getParamvalue).collect(Collectors.toList())) {
								if (pv.equals("image/png")) {
									formats.add(new AvailableFormat.AvailableFormatBuilder()
											.originalFormat(pv)
											.format("application/vnd.ogc.wms_xml")
											.href(EnvironmentVariables.API_HOST+ API_PATH_EXECUTE_OGC + distribution.getMetaId())
											.label("WMS".toUpperCase())
											.description(AvailableFormatType.ORIGINAL)
											.build());
								} else if (pv.equals("json") &&
										(op.getTemplate().contains("service=WFS") ||
												op.getMappingsByInstanceId().stream()
												.anyMatch(e -> e.getVariable().equals("service") && (map.getMappingParamvaluesById().stream().map(EDMMappingParamvalue::getParamvalue).collect(Collectors.toList()).contains("WFS") || e.getDefaultvalue().contains("WFS"))))) {
									formats.add(new AvailableFormat.AvailableFormatBuilder()
											.originalFormat(pv)
											.format("application/epos.geo+json")
											.href(EnvironmentVariables.API_HOST + API_PATH_EXECUTE + distribution.getMetaId() + API_FORMAT + "json")
											.label("GEOJSON (" + pv + ")")
											.description(AvailableFormatType.ORIGINAL)
											.build());
								} else if (pv.contains("geo%2Bjson")) {
									formats.add(new AvailableFormat.AvailableFormatBuilder()
											.originalFormat(pv)
											.format("application/epos.geo+json")
											.href(EnvironmentVariables.API_HOST + API_PATH_EXECUTE + distribution.getMetaId() + API_FORMAT + pv)
											.label("GEOJSON (" + pv + ")")
											.description(AvailableFormatType.ORIGINAL)
											.build());
								} else if (
										((pv.toLowerCase().contains("geojson") || pv.toLowerCase().contains("geo+json") || pv.toLowerCase().contains("geo-json")) 
										|| (pv.toLowerCase().contains("epos.table.geo+json") || pv.toLowerCase().contains("epos.map.geo+json")))
										&& op.getSoftwareapplicationOperationsByInstanceId().stream().map(EDMSoftwareapplicationOperation::getSoftwareapplicationByInstanceSoftwareapplicationId).collect(Collectors.toList()).isEmpty()
									) {
									formats.add(new AvailableFormat.AvailableFormatBuilder()
											.originalFormat(pv)
											.format("application/epos.geo+json")
											.href(EnvironmentVariables.API_HOST + API_PATH_EXECUTE + distribution.getMetaId() + API_FORMAT + pv)
											.label("GEOJSON (" + pv + ")")
											.description(AvailableFormatType.ORIGINAL)
											.build());
								} else {
									formats.add(new AvailableFormat.AvailableFormatBuilder()
											.originalFormat(pv)
											.format(pv)
											.href(EnvironmentVariables.API_HOST + API_PATH_EXECUTE + distribution.getMetaId() + API_FORMAT + pv)
											.label(pv.toUpperCase())
											.description(AvailableFormatType.ORIGINAL)
											.build());
								}
							}
						}
					}
					if (op.getOperationReturnsByInstanceId() != null && formats.isEmpty()) {
						for (String returns : op.getOperationReturnsByInstanceId().stream().map(EDMOperationReturns::getReturns).collect(Collectors.toList())) {
							if (returns.contains("geojson") || returns.contains("geo+json")) {
								formats.add(new AvailableFormat.AvailableFormatBuilder()
										.originalFormat(returns)
										.format("application/epos.geo+json")
										.href(EnvironmentVariables.API_HOST + API_PATH_EXECUTE + distribution.getMetaId() + API_FORMAT + returns)
										.label("GEOJSON")
										.description(AvailableFormatType.ORIGINAL)
										.build());
							} else {
								formats.add(new AvailableFormat.AvailableFormatBuilder()
										.originalFormat(returns)
										.format(returns)
										.href(EnvironmentVariables.API_HOST + API_PATH_EXECUTE + distribution.getMetaId() + API_FORMAT + returns)
										.label(returns.toUpperCase())
										.description(AvailableFormatType.ORIGINAL)
										.build());
							}
						}
					}
					if (!isWMS && op.getSoftwareapplicationOperationsByInstanceId() != null) {
						for (EDMSoftwareapplication s : op.getSoftwareapplicationOperationsByInstanceId().stream().map(EDMSoftwareapplicationOperation::getSoftwareapplicationByInstanceSoftwareapplicationId).collect(Collectors.toList())) {
							if (s.getSoftwareapplicationParametersByInstanceId() != null) {
								ArrayList<Parameter> parameterList = new ArrayList<>();
								s.getSoftwareapplicationParametersByInstanceId().stream()
								.map(elem -> {
									Parameter parameter = new Parameter();
									parameter.setEncodingFormat(elem.getEncodingformat());
									parameter.setAction(Parameter.ActionEnum.fromValue(elem.getAction()));
									parameter.setConformsTo(elem.getConformsto());
									return parameter;
								})
								.forEach(parameterList::add);

								for (Parameter param : parameterList) {
									if (param.getEncodingFormat().equals("application/epos.geo+json")
											|| param.getEncodingFormat().equals("application/epos.table.geo+json")
											|| param.getEncodingFormat().equals("application/epos.map.geo+json")) {

										formats.add(new AvailableFormat.AvailableFormatBuilder()
												.originalFormat(param.getEncodingFormat())
												.format(param.getEncodingFormat())
												.href(EnvironmentVariables.API_HOST + API_PATH_EXECUTE + distribution.getMetaId() + API_FORMAT + param.getEncodingFormat())
												.label("GEOJSON")
												.description(AvailableFormatType.CONVERTED)
												.build());
									}
									if (param.getEncodingFormat().equals("covjson")) {
										formats.add(new AvailableFormat.AvailableFormatBuilder()
												.originalFormat(param.getEncodingFormat())
												.format(param.getEncodingFormat())
												.href(EnvironmentVariables.API_HOST + API_PATH_EXECUTE + distribution.getMetaId() + API_FORMAT + param.getEncodingFormat())
												.label("COVJSON")
												.description(AvailableFormatType.CONVERTED)
												.build());
									}
								}
							}
						}
					}
				}
			}
		}
		if(distribution.getAccessURLByInstanceId()!=null && webserviceByAccessService ==null && distribution.getAccessURLByInstanceId().size() > 0) {
			String[] uri = distribution.getFormat().split("\\/");
			String format = uri[uri.length-1];
			formats.add(new AvailableFormat.AvailableFormatBuilder()
					.originalFormat(format)
					.format(format)
					.href(new ArrayList<>(distribution.getAccessURLByInstanceId()).get(0).getInstanceOperationId())
					.label(format.toUpperCase())
					.description(AvailableFormatType.ORIGINAL)
					.build());
		}
		return formats;
	}


}
