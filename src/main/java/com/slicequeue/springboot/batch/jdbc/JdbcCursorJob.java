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
import org.springframework.batch.core.job.CompositeJobParametersValidator;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;

import javax.sql.DataSource;

@EnableBatchProcessing
@SpringBootApplication
public class JdbcCursorJob {

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
    public JdbcCursorItemReader<Customer> customerJdbcCursorItemReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<Customer>()
                .name("customerItemReader")
                .dataSource(dataSource)                         // 1. 실행할 데이터 소스
                .sql("select * from customer where city = ?")   // 2. 실행할 쿼리
                .rowMapper(new CustomerRowMapper())             // 3. 사용할 RowMapper 구현체
                .preparedStatementSetter(citySetter(null))      //
                .build();
    }

    @Bean
    @StepScope
    public ArgumentPreparedStatementSetter citySetter(
            @Value("#{jobParameters['city']}") String city) {
        return new ArgumentPreparedStatementSetter(new Object[] {city});
    }

    @Bean
    public ItemWriter<Customer> itemWriter() {
        return (items) -> items.forEach(System.out::println);
    }

    @Bean
    public Step copyFileStep() {
        return this.stepBuilderFactory.get("copyFileStep")
                .<Customer, Customer>chunk(10)
                .reader(customerJdbcCursorItemReader(null))
                .writer(itemWriter())
                .build();
    }

    @Bean
    public Job job() {
        return this.jobBuilderFactory.get("job")
                .validator(validator())
                .incrementer(new RunIdIncrementer())
                .start(copyFileStep())
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(JdbcCursorJob.class, "city=Chicago");
    }

}
