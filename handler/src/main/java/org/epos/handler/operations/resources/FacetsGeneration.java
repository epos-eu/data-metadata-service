package org.epos.handler.operations.resources;

import static org.epos.handler.support.Utils.gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.epos.handler.beans.DiscoveryItem;
import org.epos.handler.beans.OrganizationBean;
import org.epos.handler.support.facets.FacetsNodeTree;
import org.epos.handler.support.facets.Node;

import com.google.gson.JsonObject;

public class FacetsGeneration {

	public static JsonObject generateResponseUsingStaticJson(JsonObject response,ArrayList<DiscoveryItem> discoveryList) {
		FacetsNodeTree fnt = new FacetsNodeTree(false);
		fnt.getNodes().forEach(node -> {
			if(node.getChildren()!=null && node.getChildren().size()!=0) {
				for(Node child : node.getChildren()) {
					if(child.getDdss()!=null) {
						List<DiscoveryItem> distributionsItem = new ArrayList<>();
						List<String> ddssS =Arrays.asList(child.getDdss().replaceAll("\\s+","").split("\\,"));
						for(DiscoveryItem dp : discoveryList) {
							if(ddssS.contains(dp.getDdss())){
								distributionsItem.add(dp);
							}
						}
						if (distributionsItem.size() > 0) {
							node.getDistributions().addAll(distributionsItem);
						}
					}
					HashMap<String, DiscoveryItem> mapDistributions = new HashMap<>();
					for(DiscoveryItem dx : node.getDistributions()) {
						if(!mapDistributions.containsKey(dx.getId())){
							mapDistributions.put(dx.getId(), dx);
						}
					}
					node.setDistributions(new ArrayList<>());
					node.getDistributions().addAll(mapDistributions.values());
				}
			}
		});
		fnt.removeEmptyLeafs(fnt.getFacets());
		response.add("results",gson.toJsonTree(fnt.getFacets()));
		return response;
	}

	public static JsonObject generateResponseUsingCategories(JsonObject response,ArrayList<DiscoveryItem> discoveryList) {
		FacetsNodeTree fnt = new FacetsNodeTree(false);
		fnt.getNodes().forEach(node -> {
			if(node.getChildren()!=null && node.getChildren().size()!=0) {
				for(Node child : node.getChildren()) {
					if(child.getDdss()!=null) {
						List<DiscoveryItem> distributionsItem = new ArrayList<>();
						List<String> ddssS =Arrays.asList(child.getDdss().replaceAll("\\s+","").split("\\,"));
						for(DiscoveryItem dp : discoveryList) {
							if(ddssS.contains(dp.getDdss())){
								distributionsItem.add(dp);
							}
						}
						if (distributionsItem.size() > 0) {
							node.getDistributions().addAll(distributionsItem);
						}
					}
					HashMap<String, DiscoveryItem> mapDistributions = new HashMap<>();
					for(DiscoveryItem dx : node.getDistributions()) {
						if(!mapDistributions.containsKey(dx.getId())){
							mapDistributions.put(dx.getId(), dx);
						}
					}
					node.setDistributions(new ArrayList<>());
					node.getDistributions().addAll(mapDistributions.values());
				}
			}
		});
		fnt.removeEmptyLeafs(fnt.getFacets());
		response.add("results",gson.toJsonTree(fnt.getFacets()));
		return response;
	}

	public static JsonObject generateResponseUsingDataproviders(JsonObject response, ArrayList<DiscoveryItem> discoveryList) {

		HashMap<String, List<DiscoveryItem>> facets = new HashMap<String, List<DiscoveryItem>>();

		discoveryList.forEach(discoveryItem -> {
			if(discoveryItem.getDataprovider().isEmpty()) addToFacets(facets, "Undefined", discoveryItem);
			else {
				for(String org : discoveryItem.getDataprovider()) {
					addToFacets(facets, org, discoveryItem);
				}
			}
		});
		response.add("results",gson.toJsonTree(facets));
		return response;
	}

	public static JsonObject generateResponseUsingServiceproviders(JsonObject response, ArrayList<DiscoveryItem> discoveryList) {

		HashMap<String, List<DiscoveryItem>> facets = new HashMap<String, List<DiscoveryItem>>();

		discoveryList.forEach(discoveryItem -> {
			if(discoveryItem.getServiceprovider().isEmpty()) addToFacets(facets, "Undefined", discoveryItem);
			else {
				for(String org : discoveryItem.getServiceprovider()) {
					addToFacets(facets, org, discoveryItem);
				}
			}
		});
		response.add("results",gson.toJsonTree(facets));
		return response;
	}

	public static void addToFacets(HashMap<String, List<DiscoveryItem>> facets, String name, DiscoveryItem item) {
		if(facets.containsKey(name)) {
			facets.get(name).add(item);
		}else {
			facets.put(name, new ArrayList<DiscoveryItem>());
			facets.get(name).add(item);
		}
	}
}
