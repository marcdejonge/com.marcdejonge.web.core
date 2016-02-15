package nl.jonghuis.web.core;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(description = "This is the configuration for the HTTP server that will be started")
public @interface HttpConfiguration {
	@AttributeDefinition(min = "1",
	                     max = "65535",
	                     description = "The port on which the server will listen for HTTP requests")
	int httpPort() default 80;

	@AttributeDefinition(min = "0",
	                     max = "65535",
	                     description = "The port on which the server will listen for Secure HTTP requests. 0 will disable HTTPS.")
	int httpsPort() default 443;

	@AttributeDefinition(description = "If this is set to true, any HTTP request will be forwarded to the same URL on HTTPS")
	boolean alwaysSwitchToHttps() default true;

	@AttributeDefinition(min = "0",
	                     max = "128",
	                     description = "The number of threads that wil be busy accepting new incoming connection. Using 0 means it will guess using the nr of avialable CPUs.")
	int nrOfConnectionHandlers() default 0;

	@AttributeDefinition(min = "0",
	                     max = "1024",
	                     description = "The number of threads that wil be handling HTTP requests. Using 0 means it will guess using the nr of avialable CPUs.")
	int nrOfWorkers() default 0;

	@AttributeDefinition(description = "The private chain file that will be used for SSL (PEM format). If left empty or file does not exist, SSL will be disabled.")
	String sslChainFile() default "";

	@AttributeDefinition(description = "The private key file that will be used for SSL (PEM format). If left empty or file does not exist, SSL will be disabled.")
	String sslKeyFile() default "";
}
