package com.epac.cap.common;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.Logger;

/***
 * A utility class for working with dates. It has only static methods and doesn't have state so it shouldn't be
 * instantiated.
 * 
 */
public final class DateUtil {
  private static final Logger logger = Logger.getLogger(DateUtil.class);
  private static final Random random = new Random();
  private static final String RFC3339_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
  private static final String RFC3339_PATTERN = "{0,date," + RFC3339_FORMAT + "}";
  private static final FastDateFormat standardDateFormat = FastDateFormat.getInstance("MM/dd/yyyy");

  /**
   * 
   * {@code DateUtil} instances should NOT be constructed in standard programming. Instead, the class should be used
   * statically.
   * 
   */
  private DateUtil() {}

  /**
   * Returns a string version of the given date in MM/dd/yyyy format.
   * 
   * @param date the date to convert to a string
   * @return the date in MM/dd/yyyy format or an empty string if the date is null
   */
  public static String toStandardDateFormat(Date date) {
    return (date != null) ? standardDateFormat.format(date) : "";
  }

  /**
   * Turn object into a formatted date string. If object is a string will try to parse it as a DateFormat.SHORT or will
   * create a SimpleDateFormat out of the customFormat string.
   * 
   * @param obj
   * @param customFormat
   * @return
   */
  public static String format(Object obj, String customFormat) {
    return format(obj, customFormat, RFC3339_PATTERN);
  }

  public static String format(Object obj, String customFormat, String outputFormat) {
    if (obj == null) {
      return null;
    }
    if ("".equals(obj)) {
      return "";
    }

    Date dateToFormat = null;
    String dateStr = obj.toString();
    if (obj instanceof java.sql.Date) {
      dateToFormat = new java.util.Date(((java.sql.Date) obj).getTime());
    } else if (obj instanceof Date) {
      dateToFormat = (Date) obj;
    } else if (obj instanceof Calendar) {
      dateToFormat = ((Calendar) obj).getTime();
    } else {
      // try to parse a date-
      if (dateStr.equalsIgnoreCase("today")) {
        dateToFormat = new Date();
      } else {
        if (customFormat != null) {
          // if the user provided a custom format, use that to parse the string provided into
          // a date object
          try {
            dateToFormat = new SimpleDateFormat(customFormat).parse(dateStr);
          } catch (ParseException e) {
            logger.error("Could not parse date " + dateStr + " using custom format " + customFormat + ".");
          }
        } else {
          try {
            // otherwise, use our default RFC3339 format. this is what the moodatetimepicker uses
            // as it's inputoutputformat and the final format returned by this method, so the date
            // 'should' be in 3339 format (or custom if provided)
            try {
              dateToFormat = parseRFC3339(dateStr);
            } catch (ParseException e1) {
              // no format has been provided and the object is not a recognized date
              // class, try to parse it as a Date.SHORT, if that doesn't work, nothing
              // we can do so just return the date string. Date.SHORT is common because
              // OGNL similar to %{dateObject} returns a string in DateFormat.SHORT format
              dateToFormat = DateFormat.getDateInstance(DateFormat.SHORT).parse(dateStr);
            }
          } catch (ParseException e) {
            logger.error("Could not parse date " + dateStr + " using RFC3339, or SHORT formats.");
          }
        }
      }
    }

    String formattedDate = null;
    if (dateToFormat != null) {
      formattedDate = MessageFormat.format(outputFormat, dateToFormat);
    } else {
      formattedDate = dateStr;
    }
    return formattedDate;
  }

  public static Date parseRFC3339(String dateStr) throws ParseException {
    return new SimpleDateFormat(RFC3339_FORMAT).parse(dateStr);
  }

  /**
   * Returns a random date between the current date and the same date of the given minYear. If minYear is less than or
   * equal to zero or if minYear is greater than the current year then the previous year will be used as the minimum
   * year. If minYear equals the current year then the lower bound of the random date range will be yesterday.
   * 
   * @param minYear the minimum year the random date should be from
   * @return a random date between the current date and the same date of the given minYear
   */
  public static Date getRandomPastDate(int minYear) {
    Calendar cdr = Calendar.getInstance();
    int currYear = cdr.get(Calendar.YEAR);
    // if year is a bad value or greater than the current year than use last
    // year
    if (minYear <= 0 || minYear > currYear) {
      minYear = currYear - 1;
    }
    cdr.set(Calendar.YEAR, minYear);
    if (minYear == currYear) {
      // go back a day if the same year
      cdr.add(Calendar.DAY_OF_MONTH, -1);
    }
    return getRandomPastDate(cdr.getTime());
  }

