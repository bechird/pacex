package com.epac.cap.common;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

/**
 * Utility for working with numbers.
 * 
 */
public final class NumberUtil {
  private static final Logger logger = Logger.getLogger(NumberUtil.class);
  // For improved performance we are creating a currency formatter per thread
  // DecimalFormat is not thread safe on its own
  private static final ThreadLocal<DecimalFormat> CURRENCY_FORMAT = new ThreadLocal<DecimalFormat>() {
    @Override
    protected DecimalFormat initialValue() {
      return new DecimalFormat("$###,###");
    }
  };

  private static final ThreadLocal<DecimalFormat> CURRENCY_WITH_DECIMALS_FORMAT = new ThreadLocal<DecimalFormat>() {
    @Override
    protected DecimalFormat initialValue() {
      return new DecimalFormat("$###,###.00");
    }
  };

  private NumberUtil() {
    // make this utility class uninstantiable
  }

  /**
   * Returns a pseudorandom, uniformly distributed int value between 0 (inclusive) and the specified value (exclusive).
   * 
   * @param maxValue the maxValue int (exclusive) in the range of possible numbers
   * @return
   */
  public static Integer getRandomInteger(int maxValue) {
    return getRandomInteger(0, maxValue);
  }

  /**
   * Returns a pseudorandom, uniformly distributed int value between the minValue (inclusive) and the max value
   * (exclusive).
   * 
   * @param maxValue the min value int (inclusive) in the range of possible numbers
   * @param maxValue the max value int (exclusive) in the range of possible numbers
   * @return
   * @throws IllegalArgumentException if maxValue is not positive
   */
  public static Integer getRandomInteger(int minValue, int maxValue) {
    Random random = new Random();
    return minValue + random.nextInt(maxValue - minValue);
  }

  /**
   * Returns a pseudorandom, uniformly distributed double value between 0.0 (inclusive) and the specified value
   * (exclusive).
   * 
   * @param maxValue the maxValue double (exclusive) in the range of possible numbers
   * @return
   */
  public static Double getRandomDouble(double maxValue) {
    return getRandomDouble(0.0, maxValue);
  }

  /**
   * Returns a pseudorandom, uniformly distributed double value between the minValue (inclusive) and the max value
   * (exclusive).
   * 
   * @param maxValue the min value double (inclusive) in the range of possible numbers
   * @param maxValue the max value double (exclusive) in the range of possible numbers
   * @return
   * 
   */
  public static Double getRandomDouble(double minValue, double maxValue) {
    Random random = new Random();
    return minValue + (random.nextDouble() * (maxValue - minValue));
  }

  /**
   * Parses the given string into an integer value. If toParse is null then null is returned
   * 
   * @param toParse
   * @return
   * @throws ParseException
   */
  public static Integer parseCurrencyIntoInteger(String toParse) throws ParseException {
    if (toParse == null) {
      return null;
    }
    Double directCostYearThreeDouble = NumberUtil.parseCurrencyIntoDouble(toParse);
    Double d = Math.floor(directCostYearThreeDouble);
    return d.intValue();
  }

  /**
   * Parses the given string into a double value. If toParse is null then null is returned
   * 
   * @param toParse
   * @return
   * @throws ParseException
   */
  public static Double parseCurrencyIntoDouble(String toParse) throws ParseException {
    if (toParse == null) {
      return null;
    }
    // help out the user by adding a dollar sign if it does not exist
    if (!toParse.matches("^\\$.*")) {
      toParse = "$" + toParse;
    }
    return NumberFormat.getCurrencyInstance().parse(toParse).doubleValue();
  }

  /**
   * Returns the difference between the numerator and denomenator expressed as a percentage. If both strings cannot be
   * parsed as numbers then 'Error' is returned
   * 
   * @param numerator
   * @param denomenator
   * @return
   * @see #getFormattedPercent(Double)
   */
  public static String getStringPercentage(String numerator, String denomenator) {
    String result = null;
    try {
      Double d1 = parseCurrencyIntoDouble(numerator);
      Double d2 = parseCurrencyIntoDouble(denomenator);
      if (d1 != null && d2 != null) {
        Double d3 = (d1 / d2);
        result = getFormattedPercent(d3);
      } else {
        logger.warn("numerator and or denomenator was null so returning Error as result");
        result = "Error";
      }
    } catch (ParseException e) {
      result = "Error";
      logger.warn(e);
    }
    return result;
  }

