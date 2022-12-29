package com.r2dsolution.comein.pegasus.config;

import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.TransactionManager;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.r2dsolution.comein.pegasus.util.SecretManagerUtils;


@Configuration
@PropertySource("classpath:aws.properties")
@PropertySource("classpath:comein.properties")
@EnableJdbcRepositories("com.r2dsolution.comein.repository")
public class DatabaseConfig extends  AbstractJdbcConfiguration { 
	
	@Value( "${accessKey}" )
	public String accessKey;
	
	@Value( "${secretKey}" )
	public String secretKey;
	
	
	@Value( "${region}" )
	public String region;
	
	@Value( "${comein.db.driver}" )
	public String driver;
	
	@Value( "${comein.mode}" )
	public String mode;
	
	
	

	@Bean
    DataSource dataSource(AWSSecretsManager secretManager) {  
		Map<String,String> awsSecrets = SecretManagerUtils.getSecret(secretManager, mode+"/db/postgresql/comein");
		String host = awsSecrets.get("host");
		String port = awsSecrets.get("port");
		String database = awsSecrets.get("dbInstance");
		String username = awsSecrets.get("username");
		String password = awsSecrets.get("password");
		
		String url =  "jdbc:postgresql://"+host+":+"+port+"/"+database;
		System.out.println("url="+url);
		
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driver);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        return dataSource;
	
    }

    @Bean
    NamedParameterJdbcOperations namedParameterJdbcOperations(DataSource dataSource) { 
        return new NamedParameterJdbcTemplate(dataSource);
    }

    
   
    @Bean
    TransactionManager transactionManager(DataSource dataSource) {                     
        return new DataSourceTransactionManager(dataSource);
    }
    
    @Bean
    public AWSCredentialsProvider initCredentialsProvider() {
    	
    	BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey,secretKey);
    	
    	
    	return new AWSStaticCredentialsProvider(awsCreds);
    }

    
    @Bean 
    public AWSSecretsManager initAWSSecretsManager() {
    	
    	 AWSSecretsManager client  = AWSSecretsManagerClientBuilder.standard()
					.withCredentials(initCredentialsProvider()) 
                 .withRegion(region)
                 .build();
    	 return client;
    }
    

}
