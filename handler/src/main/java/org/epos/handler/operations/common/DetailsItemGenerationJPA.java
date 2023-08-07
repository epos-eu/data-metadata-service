package org.epos.handler.operations.common;

import com.google.gson.JsonObject;

import org.epos.eposdatamodel.ContactPoint;
import org.epos.eposdatamodel.LinkedEntity;
import org.epos.eposdatamodel.State;
import org.epos.handler.HeaderParser;
import org.epos.handler.beans.DataServiceProvider;
import org.epos.handler.beans.ServiceParameter;
import org.epos.handler.beans.SpatialInformation;
import org.epos.handler.beans.TemporalCoverage;
import org.epos.handler.constants.EnvironmentVariables;
import org.epos.handler.dbapi.dbapiimplementation.ContactPointDBAPI;
import org.epos.handler.dbapi.model.*;
import org.epos.handler.dbapi.service.DBService;
import org.epos.handler.enums.DataOriginType;
import org.epos.handler.support.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import static org.epos.handler.dbapi.util.DBUtil.getFromDB;
import static org.epos.handler.support.Utils.gson;

public class DetailsItemGenerationJPA {

    private static final Logger LOGGER = LoggerFactory.getLogger(DetailsItemGenerationJPA.class);

    private static final String API_PATH_DETAILS = EnvironmentVariables.API_CONTEXT + "/resources/details?id=";
    private static final String EMAIL_SENDER = EnvironmentVariables.API_CONTEXT + "/sender/send-email?id=";

