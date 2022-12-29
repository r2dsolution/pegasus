package com.r2dsolution.comein.pegasus.util;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SecretManagerUtils {
	
	public static String initSecretName(String env,String name) {
		return env+name;
	}
	
	public static Regions initRegion(String name) {
		if (name!=null && name.trim().equals("ap-southeast-1")) {
			return Regions.AP_SOUTHEAST_1;
		};
		return Regions.US_EAST_2;
	}
	
	public static Map<String,String> getSecret(AWSSecretsManager client,String secretName) {

		//String secretName = mode+name;


	    // Create a Secrets Manager client
//	    AWSSecretsManager client  = AWSSecretsManagerClientBuilder.standard()
//	    								.withCredentials(new ClasspathPropertiesFileCredentialsProvider("aws.properties")) 
//	                                    .withRegion(region)
//	                                    .build();
	    
		try {
	    
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
		    
		} catch(Exception ex) {
			ex.printStackTrace();
			return new HashMap<String,String>();
		}
	}
}
