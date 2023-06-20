package org.epos.handler.support.facets;

import java.util.ArrayList;
import java.util.List;

import org.epos.handler.beans.DiscoveryItem;

public class Node
{
    private List<Node> children = null;
	private List<DiscoveryItem> distributions = null;
    private String ddss = null;
    private String id = null;
    private String name;

    public Node(String value)
    {
        this.children = new ArrayList<>();
        this.name = value;
    }

    public void addChild(Node child)
    {
        children.add(child);
    }

	public List<DiscoveryItem> getDistributions() {
		if(distributions==null) {
			distributions = new ArrayList<DiscoveryItem>();
		}
		return distributions;
	}

	public void setDistributions(List<DiscoveryItem> distributions) {
		this.distributions = distributions;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getDdss() {
		return ddss;
	}

	public void setDdss(String ddss) {
		this.ddss = ddss;
	}
	
    public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<Node> getChildren() {
		return children;
	}

	public void setChildren(List<Node> children) {
		this.children = children;
	}

	@Override
	public String toString() {
		return "Node [children=" + children + ", distributions=" + distributions + ", ddss=" + ddss + ", id=" + id
				+ ", name=" + name + "]";
	}


}