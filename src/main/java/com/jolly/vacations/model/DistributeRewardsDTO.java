package com.jolly.vacations.model;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Data;

@Data
public class DistributeRewardsDTO {

    private Double totalAmount;
    private Double perCustomer;

    @JsonAlias({"customerEmails", "customers"})
    private List<String> customerMobiles;

    private List<String> customerEmails;
    private List<JollyCustomerDTO> customers;

    public List<String> getCustomerMobiles() {
        if (customerMobiles != null && !customerMobiles.isEmpty()) {
            return customerMobiles;
        }
        if (customerEmails != null && !customerEmails.isEmpty()) {
            return customerEmails;
        }
        if (customers != null && !customers.isEmpty()) {
            return customers.stream()
                    .map(JollyCustomerDTO::getMobile)
                    .filter(Objects::nonNull)
                    .filter(mobile -> !mobile.trim().isEmpty())
                    .distinct()
                    .collect(Collectors.toList());
        }
        return customerMobiles;
    }
}