    public static JsonObject generate(JsonObject response, JsonObject parameters, HeaderParser header) {

        JsonObject params = (parameters != null && parameters.has("params")) ? gson.fromJson(parameters.get("params").getAsString(), JsonObject.class) : new JsonObject();

        LOGGER.info("Parameters {}", params);

        EntityManager em = new DBService().getEntityManager();

        List<EDMDistribution> distributionSelectedList = getFromDB(em, EDMDistribution.class,
                "distribution.findAllByMetaId",
                "METAID", parameters.get("id").getAsString());

        if (distributionSelectedList.stream().noneMatch(distrSelected -> distrSelected.getState().equals("PUBLISHED")))
            return new JsonObject();

        EDMDistribution distributionSelected = distributionSelectedList.stream().filter(distrSelected -> distrSelected.getState().equals("PUBLISHED")).collect(Collectors.toList()).get(0);

        if (distributionSelected == null) return new JsonObject();

        EDMDataproduct dp;
        if (distributionSelected.getIsDistributionsByInstanceId() != null &&
                !distributionSelected.getIsDistributionsByInstanceId().isEmpty()) {
            dp = distributionSelected.getIsDistributionsByInstanceId().stream()
                    .map(EDMIsDistribution::getDataproductByInstanceDataproductId)
                    .filter(edmDataproduct -> edmDataproduct.getState().equals(State.PUBLISHED.toString()))
                    .findFirst().orElse(null);
            if (dp == null) return new JsonObject();
        } else {
            return new JsonObject();
        }

        EDMWebservice ws = distributionSelected.getWebserviceByAccessService() != null && distributionSelected.getWebserviceByAccessService().getState().equals(State.PUBLISHED.toString()) ?
                distributionSelected.getWebserviceByAccessService() : null;
        if (ws == null && distributionSelected.getAccessService() != null) return new JsonObject();

        EDMOperation op;
        if (distributionSelected.getDistributionAccessurlsByInstanceId() != null) {
            op = getFromDB(em, EDMOperation.class, "operation.findByListOfUidAndState",
                    "LIST", distributionSelected.getDistributionAccessurlsByInstanceId().stream()
                            .map(EDMDistributionAccessurl::getAccessurl).collect(Collectors.toList()),
                    "STATE", State.PUBLISHED.toString())
                    .stream().findFirst().orElse(null);
        } else {
            return new JsonObject();
        }
        if (op == null && distributionSelected.getAccessService() != null) return new JsonObject();


        org.epos.handler.beans.Distribution distribution = new org.epos.handler.beans.Distribution();

        if (distributionSelected.getDistributionTitlesByInstanceId() != null) {
            distribution.setTitle(
                    Optional.of(
                            distributionSelected.getDistributionTitlesByInstanceId().stream()
                                    .map(EDMDistributionTitle::getTitle).collect(Collectors.joining("."))
                    ).orElse(null)
            );
        }

        if (distributionSelected.getType() != null) {
            String[] type = distributionSelected.getType().split("\\/");
            distribution.setType(type[type.length - 1]);
        }

        if (distributionSelected.getDistributionDescriptionsByInstanceId() != null) {
            distribution.setDescription(
                    Optional.of(
                            distributionSelected.getDistributionDescriptionsByInstanceId().stream()
                                    .map(EDMDistributionDescription::getDescription).collect(Collectors.joining("."))
                    ).orElse(null)
            );
        }

        distribution.setId(Optional.ofNullable(distributionSelected.getMetaId()).orElse(null));
        distribution.setUid(Optional.ofNullable(distributionSelected.getUid()).orElse(null));

        if (distributionSelected.getDistributionDownloadurlsByInstanceId() != null) {
            distribution.setDownloadURL(
                    Optional.of(
                            distributionSelected.getDistributionDownloadurlsByInstanceId().stream()
                                    .map(EDMDistributionDownloadurl::getDownloadurl).collect(Collectors.joining("."))
                    ).orElse(null)
            );
        }

        if (distributionSelected.getDistributionAccessurlsByInstanceId() != null) {
            distribution.setEndpoint(
                    Optional.of(
                            distributionSelected.getDistributionAccessurlsByInstanceId().stream()
                                    .map(EDMDistributionAccessurl::getAccessurl).collect(Collectors.joining("."))
                    ).orElse(null)
            );
        }

        distribution.setLicense(Optional.ofNullable(distributionSelected.getLicense()).orElse(null));

        // DATASET INFO
        ArrayList<String> internalIDs = new ArrayList<>();
        List<String> doi = new ArrayList<>();
        dp.getDataproductIdentifiersByInstanceId().forEach(identifier -> {
            if (identifier.getType().equals("DOI")) {
                doi.add(identifier.getIdentifier());
                distribution.setDOI(doi);
            }
            if (identifier.getType().equals("DDSS-ID")) {
                String ddss = identifier.getIdentifier();
                if (ddss != null) internalIDs.add(ddss);
            }
        });
      
        if(!dp.getContactpointDataproductsByInstanceId().isEmpty()) {
        	distribution.setDataprovidersEmailhref(EnvironmentVariables.API_HOST + API_PATH_DETAILS + String.join(",",dp.getContactpointDataproductsByInstanceId().stream().map(EDMContactpointDataproduct::getInstanceContactpointId).collect(Collectors.toList())));
        }


        distribution.setFrequencyUpdate(Optional.ofNullable(dp.getAccrualperiodicity()).orElse(null));
        distribution.setHref(EnvironmentVariables.API_HOST + API_PATH_DETAILS + distributionSelected.getMetaId());
        distribution.setInternalID(internalIDs);

        Set<String> keywords = new HashSet<>(Arrays.stream(Optional.ofNullable(dp.getKeywords()).orElse("").split(",\t")).map(String::toLowerCase).map(String::trim).collect(Collectors.toList()));
        if (ws != null)
            keywords.addAll(Arrays.stream(Optional.ofNullable(ws.getKeywords()).orElse("").split(",\t")).map(String::toLowerCase).map(String::trim).collect(Collectors.toList()));
        keywords.removeAll(Collections.singleton(null));
        keywords.removeAll(Collections.singleton(""));
        distribution.setKeywords(new ArrayList<>(keywords));

        if (dp.getDataproductSpatialsByInstanceId() != null) {
            for (EDMDataproductSpatial s : dp.getDataproductSpatialsByInstanceId())
                distribution.getSpatial().addPaths(SpatialInformation.doSpatial(s.getLocation()), SpatialInformation.checkPoint(s.getLocation()));
        }

        // how to handle multiple temporal? at the moment use only the first one
        TemporalCoverage tc = new TemporalCoverage();
        if (dp.getDataproductTemporalsByInstanceId() != null && dp.getDataproductTemporalsByInstanceId().size() > 0) {
            Timestamp startdate = new ArrayList<>(dp.getDataproductTemporalsByInstanceId()).get(0).getStartdate();
            Timestamp enddate = new ArrayList<>(dp.getDataproductTemporalsByInstanceId()).get(0).getEnddate();
            String startDate;
            String endDate;

            if (startdate != null) {
                startDate = startdate.toString().replace(".0", "Z").replace(" ", "T");
                if (!startDate.contains("Z")) startDate = startDate + "Z";
            } else startDate = null;
            if (enddate != null) {
                endDate = enddate.toString().replace(".0", "Z").replace(" ", "T");
                if (!endDate.contains("Z")) endDate = endDate + "Z";
            } else endDate = null;

            tc.setStartDate(startDate);
            tc.setEndDate(endDate);
        }
        distribution.setTemporalCoverage(tc);

        if (dp.getPublishersByInstanceId() != null) {
            List<DataServiceProvider> dataProviders = getProviders(dp.getPublishersByInstanceId().stream().map(EDMPublisher::getEdmEntityIdByMetaOrganizationId).collect(Collectors.toList()));
            distribution.setDataProvider(dataProviders);
        }

        if (dp.getDataproductCategoriesByInstanceId() != null) {
            distribution.setScienceDomain(Optional.of(dp.getDataproductCategoriesByInstanceId().stream()
                    .map(EDMDataproductCategory::getCategoryByCategoryId)
                    .map(EDMCategory::getUid).collect(Collectors.toList())).orElse(null));
        }

        distribution.setHasQualityAnnotation(Optional.ofNullable(dp.getHasQualityAnnotation()).orElse(null));

        // WEBSERVICE INFO
        if (ws != null) {
            distribution.setServiceDescription(Optional.ofNullable(ws.getDescription()).orElse(null));

            if (ws.getWebserviceDocumentationsByInstanceId() != null) {
                distribution.setServiceDocumentation(Optional.of(ws.getWebserviceDocumentationsByInstanceId().stream()
                        .map(EDMWebserviceDocumentation::getDocumentation)
                        .collect(Collectors.joining("."))).orElse(null));
            }

            distribution.setServiceName(Optional.ofNullable(ws.getName()).orElse(null));

            if (ws.getEdmEntityIdByProvider() != null) {
                List<DataServiceProvider> serviceProviders = getProviders(List.of(ws.getEdmEntityIdByProvider()));
                if (!serviceProviders.isEmpty()){
                    distribution.setServiceProvider(serviceProviders.get(0));
                }
            }

            if (ws.getWebserviceSpatialsByInstanceId() != null) {
                for (EDMWebserviceSpatial s : ws.getWebserviceSpatialsByInstanceId())
                    distribution.getServiceSpatial().addPaths(SpatialInformation.doSpatial(s.getLocation()), SpatialInformation.checkPoint(s.getLocation()));
            }

            TemporalCoverage tcws = new TemporalCoverage();
            if (ws.getWebserviceTemporalsByInstanceId() != null && ws.getWebserviceTemporalsByInstanceId().size() > 0) {
                Timestamp startdate = new ArrayList<>(ws.getWebserviceTemporalsByInstanceId()).get(0).getStartdate();
                Timestamp enddate = new ArrayList<>(ws.getWebserviceTemporalsByInstanceId()).get(0).getEnddate();

                String startDate;
                String endDate;

                if (startdate != null) {
                    startDate = startdate.toString().replace(".0", "Z").replace(" ", "T");
                    if (!startDate.contains("Z")) startDate = startDate + "Z";
                } else startDate = null;
                if (enddate != null) {
                    endDate = enddate.toString().replace(".0", "Z").replace(" ", "T");
                    if (!endDate.contains("Z")) endDate = endDate + "Z";
                } else endDate = null;

                tcws.setStartDate(startDate);
                tcws.setEndDate(endDate);
            }
            distribution.setServiceTemporalCoverage(tcws);

            if (ws.getWebserviceCategoriesByInstanceId() != null) {
                distribution.setServiceType(Optional.of(ws.getWebserviceCategoriesByInstanceId().stream()
                        .map(EDMWebserviceCategory::getCategoryByCategoryId)
                        .map(EDMCategory::getUid).collect(Collectors.toList())).orElse(null));
            }
            
            if(!ws.getContactpointWebservicesByInstanceId().isEmpty()) {
            	distribution.setServiceprovidersEmailhref(EnvironmentVariables.API_HOST + EMAIL_SENDER + String.join(",",ws.getContactpointWebservicesByInstanceId().stream().map(EDMContactpointWebservice::getInstanceContactpointId).collect(Collectors.toList())));
            }

        }

        distribution.setParameters(new ArrayList<>());
        // OPERATION AND PARAMETERS
        if (Objects.nonNull(op)) {
            distribution.setEndpoint(op.getTemplate());
            distribution.setServiceEndpoint(op.getTemplate().split("\\{")[0]);
            distribution.setOperationid(op.getUid());
            if (op.getMappingsByInstanceId() != null) {
                for (EDMMapping mp : op.getMappingsByInstanceId()) {
                    ServiceParameter sp = new ServiceParameter();
                    sp.setDefaultValue(mp.getDefaultvalue());
                    sp.setEnumValue(
                            mp.getMappingParamvaluesById() != null ?
                                    mp.getMappingParamvaluesById().stream().map(EDMMappingParamvalue::getParamvalue).collect(Collectors.toList())
                                    : new ArrayList<>()
                    );
                    sp.setName(mp.getVariable());
                    sp.setMaxValue(mp.getMaxvalue());
                    sp.setMinValue(mp.getMinvalue());
                    sp.setLabel(mp.getLabel() != null ? mp.getLabel().replaceAll("@en", "") : null);
                    sp.setProperty(mp.getProperty());
                    sp.setRequired(mp.getRequired());
                    sp.setType(mp.getRange() != null ? mp.getRange().replace("xsd:", "") : null);
                    sp.setValue(null);
                    if (header.getOrigin().equals(DataOriginType.EXTERNALACCESS)) {
                        if (parameters.has("useDefaults") && parameters.get("useDefaults").getAsBoolean()) {
                            if (mp.getDefaultvalue() != null) {
                                if (mp.getProperty() != null && mp.getValuepattern() != null) {
                                    if (mp.getProperty().equals("schema:startDate") || mp.getProperty().equals("schema:endDate")) {
                                        try {
                                            sp.setValue(Utils.convertDateUsingPattern(mp.getDefaultvalue(), null, mp.getValuepattern()));
                                        } catch (ParseException e) {
                                            LOGGER.error(e.getLocalizedMessage());
                                        }
                                    }
                                } else sp.setValue(mp.getDefaultvalue());
                            } else sp.setValue(null);
                        } else {
                            if (params.has(mp.getVariable()) && !params.get(mp.getVariable()).getAsString().equals("")) {
                                if (mp.getProperty() != null && mp.getValuepattern() != null) {
                                    if (mp.getProperty().equals("schema:startDate") || mp.getProperty().equals("schema:endDate")) {
                                        try {
                                            sp.setValue(Utils.convertDateUsingPattern(params.get(mp.getVariable()).getAsString(), null, mp.getValuepattern()));
                                        } catch (ParseException e) {
                                            LOGGER.error(e.getLocalizedMessage());
                                        }
                                    }
                                } else if (mp.getProperty() == null && mp.getValuepattern() != null) {
                                    if (Utils.checkStringPattern(params.get(mp.getVariable()).getAsString(), mp.getValuepattern()))
                                        sp.setValue(params.get(mp.getVariable()).getAsString());
                                    else if (!Utils.checkStringPattern(params.get(mp.getVariable()).getAsString(), mp.getValuepattern()) && Utils.checkStringPatternSingleQuotes(mp.getValuepattern()))
                                        sp.setValue("'" + params.get(mp.getVariable()).getAsString() + "'");
                                    else sp.setValue(params.get(mp.getVariable()).getAsString()); //return new JsonObject();
                                } else sp.setValue(params.get(mp.getVariable()).getAsString());
                            } else sp.setValue(null);
                        }
                        if(sp.getValue()!=null)
							try {
								sp.setValue(URLEncoder.encode(sp.getValue(), StandardCharsets.UTF_8.toString()));
							} catch (UnsupportedEncodingException e) {
                                LOGGER.error(e.getLocalizedMessage());
							}
                    }
                    sp.setValuePattern(mp.getValuepattern());
                    sp.setVersion(null);
                    sp.setReadOnlyValue(mp.getReadOnlyValue());
                    sp.setMultipleValue(mp.getMultipleValues());
                    distribution.getParameters().add(sp);
                }
            }
        }

        distribution.setAvailableFormats(AvailableFormatsGeneration.generate(distributionSelected));
        
        em.close();

        return gson.toJsonTree(distribution).getAsJsonObject();
    }