  /**
   * Returns a random date between the min date (inclusive) given and the current date (exclusive).
   * 
   * @param minDate the minimum date the random date can be (inclusive)
   * @return a random date between the min date (inclusive) given and the current date (exclusive).
   * @see #getRandomDate(Date, Date)
   */
  public static Date getRandomPastDate(Date minDate) {
    Date maxDate = new Date();
    if (minDate != null && minDate.after(maxDate)) {
      Calendar cdr = Calendar.getInstance();
      cdr.setTime(maxDate);
      // go back a month
      cdr.add(Calendar.MONTH, -1);
      minDate = cdr.getTime();
    }
    return getRandomDate(minDate, maxDate);
  }

  /**
   * Returns a random date between the current date and the same date of the given maxYear. If maxYear is less than or
   * equal to zero or if maxYear is less than the current year then the previous year will be used as the maximum year.
   * If maxYear equals the current year then the upper bound of the random date range will be a month from now.
   * 
   * @param maxYear the maximum year the random date should be from
   * @return a random date between the current date and the same date of the given minYear
   */
  public static Date getRandomFutureDate(int maxYear) {
    Calendar cdr = Calendar.getInstance();
    int currYear = cdr.get(Calendar.YEAR);
    // if year is a bad value or less than the current year than use last year
    if (maxYear <= 0 || maxYear < currYear) {
      maxYear = currYear + 1;
    }
    cdr.set(Calendar.YEAR, maxYear);
    if (maxYear == currYear) {
      // go forward a month if the same year
      cdr.add(Calendar.MONTH, 1);
    }
    return getRandomFutureDate(cdr.getTime());
  }

  /**
   * Returns a random date between the current date (inclusive) and the max date (exclusive) given.
   * 
   * @param maxDate the maximum date the random date can be (exclusive)
   * @return a random date between the current date (inclusive) and the max date (exclusive) given.
   * @see #getRandomDate(Date, Date)
   */
  public static Date getRandomFutureDate(Date maxDate) {
    Date minDate = new Date();
    if (maxDate != null && maxDate.before(minDate)) {
      Calendar cdr = Calendar.getInstance();
      cdr.setTime(minDate);
      // go forward a month
      cdr.add(Calendar.MONTH, 1);
      maxDate = cdr.getTime();
    }
    return getRandomDate(minDate, maxDate);
  }

  /**
   * Returns a random date between the min date (inclusive) and the max date (exclusive).
   * 
   * <p>
   * If the min date is null then the standard base time known as "the epoch", namely January 1, 1970, 00:00:00 GMT will
   * be used as the date time of the minDate. Likewise if the max date is null then the current date + 50 years is used
   * 
   * 
   * @param minDate the minimum date the random date can be (inclusive)
   * @param maxDate the maximum date the random date can be (exclusive)
   * @return a random date between the min date (inclusive) and the max date (exclusive)
   * @see #getMaxDate()
   */
  public static Date getRandomDate(Date minDate, Date maxDate) {
    if (minDate == null) {
      minDate = new Date(0);
    }
    if (maxDate == null) {
      maxDate = addYearsToDate(null, 50);
    }
    if (minDate.after(maxDate)) {
      // min and max are switched, switch them
      Date tmpMinDate = new Date(maxDate.getTime());
      maxDate = new Date(minDate.getTime());
      minDate = tmpMinDate;
    }
    long difference = maxDate.getTime() - minDate.getTime();
    difference = difference == 0 ? 100 : difference;
    double nextDouble = random.nextDouble();
    long randomTime = (long) ((nextDouble * difference) + minDate.getTime());
    return new Date(randomTime);
  }

  /**
   * @return the a date yearsFromDate years from it
   * @deprecated use addYearsToDate instead
   * @see #addYearsToDate(Date, int)
   */
  @Deprecated
  public static Date getDateXYearsFromDate(Date startDate, int yearsFromDate) {
    return addYearsToDate(startDate, yearsFromDate);
  }

  /**
   * Adds the given number of years to the startDate.
   * 
   * @param aDate the start date. if null then the current date is used.
   * @param numYears the number of years after the given date to get a date for
   * @return a date x number of years from the given date
   */
  public static Date addYearsToDate(Date startDate, int numYears) {
    Calendar cal = Calendar.getInstance();
    if (startDate == null) {
      cal.setTime(new Date());
    } else {
      cal.setTime(startDate);
    }
    cal.add(Calendar.YEAR, numYears);

    return cal.getTime();
  }

