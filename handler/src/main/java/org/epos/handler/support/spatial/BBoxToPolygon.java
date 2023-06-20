package org.epos.handler.support.spatial;

import com.google.gson.JsonObject;

public class BBoxToPolygon {

	private static final String NORTH_LAT = "epos:northernmostLatitude";
	private static final String EAST_LON  = "epos:easternmostLongitude";
	private static final String SOUTH_LAT = "epos:southernmostLatitude";
	private static final String WEST_LON  = "epos:westernmostLongitude";

	public static String transform(JsonObject messageObject) {
		String polyString ="POLYGON((";
		polyString+=String.valueOf(messageObject.get(EAST_LON).getAsDouble());
		polyString+=" "+String.valueOf(messageObject.get(NORTH_LAT).getAsDouble());
		polyString+=", "+String.valueOf(messageObject.get(EAST_LON).getAsDouble());
		polyString+=" "+String.valueOf(messageObject.get(SOUTH_LAT).getAsDouble());
		polyString+=", "+String.valueOf(messageObject.get(WEST_LON).getAsDouble());
		polyString+=" "+String.valueOf(messageObject.get(SOUTH_LAT).getAsDouble());
		polyString+=", "+String.valueOf(messageObject.get(WEST_LON).getAsDouble());
		polyString+=" "+String.valueOf(messageObject.get(NORTH_LAT).getAsDouble());
		polyString+=", "+String.valueOf(messageObject.get(EAST_LON).getAsDouble());
		return polyString+=" "+String.valueOf(messageObject.get(NORTH_LAT).getAsDouble())+"))";

	}

}
