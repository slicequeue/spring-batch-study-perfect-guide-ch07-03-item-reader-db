package com.slicequeue.springboot.batch.jdbc.mapper;

import com.slicequeue.springboot.batch.jdbc.domain.Customer;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerRowMapper implements RowMapper<Customer> {

    @Override
    public Customer mapRow(ResultSet rs, int rowNum) throws SQLException {
        Customer customer = new Customer();

        customer.setId(rs.getLong("id"));
        customer.setAddress(rs.getString("address"));
        customer.setCity(rs.getString("city"));
        customer.setFirstName(rs.getString("firstName"));
        customer.setMiddleInitial(rs.getString("middleInitial"));
        customer.setLastName(rs.getString("lastName"));
        customer.setState(rs.getString("state"));
        customer.setZipCode(rs.getString("zipCode"));

        return customer;
    }

}