  /**
   * Adds the given number of days to the startDate.
   * 
   * @param aDate the start date. if null then the current date is used.
   * @param numDays the number of days after the given date to get a date for
   * @return a date x number of hours from the given date
   */
  public static Date addWeeksToDate(Date startDate, int numWeeks) {
    Calendar cal = Calendar.getInstance();
    if (startDate == null) {
      cal.setTime(new Date());
    } else {
      cal.setTime(startDate);
    }
    cal.add(Calendar.WEEK_OF_MONTH, numWeeks);

    return cal.getTime();
  }

  /**
   * Adds the given number of days to the startDate.
   * 
   * @param aDate the start date. if null then the current date is used.
   * @param numDays the number of days after the given date to get a date for
   * @return a date x number of hours from the given date
   */
  public static Date addDaysToDate(Date startDate, int numDays) {
    Calendar cal = Calendar.getInstance();
    if (startDate == null) {
      cal.setTime(new Date());
    } else {
      cal.setTime(startDate);
    }
    cal.add(Calendar.DAY_OF_MONTH, numDays);

    return cal.getTime();
  }

  /**
   * Adds the given number of hours to the startDate.
   * 
   * @param aDate the start date. if null then the current date is used.
   * @param numHours the number of hours after the given date to get a date for
   * @return a date x number of hours from the given date
   */
  public static Date addHoursToDate(Date startDate, int numHours) {
    Calendar cal = Calendar.getInstance();
    if (startDate == null) {
      cal.setTime(new Date());
    } else {
      cal.setTime(startDate);
    }
    cal.add(Calendar.HOUR, numHours);

    return cal.getTime();
  }

  /**
   * @return the maximum date for the current calendar
   */
  public static Date getMaxDate() {
    Calendar cal = Calendar.getInstance();
    cal.clear();
    cal.set(cal.getActualMaximum(Calendar.YEAR), cal.getActualMaximum(Calendar.MONTH),
            cal.getActualMaximum(Calendar.DAY_OF_MONTH), cal.getActualMaximum(Calendar.HOUR),
            cal.getActualMaximum(Calendar.MINUTE), cal.getActualMaximum(Calendar.SECOND));

    cal.set(Calendar.MILLISECOND, cal.getActualMaximum(Calendar.MILLISECOND));

    return cal.getTime();
  }

  /**
   * Transform a date (represented as a long) to a XMLGregorianCalendar
   * 
   * @param date the date to transform in milliseconds
   * 
   * @return the date transformed into an XMLGregorianCalender
   */
  public static XMLGregorianCalendar long2Gregorian(long date) {
    DatatypeFactory dataTypeFactory;
    try {
      dataTypeFactory = DatatypeFactory.newInstance();
    } catch (DatatypeConfigurationException e) {
      throw new RuntimeException(e);
    }
    GregorianCalendar gc = new GregorianCalendar();
    gc.setTimeInMillis(date);
    return dataTypeFactory.newXMLGregorianCalendar(gc);
  }

  /**
   * Transform a date in Date to XMLGregorianCalendar
   * 
   * @param date the date to transform in milliseconds
   * 
   * @return the date transformed into an XMLGregorianCalender
   */
  public static XMLGregorianCalendar date2Gregorian(Date date) {
    return long2Gregorian(date.getTime());
  }

  /**
   * Determines if the given date is between the from and to date both inclusive and ignoring the time of day on the
   * from and to dates.
   * 
   * @param date the date to compare
   * @param fromDate the min date inclusive
   * @param toDate the max date inclusive
   * @return
   */
  public static boolean isBetweenDays(Date date, Date fromDate, Date toDate) {
    if (date == null) {
      throw new IllegalArgumentException("Date cannot be null");
    }
    if (fromDate == null || toDate == null) {
      return false;
    }

    boolean isBetween = false;
    isBetween = date.compareTo(getStart(fromDate)) >= 0 && date.compareTo(getEnd(toDate)) <= 0;
    return isBetween;
  }

  /**
   * <p>
   * Checks if two dates are on the same day ignoring time.
   * </p>
   * 
   * @param date1 the first date, not altered, not null
   * @param date2 the second date, not altered, not null
   * @return true if they represent the same day
   * @throws IllegalArgumentException if either date is null
   */
  public static boolean isSameDay(Date date1, Date date2) {
    if (date1 == null || date2 == null) {
      throw new IllegalArgumentException("The dates must not be null");
    }
    Calendar cal1 = Calendar.getInstance();
    cal1.setTime(date1);
    Calendar cal2 = Calendar.getInstance();
    cal2.setTime(date2);
    return isSameDay(cal1, cal2);
  }

