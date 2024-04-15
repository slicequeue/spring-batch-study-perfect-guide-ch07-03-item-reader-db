package com.slicequeue.springboot.batch.jdbc;

import com.slicequeue.springboot.batch.jdbc.domain.Customer;
import com.slicequeue.springboot.batch.jdbc.mapper.CustomerRowMapper;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@EnableBatchProcessing
@SpringBootApplication
public class JdbcPagingJob {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public JobParametersValidator validator() {
        return new DefaultJobParametersValidator(
                new String[] {"city"},
                new String[] {"run.id"}
        );
    }

    @Bean
    @StepScope
    public JdbcPagingItemReader<Customer> customerJdbcPagingItemReader(
            DataSource dataSource,
            PagingQueryProvider queryProvider,
            @Value("#{jobParameters['city']}") String city
    ) {

        Map<String, Object> parameterValues = new HashMap<>(1);
        parameterValues.put("city", city);

        return new JdbcPagingItemReaderBuilder<Customer>()
                .name("customerJdbcPagingItemReader")
                .dataSource(dataSource)             // 1. 데이터 소스
                .queryProvider(queryProvider)       // 2. PagingQueryProvider 구현체 - pagingQueryProvider 아래 구현한 Bean
                .parameterValues(parameterValues)   // 5. 파라미터
                .pageSize(10)                       // 4. 페이지 크기
                .rowMapper(new CustomerRowMapper()) // 3. RowMapper 구현체
                .build();
    }

    @Bean
    public SqlPagingQueryProviderFactoryBean pagingQueryProvider(DataSource dataSource) {
        SqlPagingQueryProviderFactoryBean factoryBean = new SqlPagingQueryProviderFactoryBean();

        factoryBean.setDataSource(dataSource);              // 데이터 소스
        factoryBean.setSelectClause("select *");            // select 절
        factoryBean.setFromClause("from Customer");         // from 절
        factoryBean.setWhereClause("where city = :city");   // where 절
        factoryBean.setSortKey("lastName");                 // 정렬 키값

        return factoryBean;
    }

    @Bean
	public ItemWriter<Customer> itemWriter() {
		return (items) -> items.forEach(System.out::println);
	}

    @Bean
    public Step copyFileStep() {
        return this.stepBuilderFactory.get("copyFileStep")
                .<Customer, Customer>chunk(10)
                .reader(customerJdbcPagingItemReader(null, null, null))
                .writer(itemWriter())
                .build();
    }

    @Bean
    public Job job() {
        return this.jobBuilderFactory.get("job-jdbc-paging")
                .validator(validator())
                .incrementer(new RunIdIncrementer())
                .start(copyFileStep())
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(JdbcPagingJob.class, "city=Bellevue");
    }

}
