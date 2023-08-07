/*******************************************************************************
 * Copyright 2021 EPOS ERIC
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.epos.handler.main;

import static org.epos.handler.dbapi.util.LoadCache.loadCache;

import java.io.IOException;
import java.util.Optional;

import org.epos.handler.RequestHandler;
import org.epos.handler.beans.Facets;
import org.epos.handler.constants.EnvironmentVariables;
import org.epos.handler.operations.monitoring.ZabbixExecutor;
import org.epos.router_framework.RelayRouter;
import org.epos.router_framework.RelayRouterBuilder;
import org.epos.router_framework.domain.Actor;
import org.epos.router_framework.domain.BuiltInActorType;
import org.epos.router_framework.exception.RoutingException;
import org.epos.router_framework.types.ServiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class App implements CommandLineRunner
{
	private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
	
	
	@Autowired
	private RelayRouter dbRelayRouter;


	public static void main(String[] args) {
		new SpringApplication(App.class).run(args);
	}

	@Bean
    public RelayRouter router() 
    {
		LOGGER.info("Create connection to RabbitMQ");
		int threads = (Runtime.getRuntime().availableProcessors()*2)-1;

		int numOfPublishers = System.getenv("NUM_OF_PUBLISHERS")!=null? Integer.parseInt(System.getenv("NUM_OF_PUBLISHERS")) : 2;
		int numOfConsumers = System.getenv("NUM_OF_CONSUMERS")!=null? Integer.parseInt(System.getenv("NUM_OF_CONSUMERS")) : threads-numOfPublishers;

		if(numOfConsumers<1) numOfConsumers=2;
		if(numOfPublishers<1) numOfPublishers=2;

		
		Optional<RelayRouter> dbRouter = RelayRouterBuilder.instance(Actor.getInstance(BuiltInActorType.DB_CONNECTOR.verbLabel()).get())
				.addServiceType(ServiceType.METADATA, Actor.getInstance(BuiltInActorType.WEB_API.verbLabel()).get())
				.addServiceType(ServiceType.SCHEDULER, Actor.getInstance(BuiltInActorType.BACK_OFFICE.verbLabel()).get())
				.addServiceType(ServiceType.INGESTOR, Actor.getInstance(BuiltInActorType.INGESTOR.verbLabel()).get())
				.addServiceType(ServiceType.WORKSPACE, Actor.getInstance(BuiltInActorType.WORKSPACE.verbLabel()).get())
				.addServiceType(ServiceType.EXTERNAL, Actor.getInstance(BuiltInActorType.TCS_CONNECTOR.verbLabel()).get())
				.addServiceType(ServiceType.SENDER, Actor.getInstance(BuiltInActorType.EMAIL_SENDER.verbLabel()).get())
				.addPlainTextPayloadTypeSupport(actor -> new RequestHandler(actor))
				.setNumberOfPublishers(numOfPublishers)
				.setNumberOfConsumers(numOfConsumers)
				.build();

		return dbRouter.orElseThrow(() -> new BeanInitializationException(
        		"Router instance for DBConnector component could not be instantiated"));
    }

	
	@Override
	public void run(String... arg0) {
		LOGGER.info("Starting Data Metadata Service");

		if(EnvironmentVariables.MONITORING.equals("true")) LOGGER.info("Monitoring enabled");
		else LOGGER.info("Monitoring disabled");
		
		dbRelayRouter = router();
       
       try {
    	   dbRelayRouter.init(EnvironmentVariables.BROKER_HOST,
					EnvironmentVariables.BROKER_VHOST,
					EnvironmentVariables.BROKER_USERNAME,
					EnvironmentVariables.BROKER_PASSWORD);
			LOGGER.debug("Initialising updater");
			loadCache();
		} catch (RoutingException e) {
			LOGGER.error("Error initialising router", e);
		}

		if(EnvironmentVariables.MONITORING.equals("true")) {
			ZabbixExecutor.getInstance();
		}
	}

}