  /**
   * Checks if two calendars represent the same day ignoring time.
   * 
   * @param cal1 the first calendar, not altered, not null
   * @param cal2 the second calendar, not altered, not null
   * @return true if they represent the same day
   * @throws IllegalArgumentException if either calendar is null
   */
  public static boolean isSameDay(Calendar cal1, Calendar cal2) {
    if (cal1 == null || cal2 == null) {
      throw new IllegalArgumentException("The dates must not be null");
    }
    return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1
            .get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
  }

  /**
   * Checks if a date is today.
   * 
   * @param date the date, not altered, not null.
   * @return true if the date is today.
   * @throws IllegalArgumentException if the date is null
   */
  public static boolean isToday(Date date) {
    return isSameDay(date, Calendar.getInstance().getTime());
  }

  /**
   * Checks if a calendar date is today.
   * 
   * @param cal the calendar, not altered, not null
   * @return true if cal date is today
   * @throws IllegalArgumentException if the calendar is null
   */
  public static boolean isToday(Calendar cal) {
    return isSameDay(cal, Calendar.getInstance());
  }

  /**
   * Checks if the first date is before the second date ignoring time.
   * 
   * @param date1 the first date, not altered, not null
   * @param date2 the second date, not altered, not null
   * @return true if the first date day is before the second date day.
   * @throws IllegalArgumentException if the date is null
   */
  public static boolean isBeforeDay(Date date1, Date date2) {
    if (date1 == null || date2 == null) {
      throw new IllegalArgumentException("The dates must not be null");
    }
    Calendar cal1 = Calendar.getInstance();
    cal1.setTime(date1);
    Calendar cal2 = Calendar.getInstance();
    cal2.setTime(date2);
    return isBeforeDay(cal1, cal2);
  }

  /**
   * Checks if the first calendar date is before the second calendar date ignoring time.
   * 
   * @param cal1 the first calendar, not altered, not null.
   * @param cal2 the second calendar, not altered, not null.
   * @return true if cal1 date is before cal2 date ignoring time.
   * @throws IllegalArgumentException if either of the calendars are null
   */
  public static boolean isBeforeDay(Calendar cal1, Calendar cal2) {
    if (cal1 == null || cal2 == null) {
      throw new IllegalArgumentException("The dates must not be null");
    }
    if (cal1.get(Calendar.ERA) < cal2.get(Calendar.ERA)) {
      return true;
    }
    if (cal1.get(Calendar.ERA) > cal2.get(Calendar.ERA)) {
      return false;
    }
    if (cal1.get(Calendar.YEAR) < cal2.get(Calendar.YEAR)) {
      return true;
    }
    if (cal1.get(Calendar.YEAR) > cal2.get(Calendar.YEAR)) {
      return false;
    }
    return cal1.get(Calendar.DAY_OF_YEAR) < cal2.get(Calendar.DAY_OF_YEAR);
  }

  /**
   * Checks if the first date is after the second date ignoring time.
   * 
   * @param date1 the first date, not altered, not null
   * @param date2 the second date, not altered, not null
   * @return true if the first date day is after the second date day.
   * @throws IllegalArgumentException if the date is null
   */
  public static boolean isAfterDay(Date date1, Date date2) {
    if (date1 == null || date2 == null) {
      throw new IllegalArgumentException("The dates must not be null");
    }
    Calendar cal1 = Calendar.getInstance();
    cal1.setTime(date1);
    Calendar cal2 = Calendar.getInstance();
    cal2.setTime(date2);
    return isAfterDay(cal1, cal2);
  }

  /**
   * <p>
   * Checks if the first calendar date is after the second calendar date ignoring time.
   * </p>
   * 
   * @param cal1 the first calendar, not altered, not null.
   * @param cal2 the second calendar, not altered, not null.
   * @return true if cal1 date is after cal2 date ignoring time.
   * @throws IllegalArgumentException if either of the calendars are null
   */
  public static boolean isAfterDay(Calendar cal1, Calendar cal2) {
    if (cal1 == null || cal2 == null) {
      throw new IllegalArgumentException("The dates must not be null");
    }
    if (cal1.get(Calendar.ERA) < cal2.get(Calendar.ERA)) {
      return false;
    }
    if (cal1.get(Calendar.ERA) > cal2.get(Calendar.ERA)) {
      return true;
    }
    if (cal1.get(Calendar.YEAR) < cal2.get(Calendar.YEAR)) {
      return false;
    }
    if (cal1.get(Calendar.YEAR) > cal2.get(Calendar.YEAR)) {
      return true;
    }
    return cal1.get(Calendar.DAY_OF_YEAR) > cal2.get(Calendar.DAY_OF_YEAR);
  }

