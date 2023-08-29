package org.epos.handler.support.facets;

import java.util.ArrayList;
import java.util.List;

import org.epos.handler.beans.Facets;

import com.google.gson.JsonSyntaxException;
import static org.epos.handler.support.Utils.gson;

public class FacetsNodeTree {

	private Node facets;
	private List<Node> nodes;

	public FacetsNodeTree(Boolean fromDatabase) {
		try {
			if(fromDatabase) {
				this.facets = gson.fromJson(Facets.getInstance().getFacetsFromDatabase(), Node.class);
				nodes = returnAllNodes(facets);
			}else {
				this.facets = gson.fromJson(Facets.getInstance().getFacetsStatic(), Node.class);
				nodes = returnAllNodes(facets);
			}
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		}
	}

	public Node getFacets() {
		return facets;
	}

	public void setFacets(Node facets) {
		this.facets = facets;
	}

	public static List<Node> returnAllNodes(Node node){
		List<Node> listOfNodes = new ArrayList<Node>();
		addAllNodes(node, listOfNodes);
		return listOfNodes;
	}

	private static void addAllNodes(Node node, List<Node> listOfNodes) {
		if (node != null) {
			listOfNodes.add(node);
			List<Node> children = node.getChildren();
			if (children != null) {
				for (Node child: children) {
					addAllNodes(child, listOfNodes);
				}
			}
		}
	}

	public List<Node> getNodes() {
		nodes = returnAllNodes(facets);
		return nodes;
	}

	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}

	public Node removeEmptyLeafs(Node root) {
		if(root.getChildren()!=null) {
			for(int i = 0; i<root.getChildren().size(); i++) {
				Node child = removeEmptyLeafs(root.getChildren().get(i));
				if(child == null) {
					root.getChildren().remove(i);
					i--;
				}
				else if(child.getChildren()==null && child.getDistributions().isEmpty()) {
					root.getChildren().remove(i);
					i--;
				}
			}
		}

		if((root.getChildren()==null || root.getChildren().isEmpty()) && root.getDistributions().isEmpty())
			return null;

		return root;
	}
}
