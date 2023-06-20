package org.epos.handler.main;

import org.epos.eposdatamodel.State;
import org.epos.handler.dbapi.dbapiimplementation.DataProductDBAPI;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class DBHealthIndicator 
implements HealthIndicator {

	private final String message_key = "DB Connection service";

	@Override
	public Health health() {
		try {
			new DataProductDBAPI().getAllByState(State.PUBLISHED);
			return Health.up().withDetail(message_key, "Available").build();
		}catch(Exception e) {
			return Health.down().withDetail(message_key, "Not Available").build();
		}
	}

}
