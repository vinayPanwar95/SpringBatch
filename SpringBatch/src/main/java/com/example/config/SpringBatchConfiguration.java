package com.example.config;



import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource ;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sound.sampled.Line;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.entity.User;

@Configuration
@EnableBatchProcessing
public class SpringBatchConfiguration {
	
	@Autowired
	public DataSource  dataSource;
	
	@Bean
	public Job job(JobBuilderFactory jobBuilderFactory,
			StepBuilderFactory stepBuilderFactory,
			ItemReader<User> itemReader,
			ItemProcessor<User, User> itemProcessor,
			ItemWriter<User> itemWriter) {
		
		Step step = stepBuilderFactory.get("ETL-file-load")
				.<User, User>chunk(100)
				.reader(itemReader)
				.processor(itemProcessor)
				.writer(itemWriter)
				.build();
		
		
		return jobBuilderFactory.get("ETL-Load")
		.incrementer(new RunIdIncrementer())
		.start(step)
		.build();
		
		
		 
		
	}
	
	@Bean 
	public DataSource dataSource() {
		final DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setUrl("jdbc:mysql://localhost:3306/mydatabse");
		dataSource.setUsername("root");
		dataSource.setPassword("root");
		return dataSource;
		
	}
	
	
	/*@Bean
	public FlatFileItemReader<User> fileItemReader(@Value("${input}") Resource resource){
		FlatFileItemReader<User>  fileItemReader = new FlatFileItemReader<>();
		fileItemReader.setResource(	resource);
		fileItemReader.setName("CSV-Reader");
		fileItemReader.setLinesToSkip(1);
		fileItemReader.setLineMapper(lineMapper());
		return fileItemReader;
	}*/
	
	public JdbcCursorItemReader<User> itemReader() {
		JdbcCursorItemReader<User> reader = new JdbcCursorItemReader<User>();
		reader.setDataSource(dataSource);
		reader.setSql("select id from user");
		reader.setRowMapper(new RowMapper<User>() {
			
			@Override
			public User mapRow(ResultSet rs, int rowNum) throws SQLException {
				 User user = new User();
				   user.setId(rs.getInt("id"));
				   user.setName(rs.getString("name"));
				
				return user;
			}
		});
		return reader;
	
	}
	
	
	
	@Bean
	public LineMapper<User> lineMapper(){
		DefaultLineMapper<User> defaultLineMapper = new DefaultLineMapper<>();
		DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
		
		lineTokenizer.setDelimiter(",");
		lineTokenizer.setStrict(false);
		lineTokenizer.setNames(new String[] {"id","name","department","salary"});
		
		BeanWrapperFieldSetMapper<User> beanWrapperFieldSetMapper = new BeanWrapperFieldSetMapper<>();
		beanWrapperFieldSetMapper.setTargetType(User.class);
		
		defaultLineMapper.setLineTokenizer(lineTokenizer);
		defaultLineMapper.setFieldSetMapper(beanWrapperFieldSetMapper);
		return defaultLineMapper;
	}

}
