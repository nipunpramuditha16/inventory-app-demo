package com.abcretail.inventory.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CalculatorTest {

    private Calculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new Calculator();
    }

    @Test
    void addReturnsSum() {
        // Lab Task 5: change 30 to 100 to make this test (and the pipeline) fail on purpose.
        assertEquals(30, calculator.add(40, 20));
    }

    @Test
    void addHandlesNegativeNumbers() {
        assertEquals(-5, calculator.add(-10, 5));
    }

    @Test
    void subtractReturnsDifference() {
        assertEquals(15, calculator.subtract(20, 5));
    }

    @Test
    void multiplyReturnsProduct() {
        assertEquals(200, calculator.multiply(10, 20));
    }

    @Test
    void divideReturnsQuotient() {
        assertEquals(5, calculator.divide(20, 4));
    }

    @Test
    void divideByZeroThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> calculator.divide(10, 0));
    }
}