  /**
   * @param numerator
   * @param denomenator
   * @param appliedTo
   * @return
   */
  public static String getStringPercentageApplied(String numerator, String denomenator, String appliedTo) {
    String result = null;

    if (numerator != null && denomenator != null && appliedTo != null) {
      try {
        // help out the user by adding a dollar sign if it does not exist
        if (!numerator.matches("^\\$.*")) {
          numerator = "$" + numerator;
        }
        if (!denomenator.matches("^\\$.*")) {
          denomenator = "$" + denomenator;
        }
        if (!appliedTo.matches("^\\$.*")) {
          appliedTo = "$" + appliedTo;
        }
        NumberFormat format = NumberFormat.getCurrencyInstance();
        Double d1 = format.parse(numerator).doubleValue();
        Double d2 = format.parse(denomenator).doubleValue();
        Double d3 = format.parse(appliedTo).doubleValue();
        Double d4 = (d1 / d2) * d3;
        // since i'm pretty sure only sps uses string percentage applied, do not allow decimals as requested by tracy
        result = getFormattedCurrency(d4, false);
      } catch (ParseException e) {
        result = "Error";
        logger.warn(e);
      }
    }
    return result;
  }


  /**
   * Returns the given double value formatted in currency format or null if the given value is null
   * 
   * @param d
   * @return
   */
  public static String getFormattedCurrency(Double d, boolean allowDecimals) {
    if (d == null) {
      return null;
    }
    if (allowDecimals) {
      return CURRENCY_WITH_DECIMALS_FORMAT.get().format(d);
    } else {
      return CURRENCY_FORMAT.get().format(Math.ceil(d));
    }
  }

  /**
   * Returns the given integer value formatted in currency format or null if the given value is null
   * 
   * @param d
   * @return
   */
  public static String getFormattedCurrency(Integer d, boolean allowDecimals) {
    if (d == null) {
      return null;
    }
    if (allowDecimals) {
      return CURRENCY_WITH_DECIMALS_FORMAT.get().format(d);
    } else {
      return CURRENCY_FORMAT.get().format(d);
    }
  }

  /**
   * Formats the given number as a percentage string. This translates to rounding the number and then adding % to the
   * end of the string. If the percentage is less than 1 but greater than 0 then '<1%' will be returned.
   * 
   * @param d the number to format as a percentage string
   * @return the formatted number or null if d is null
   */
  public static String getFormattedPercent(Double d) {
    if (d == null) {
      return null;
    }

    String percent;
    if (d > 0.00 && d < 0.01) {
      percent = "<1%";
    } else {
      percent = NumberFormat.getPercentInstance().format(d);
    }

    return percent;
  }

  public static Integer getIntegerPercentApplied(Integer numerator, Integer denomenator, Integer appliedTo) {
    Integer answer = null;
    if (numerator != null && denomenator != null && appliedTo != null) {
      Double d1 = numerator.doubleValue();
      Double d2 = denomenator.doubleValue();
      Double d3 = appliedTo.doubleValue();
      Double d4 = (d1 / d2) * d3;
      answer = d4.intValue();
    }
    return answer;
  }

  /**
   * Creates a list of numbers from the from param to the to param. Both are inclusive. If to is greater than from then
   * the reverse (a descending list) will be returned.
   * 
   * @param from the starting number
   * @param to the ending number
   * @return
   */
  public static List<Integer> getNumberList(int from, int to) {
    List<Integer> listOfNumbers = new ArrayList<Integer>();
    if (from > to) {
      for (int i = from; i >= to; i--) {
        listOfNumbers.add(i);
      }
    } else {
      for (int i = from; i <= to; i++) {
        listOfNumbers.add(i);
      }
    }
    return listOfNumbers;
  }
}
