package com.epac.cap.handler;

import java.util.Date;

/**
 * A helper class for comparators to extend from to simplify comparator implementations mainly by removing duplicate null
 * object conditionals.
 * 
 * @author smithjac
 */
public abstract class AbstractComparator {
  /**
   * Compares two objects to see if either are null and returns an Integer object to allow for a null result.
   * <p>
   * If both objects are the same reference or both are null then 0 is returned. If {@code obj1} is null and
   * {@code obj2} isn't then 1 is returned and -1 is returned for the opposite case. If neither objects are null then
   * null is returned.
   * 
   * @param obj1 the first object to compare
   * @param obj2 the second object to compare
   * @return an Integer indicating if any of the objects are null or not.
   */
  protected Integer nullCompare(Object obj1, Object obj2) {
    if (obj1 == obj2) {
      //both are null or both are the same reference
      return 0;
    }
    //ok they both aren't null, see if either is null
    if (obj1 == null) {
      //obj1 is null so obj2 can't be null based on earlier == check
      return 1;
    } else if (obj2 == null) {
      //obj2 is null so obj1 can't be null based on earlier == check
      return -1;
    } else {
      return null;
    }
  }
  
  /**
   * Compares two objects to see if either are null and returns an Integer object to allow for a null result.
   * <p>
   * If both objects are the same reference or both are null then 1 is returned. If {@code obj1} is null and
   * {@code obj2} isn't then 1 is returned and -1 is returned for the opposite case. If neither objects are null then
   * null is returned.
   * 
   * @param obj1 the first object to compare
   * @param obj2 the second object to compare
   * @return an Integer indicating if any of the objects are null or not.
   */
  protected Integer nullCompareForSortedSet(Object obj1, Object obj2) {
    if (obj1 == obj2) {
      //both are null or both are the same reference
      return 1;
    }
    //ok they both aren't null, see if either is null
    if (obj1 == null) {
      //obj1 is null so obj2 can't be null based on earlier == check
      return 1;
    } else if (obj2 == null) {
      //obj2 is null so obj1 can't be null based on earlier == check
      return -1;
    } else {
      return null;
    }
  }

  /**
   * Compares 2 integers in ascending order with nulls last.
   * 
   * @param obj1 the first object to compare
   * @param obj2 the second object to compare
   * @return -1,0, or 1 depending on how {@code obj1} compares with {@code obj2}
   * @see #nullCompare(Object, Object)
   * @see Integer#compareTo(Integer)
   */
  protected int compare(Integer obj1, Integer obj2) {
    Integer nullCompare = nullCompare(obj1, obj2);
    if (nullCompare != null) {
      return nullCompare;
    } else {
      return obj1.compareTo(obj2);
    }
  }
  
  /**
   * Compares 2 integers in ascending order with nulls last.
   * Avoids returning 0 b/c sets won't hold the element in that case as it considers it a duplicate
   * @param obj1 the first object to compare
   * @param obj2 the second object to compare
   * @return -1, or 1 depending on how {@code obj1} compares with {@code obj2}
   * @see #nullCompare(Object, Object)
   * @see Integer#compareTo(Integer)
   */
  protected int compareForSortedSet(Integer obj1, Integer obj2) {
    Integer nullCompare = nullCompareForSortedSet(obj1, obj2);
    if (nullCompare != null) {
      return nullCompare;
    } else {
      return obj1 < obj2 ? -1: 1;
    }
  }

  /**
   * Compares 2 strings in ascending order (case sensitive) with nulls last.
   * 
   * @param obj1 the first object to compare
   * @param obj2 the second object to compare
   * @return a negative integer,0, or a positive integer depending on how {@code obj1} compares with {@code obj2}
   * @see #nullCompare(Object, Object)
   * @see String#compareTo(String)
   */
  protected int compare(String obj1, String obj2) {
    return this.compare(obj1, obj2, false);
  }

  /**
   * Compares 2 strings in ascending order with nulls last.
   * 
   * @param obj1 the first object to compare
   * @param obj2 the second object to compare
   * @param ignoreCase whether or not to do a case insensitive compare
   * @return a negative integer,0, or a positive integer depending on how {@code obj1} compares with {@code obj2}
   * @see #nullCompare(Object, Object)
   * @see String#compareTo(String)
   */
  protected int compare(String obj1, String obj2, boolean ignoreCase) {
    Integer nullCompare = nullCompare(obj1, obj2);
    if (nullCompare != null) {
      return nullCompare;
    } else {
      if (ignoreCase) {
        return obj1.compareToIgnoreCase(obj2);
      } else {
        return obj1.compareTo(obj2);
      }
    }
  }

  /**
   * Compares 2 booleans in ascending order with nulls last.
   * 
   * @param obj1 the first object to compare
   * @param obj2 the second object to compare
   * @return -1,0, or 1 depending on how {@code obj1} compares with {@code obj2}
   * @see #nullCompare(Object, Object)
   * @see Boolean#compareTo(Boolean)
   */
  protected int compare(Boolean obj1, Boolean obj2) {
    Integer nullCompare = nullCompare(obj1, obj2);
    if (nullCompare != null) {
      return nullCompare;
    } else {
      return obj1.compareTo(obj2);
    }
  }

  /**
   * Compares 2 dates in ascending order with nulls last.
   * 
   * @param obj1 the first object to compare
   * @param obj2 the second object to compare
   * @return -1,0, or 1 depending on how {@code obj1} compares with {@code obj2}
   * @see #nullCompare(Object, Object)
   * @see Date#compareTo(Date)
   */
  protected int compare(Date obj1, Date obj2) {
    Integer nullCompare = nullCompare(obj1, obj2);
    if (nullCompare != null) {
      return nullCompare;
    } else {
      return obj1.compareTo(obj2);
    }
  }
  
  /**
   * Compares 2 dates in ascending order with nulls last.
   * Avoids returning 0 b/c sets won't hold the element in that case as it considers it a duplicate
   * @param obj1 the first object to compare
   * @param obj2 the second object to compare
   * @return -1,0, or 1 depending on how {@code obj1} compares with {@code obj2}
   * @see #nullCompare(Object, Object)
   * @see Date#compareTo(Date)
   */
  protected int compareForSortedSet(Date obj1, Date obj2) {
    Integer nullCompare = nullCompareForSortedSet(obj1, obj2);
    if (nullCompare != null) {
      return nullCompare;
    } else {
      return (obj1.getTime()<obj2.getTime() ? -1 : 1);
    }
  }
}
