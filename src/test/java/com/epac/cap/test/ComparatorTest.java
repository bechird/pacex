package com.epac.cap.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Base test for comparators. Adds a method for easy assertion of ordering based on a comparator.
 * 
 * @param <T> the object being compared / tested
 */
public class ComparatorTest<T> {
  public static <T> void assertElementsOrderedLikeThis(Comparator<T> comparator, T... elements) {
    List<T> expectedOrder = Arrays.asList(elements);
    List<T> shuffledAndSorted = new ArrayList<T>(expectedOrder);
    Collections.shuffle(shuffledAndSorted, new Random(0));
    Collections.sort(shuffledAndSorted, comparator);
    assertEquals(expectedOrder, shuffledAndSorted);

    List<T> reversedAndSorted = new ArrayList<T>(expectedOrder);
    Collections.reverse(reversedAndSorted);
    Collections.sort(reversedAndSorted, comparator);
    assertEquals(expectedOrder, reversedAndSorted);
  }
}