    private static List<DataServiceProvider> getProviders(List<EDMEdmEntityId> organizationsCollection) {
        List<EDMOrganization> organizations = new ArrayList<>();
        for (EDMEdmEntityId edmMetaId : organizationsCollection) {
            if (edmMetaId.getOrganizationsByMetaId() != null && !edmMetaId.getOrganizationsByMetaId().isEmpty()) {
                ArrayList<EDMOrganization> list = edmMetaId.getOrganizationsByMetaId().stream()
                        .filter(e -> e.getState().equals(State.PUBLISHED.toString()))
                        .collect(Collectors.toCollection(ArrayList::new));
                organizations.addAll(list);
            }
        }

        List<DataServiceProvider> organizationStructure = new ArrayList<>();
        for (EDMOrganization org : organizations) {
            // only take into account the organization with legalname
            if (org.getOrganizationLegalnameByInstanceId() != null && !org.getOrganizationLegalnameByInstanceId().isEmpty()) {

                // se il data provider nei metadati è un figlio -> allora mostra in gui solo il figlio
                // se il data provider indicato nei metadati è un padre -> allora mostrare in gui il padre con tutti i suoi figli

                String mainOrganizationLegalName;
                List<DataServiceProvider> relatedOrganizations = new ArrayList<>();

                mainOrganizationLegalName = org.getOrganizationLegalnameByInstanceId().stream()
                        .map(EDMOrganizationLegalname::getLegalname)
                        .collect(Collectors.joining("."));

                if (Objects.nonNull(org.getSon()) && !org.getSon().isEmpty()) {
                    relatedOrganizations.addAll(
                            org.getSon().stream()
                                    .filter(relatedOrganization ->
                                            relatedOrganization.getOrganizationLegalnameByInstanceId() != null &&
                                                    !relatedOrganization.getOrganizationLegalnameByInstanceId().isEmpty())
                                    .map(relatedOrganization -> {

                                        String relatedOrganizationLegalName = relatedOrganization.getOrganizationLegalnameByInstanceId()
                                                .stream().map(EDMOrganizationLegalname::getLegalname)
                                                .collect(Collectors.joining("."));
                                        DataServiceProvider relatedDataprovider = new DataServiceProvider();
                                        relatedDataprovider.setDataProviderLegalName(relatedOrganizationLegalName);
                                        relatedDataprovider.setDataProviderUrl(relatedOrganization.getUrl());
                                        return relatedDataprovider;

                                    })
                                    .collect(Collectors.toList())
                    );
                }

                DataServiceProvider dataServiceProvider = new DataServiceProvider();
                dataServiceProvider.setDataProviderLegalName(mainOrganizationLegalName);
                dataServiceProvider.setRelatedDataProvider(relatedOrganizations);
                dataServiceProvider.setDataProviderUrl(org.getUrl());

                organizationStructure.add(dataServiceProvider);

            }

        }
        return organizationStructure;
    }
}