package com.r2dsolution.comein.pegasus.processor;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

//import com.amazonaws.regions.Region;

public class SecretManagerProcessor implements EnvironmentPostProcessor {


	private static Logger log = LoggerFactory.getLogger(SecretManagerProcessor.class);

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment env, SpringApplication application) {
//	    String secretName = "dev/db/postgresql/comein";
	    String region = "ap-southeast-1";
		System.out.println("loading........SecretManagerProcessor");
		try {
			Map<String,String> secrets = getSecret("dev","/db/postgresql/comein",region);
			
			Map props = new HashMap();
			props.put("DB_USERNAME", secrets.get("username"));
			props.put("DB_PASSWORD", secrets.get("password"));
			props.put("DB_HOST", secrets.get("host"));
			props.put("DB_PORT", secrets.get("port"));
			props.put("DB_INSTANCE", secrets.get("dbInstance"));
			
			MapPropertySource e = new MapPropertySource("SECRET_MANAGER",props);
			env.getPropertySources().addLast(e);
		
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	
	public Map<String,String> getSecret(String mode,String name,String region) throws Exception{

		String secretName = mode+name;


	    // Create a Secrets Manager client
	    AWSSecretsManager client  = AWSSecretsManagerClientBuilder.standard()
	    								.withCredentials(new ClasspathPropertiesFileCredentialsProvider("aws.properties")) 
	                                    .withRegion(region)
	                                    .build();
	    

	    
	    String secret, decodedBinarySecret;
	    GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
	                    .withSecretId(secretName);
	    GetSecretValueResult getSecretValueResult = null;

	   
	        getSecretValueResult = client.getSecretValue(getSecretValueRequest);
	    

		    // Decrypts secret using the associated KMS key.
		    // Depending on whether the secret is a string or binary, one of these fields will be populated.
		    if (getSecretValueResult.getSecretString() != null) {
		        secret = getSecretValueResult.getSecretString();
		    }
		    else {
		        decodedBinarySecret = new String(Base64.getDecoder().decode(getSecretValueResult.getSecretBinary()).array());
		    }
	
		    ObjectMapper mapper = new ObjectMapper();
		    TypeReference<HashMap<String,String>> typeRef 
	        	= new TypeReference<HashMap<String,String>>() {};
		 
		    System.out.println("name="+getSecretValueResult.getName());
		    Map<String,String> results = mapper.readValue(getSecretValueResult.getSecretString(),typeRef);
		   
		    return results;
	}
}
