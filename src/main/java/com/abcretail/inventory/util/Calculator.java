package com.abcretail.inventory.util;

import org.springframework.stereotype.Component;

/**
 * Small arithmetic helper used by the inventory service.
 * It is intentionally simple so that students can easily read and modify its unit tests
 * (see Task 5 of the lab sheet: change an expected value to make the pipeline fail).
 */
@Component
public class Calculator {

    public int add(int a, int b) {
        return a + b;
    }

    public int subtract(int a, int b) {
        return a - b;
    }

    public int multiply(int a, int b) {
        return a * b;
    }

    public int divide(int a, int b) {
        if (b == 0) {
            throw new IllegalArgumentException("Cannot divide by zero");
        }
        return a / b;
    }
}
