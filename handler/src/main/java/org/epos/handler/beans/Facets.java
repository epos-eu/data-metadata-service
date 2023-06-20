package org.epos.handler.beans;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.epos.eposdatamodel.Category;
import org.epos.eposdatamodel.CategoryScheme;
import org.epos.handler.dbapi.dbapiimplementation.CategoryDBAPI;
import org.epos.handler.dbapi.dbapiimplementation.CategorySchemeDBAPI;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Facets {

	private static File[] getResourceFolderFiles(String folder) {
		File[] files = new File(folder).listFiles();
		Arrays.sort(files);
		return files;
	}

	private Facets() {}

	public static JsonObject getFacets() throws IOException {
		Gson gson = new Gson();
		JsonObject facetsObject = new JsonObject();
		JsonArray domainsFacets = new JsonArray();
		JsonObject json;

		for (File f : getResourceFolderFiles("facets")) {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8));
			json = gson.fromJson(bufferedReader, JsonArray.class).get(0).getAsJsonObject();
			domainsFacets.add(json);
		}


		facetsObject.addProperty("name", "domains");
		facetsObject.add("children", domainsFacets);

		return facetsObject;
	}

	public static JsonObject getFacetsFromDatabase() throws IOException {
		JsonArray domainsFacets = new JsonArray();
		JsonObject facetsObject = new JsonObject();
		
		CategorySchemeDBAPI schemes = new CategorySchemeDBAPI();
		CategoryDBAPI categories = new CategoryDBAPI();

		List<CategoryScheme> categorySchemesList = schemes.getAll().stream().filter(e->e.getUid().contains("category:")).collect(Collectors.toList());
		List<Category> categoriesList = categories.getAll().stream().filter(e->e.getUid().contains("category:")).collect(Collectors.toList());

		for(CategoryScheme sch : categorySchemesList) {
			JsonObject facetDomain = new JsonObject();

			facetDomain.addProperty("name", sch.getTitle());
			facetDomain.add("children", recursiveChildren(categoriesList, sch.getUid() ,null));
			domainsFacets.add(facetDomain);
		}
		facetsObject.addProperty("name", "domains");
		facetsObject.add("children", domainsFacets);

		return facetsObject;
	}

	public static JsonArray recursiveChildren(List<Category> categoriesList, String domain, String father) {
		CategorySchemeDBAPI schemes = new CategorySchemeDBAPI();
		JsonArray children = new JsonArray();
		if(father==null) {
			for(Category cat : categoriesList) {
				if(cat.getInScheme().equals(schemes.getByUid(domain).get(0).getInstanceId())) {
					if(cat.getBroader()==null) {
						JsonObject facetsObject = new JsonObject();
						facetsObject.addProperty("name", cat.getDescription());
						facetsObject.add("children", recursiveChildren(categoriesList, domain, cat.getInstanceId()));
						children.add(facetsObject);
					}
				}
			}
		} else {
			for(Category cat : categoriesList) {
				if(cat.getInScheme().equals(schemes.getByUid(domain).get(0).getInstanceId())) {
					if(cat.getBroader()!=null && cat.getBroader().equals(father)) {
						JsonObject facetsObject = new JsonObject();
						if(cat.getNarrower() == null) {
							facetsObject.addProperty("name", cat.getDescription());
							facetsObject.addProperty("ddss", cat.getUid());
						} else {
							facetsObject.addProperty("name", cat.getDescription());
							facetsObject.add("children", recursiveChildren(categoriesList, domain, cat.getInstanceId()));
						}
						children.add(facetsObject);
					}
				}
			}
		}

		return children;

	}
}
