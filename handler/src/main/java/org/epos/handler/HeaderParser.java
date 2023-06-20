package org.epos.handler;

import static org.epos.handler.support.Utils.gson;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.epos.handler.enums.APIActionType;
import org.epos.handler.enums.DataOriginType;
import org.epos.handler.enums.OperationType;
import org.epos.handler.enums.RequestDomainType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class HeaderParser {

	private static final Logger LOGGER = LoggerFactory.getLogger(HeaderParser.class); 

	private OperationType operation;
	private DataOriginType origin;
	private RequestDomainType domain;
	private String object;

	public HeaderParser(HeaderParserBuilder builder) {
		this.operation = builder.operation;
		this.origin = builder.origin;
		this.domain = builder.domain;
		this.object = builder.object;
	}

	public OperationType getOperation() {
		return operation;
	}

	public void setOperation(OperationType operation) {
		this.operation = operation;
	}

	public DataOriginType getOrigin() {
		return origin;
	}

	public void setOrigin(DataOriginType origin) {
		this.origin = origin;
	}

	public RequestDomainType getDomain() {
		return domain;
	}

	public void setDomain(RequestDomainType domain) {
		this.domain = domain;
	}

	public String getObject() {
		return object;
	}

	public void setObject(String object) {
		this.object = object;
	}



	@Override
	public String toString() {
		return "HeaderParser [operation=" + operation + ", origin=" + origin + ", domain=" + domain + ", object="
				+ object + "]";
	}



	public static class HeaderParserBuilder{

		private OperationType operation;
		private DataOriginType origin;
		private RequestDomainType domain;
		private String object;


		public HeaderParserBuilder() {}

		public HeaderParserBuilder(Map<String, Object> header) {

			operation(OperationType.getInstance(header.get("epos_operation-type").toString()).get());

			try {
				Type empMapType = new TypeToken<Map<String, String>>() {}.getType();
				Map<String, String> requestType = gson.fromJson(header.get("epos_request-type").toString(), empMapType);

				origin(DataOriginType.getInstance(requestType.get("origin").toString()).get());
				domain(RequestDomainType.getInstance(requestType.get("domain").toString()).get());
				object(Optional.ofNullable(requestType.get("object")).orElse("null"));
			} catch(JsonSyntaxException jse) {
				LOGGER.warn(jse.getLocalizedMessage());
				List<String> tokens = Arrays.asList(header.get("epos_request-type").toString().split("\\."));
				tokens.forEach(token -> {
					if(DataOriginType.getInstance(token).isPresent()) {
						origin(DataOriginType.getInstance(token).get());
					}
					if(RequestDomainType.getInstance(token).isPresent()) {
						domain(RequestDomainType.getInstance(token).get());
					}
					if(APIActionType.getInstance(token).isPresent()) {
						object(APIActionType.getInstance(token).get().toString());
					}
				});
			}
		}

		public HeaderParserBuilder operation(OperationType operation) {
			this.operation = operation;
			return this;
		}

		public HeaderParserBuilder origin(DataOriginType origin) {
			this.origin = origin;
			return this;
		}

		public HeaderParserBuilder domain(RequestDomainType domain) {
			this.domain = domain;
			return this;
		}

		public HeaderParserBuilder object(String object) {
			this.object = object;
			return this;
		}

		public HeaderParser build() {
			return new HeaderParser(this);
		}
	}
}
