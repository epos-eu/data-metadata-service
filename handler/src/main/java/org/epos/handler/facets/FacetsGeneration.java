package org.epos.handler.facets;

import static org.epos.handler.support.Utils.gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.epos.handler.beans.DiscoveryItem;

import com.google.gson.JsonObject;

public class FacetsGeneration {
	
	public static JsonObject generateOnlyFacetsTree(JsonObject response,ArrayList<DiscoveryItem> discoveryList) {
		FacetsNodeTree fnt = new FacetsNodeTree(true);
		response.add("results",gson.toJsonTree(fnt.getFacets()));
		return response;
	}

	public static JsonObject generateResponseUsingCategories(JsonObject response,ArrayList<DiscoveryItem> discoveryList) {
		FacetsNodeTree fnt = new FacetsNodeTree(true);
		fnt.getNodes().forEach(node -> {
			List<DiscoveryItem> distributionsItem = new ArrayList<>();
			for(DiscoveryItem dp : discoveryList) {
				if(dp.getDataproductCategories() == null) continue;//System.err.println(dp.getTitle());
				else {
					if(node.getDdss()!=null && dp.getDataproductCategories().contains(node.getDdss())){
						if(!distributionsItem.stream().anyMatch(p -> p.getId().equals(dp.getId())))
							distributionsItem.add(dp);
					}
				}
			}
			node.setDistributions(new ArrayList<>());
			node.getDistributions().addAll(distributionsItem);
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
