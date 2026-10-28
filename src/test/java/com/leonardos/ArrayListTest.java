package com.leonardos;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import tinyjdk.ArrayList;

public class ArrayListTest {
    @Test
    void findExistingElements() {
        ArrayList<String> list = new ArrayList<>();
        list.add("A");
        list.add("B");
        list.add("C");

        assertTrue(list.contains("B"));
    }

}