  /** Returns the given date with the time set to the start of the day. */
  public static Date getStart(Date date) {
    return clearTime(date);
  }

  /** Returns the given date with the time values cleared. */
  public static Date clearTime(Date date) {
    if (date == null) {
      return null;
    }
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    clearTime(c);
    return c.getTime();
  }

  /**
   * Clears all time fields from the given calendar.
   * 
   * @param cal the calender to clear time for.
   */
  public static void clearTime(Calendar cal) {
    if (cal == null) {
      return;
    }

    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.HOUR, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    cal.set(Calendar.AM_PM, Calendar.AM);
  }

  /**
   * Resets the given calendars current day of the month to 1
   * 
   * @param c the calendar
   */
  public static void clearDayOfMonth(Calendar c) {
    if (c == null) {
      return;
    }
    c.set(Calendar.DAY_OF_MONTH, 1);
  }

  /**
   * Determines whether or not a date has any time values.
   * 
   * @param date The date.
   * @return true iff the date is not null and any of the date's hour, minute, seconds or millisecond values are greater
   *         than zero.
   */
  public static boolean hasTime(Date date) {
    if (date == null) {
      return false;
    }
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    if (c.get(Calendar.HOUR_OF_DAY) > 0) {
      return true;
    }
    if (c.get(Calendar.MINUTE) > 0) {
      return true;
    }
    if (c.get(Calendar.SECOND) > 0) {
      return true;
    }
    if (c.get(Calendar.MILLISECOND) > 0) {
      return true;
    }
    return false;
  }

  /** Returns the given date with time set to the end of the day (23:59:59.999) */
  public static Date getEnd(Date date) {
    if (date == null) {
      return null;
    }
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    c.set(Calendar.HOUR_OF_DAY, 23);
    c.set(Calendar.MINUTE, 59);
    c.set(Calendar.SECOND, 59);
    c.set(Calendar.MILLISECOND, 999);
    return c.getTime();
  }


  /**
   * Determines how many week days are between the two dates.
   * 
   * <p>
   * For a more reliable implementation of finding how many work days are between 2 dates see DayService.
   * 
   * @param startDt
   * @param endDt
   * @return
   */
  public static int getWorkDays(Date startDt, Date endDt) {
    Calendar startCal, endCal;
    startCal = Calendar.getInstance();
    startCal.setTime(startDt);
    endCal = Calendar.getInstance();
    endCal.setTime(endDt);
    int workDays = 0;

    // Return 0 if start and end are the same
    if (startCal.getTimeInMillis() == endCal.getTimeInMillis()) {
      return 0;
    }
    // Just in case the dates were transposed this prevents infinite loop
    if (startCal.getTimeInMillis() > endCal.getTimeInMillis()) {
      startCal.setTime(endDt);
      endCal.setTime(startDt);
    }

    do {
      startCal.add(Calendar.DAY_OF_MONTH, 1);
      if (startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY
              && startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
        ++workDays;
      }
    } while (startCal.getTimeInMillis() < endCal.getTimeInMillis());

    return workDays;
  }

  /**
   * Returns the number of days between the two given dates. If the dates refer to the same day then 0 is returned.
   * 
   * @param startDate
   * @param endDate
   * @return
   * @throws IllegalArgumentException if either startDate or endDate are null
   */
  public static int getDaysBetween(Date startDate, Date endDate) {
    if (startDate == null || endDate == null) {
      throw new IllegalArgumentException("Start Date and End Date must be provided.");
    }

    Calendar startCal = Calendar.getInstance();
    Calendar endCal = Calendar.getInstance();
    // clear time so that after and before tests below work as expected
    startDate = clearTime(startDate);
    endDate = clearTime(endDate);

    if (startDate.after(endDate)) {
      // looks like the dates were transposed, switch the comparison in this method to avoid infinite loop
      startCal.setTime(endDate);
      endCal.setTime(startDate);
    } else {
      startCal.setTime(startDate);
      endCal.setTime(endDate);
    }

    int daysBetween = 0;
    while (startCal.before(endCal)) {
      startCal.add(Calendar.DAY_OF_MONTH, 1);
      daysBetween++;
    }
    return daysBetween;
  }
}
