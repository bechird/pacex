package com.epac.cap.common;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * A utility for working with Strings
 * 
 */
public final class StringUtil {
  public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
  public static final Charset UTF_8 = Charset.forName("UTF-8");

  private static Logger logger = Logger.getLogger(StringUtil.class);
  private static final Pattern HTML_PATTERN = Pattern.compile(".*\\<[^>]+>.*", Pattern.DOTALL);
  
  private StringUtil() {
    // make this utility class uninstantiable
  }
  
  /**
   * Encodes the given String into a sequence of bytes using the UTF-8 charset.
   * 
   * @param string the string to get the bytes from
   * @return UTF-8 encoded bytes of the given string or null if the given string is null
   */
  public static byte[] getBytesUtf8(final String string) {
    if (string == null) {
      return null;
    }
    return string.getBytes(UTF_8);
  }

  /**
   * Encodes the given String into a sequence of bytes using the ISO-8859-1 charset.
   * 
   * @param string the string to get the bytes from
   * @return ISO-8859-1 encoded bytes of the given string or null if the given string is null
   */
  public static byte[] getBytesIso8859(final String string) {
    if (string == null) {
      return null;
    }
    return string.getBytes(ISO_8859_1);
  }
  
  /**
   * Creates a string by decoding the given bytes using the UTF-8 Charset.
   * 
   * @param bytes the bytes to decode
   * @return a string decoded using the given bytes and the UTF-8 charset or null if the bytes are null
   */
  public static String bytesToUtf8(final byte[] bytes){
    return bytes == null ? null : new String(bytes, UTF_8);    
  }
  
  /**
   * Determines if the given string contains any html tags.
   * <p>
   * This is a quick method based upon a regular expression checking for a start tag &lt; followed by anything not an
   * end tag &gt; then followed by and end tag and optionally anything after that. This handles new lines in the string
   * as well.
   * 
   * @param str the string to check for html tags
   * @return true if the str contains html tags
   */
  public static boolean containsHtml(String str) {
    if (str == null) {
      return false;
    }
    return HTML_PATTERN.matcher(str).matches();
  }

  /**
   * Returns a string of the given length filled with random letters
   * 
   * @param length the length of string to return, if < 1 than 1 is used
   * @return
   */
  public static String getRandomString(int length) {
    StringBuilder builder = new StringBuilder();
    if (length < 1) {
      length = 1;
    }

    for (int i = 0; i < length; i++) {
      int randomCharInt = NumberUtil.getRandomInteger(65, 123);
      // if non alphabetic char skip to the next closest letter
      if (randomCharInt > 90 && randomCharInt < 97) {
        randomCharInt = randomCharInt + 6;
      }
      builder.append((char) (randomCharInt));
    }

    return builder.toString();
  }

  /**
   * Grabs the first {@code numWords} number of words to grab from the given text. Includes any whitespace or
   * punctuation after each word. If text is null then null is returned. If numWords is not positive then an empty
   * string is returned.
   * 
   * @param numWords the number of words to return
   * @param text the text to grab words from
   * @return the first {@code numWords} number of words to grab from the given text.
   */
  public static String getFirstNWords(int numWords, String text) {
    if (text == null) {
      return null;
    }
    int numSpacesInWord = text.split("\\s").length;
    if (numWords > numSpacesInWord) {
      numWords = numSpacesInWord;
    }
    String pattern = "(\\S+\\s*){N}".replace("N", String.valueOf(numWords));
    Scanner sc = new Scanner(text);
    return sc.findInLine(pattern);
  }

  /**
   * Replaces carriage returns in the given string with HTML break tags (&lt;br /&gt;).
   * 
   * @param strToFix the string to operate on
   * @return
   */
  public static String replaceCarriageReturnWithBr(String strToFix) {
    if (strToFix == null) {
      return strToFix;
    }

    return strToFix.replaceAll("(\r\n|\r|\n|\n\r)", "<br />");
  }

  /**
   * Converts a string to lower camel case. Doesn't handle every situation but should do it's basic purpose. If the
   * string is already lower camel case then its returned as is.
   * 
   * @param originalString the string to convert to lower camel case
   * @return the originalString in lower camel case format or null if the original is null
   */
  public static String toCamelCase(String originalString) {
    if (originalString == null) {
      return originalString;
    }
    String camelCaseString = null;

    originalString = originalString.replaceAll("[\\W_]", " ");

    String[] parts = originalString.split(" ");
    if (parts.length == 1) {
      camelCaseString = originalString.toLowerCase();
    } else {
      StringBuilder camelBuilder = new StringBuilder(parts[0].toLowerCase());
      for (int i = 1; i < parts.length; i++) {
        camelBuilder.append(parts[i].substring(0, 1).toUpperCase());
        camelBuilder.append(parts[i].substring(1, parts[i].length()).toLowerCase());
      }
      camelCaseString = camelBuilder.toString();
    }

    return camelCaseString;
  }

  /**
   * Breaks apart a camel case string into words. For example: "camelCaseWord" becomes "camel Case Word"
   * 
   * @param camelCaseString a string in camel case format
   * @return the string broken apart into words or null if the input string is null
   */
  public static String camelCaseToWords(String camelCaseString) {
    if (camelCaseString == null) {
      return camelCaseString;
    } else {
      return StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(camelCaseString), " ");
    }
  }

  /**
   * Breaks apart a camel case string into words and capitalizes the first word so that each word is capitalized.
   * 
   * For example: "camelCaseWord" becomes "Camel Case Word"
   * 
   * @param camelCaseString a string in camel case format
   * @return the string broken apart into capitalized words or null if the input string is null
   */
  public static String camelCaseToCapitalizedWords(String camelCaseString) {
    return StringUtils.capitalize(camelCaseToWords(camelCaseString));
  }

  /**
   * Adds the proper article for the word. If the word starts with a value the article will have 'n' appended to it. For
   * example: toProperArticle(a,apple) returns "an apple"
   * 
   * @param article the article preceding the word
   * @param word the word to add the article to
   * @return
   */
  public static String toProperArticle(String article, String word) {
    return ((word.matches("[aeiouAEIOU].*")) ? article + "n" : article) + " " + word;
  }

  /**
   * Splits a comma separated string into a List. If the string is null an empty list is returned.
   * 
   * @return a list containing each string separated by a comma.
   */
  @SuppressWarnings("unchecked")
  public static List<String> commaSeparatedStringToList(String val) {
    if (val != null) {
      String[] list = val.split("[ ]*,[ ]*");
      return Arrays.asList(list);
    } else {
      return Collections.EMPTY_LIST;
    }
  }

  /**
   * 
   * @param url
   * @param paramNames
   * @return
   * @deprecated use {@link #removeParamsFromUrl(String, String...)}
   */
  @Deprecated()
  public static String removeParamsFromUrl(String url, List<String> paramNames) {
    if (paramNames == null) {
      return null;
    }
    String[] names = paramNames.toArray(new String[paramNames.size()]);
    return removeParamsFromUrl(url, names);
  }

  /**
   * Removes any parameter name/value pairs (including the associated ampersand parameter separator) from the given url
   * where the parameter name is in the given list. If all parameters are removed from the url the url will still
   * contain the ? part of the URL.
   * 
   * @param url the URL to remove params from. Should not be null.
   * @param paramNames the list of parameter names to remove from the URL. Should not be null.
   * @return the URL with desired parameters removed
   */
  public static String removeParamsFromUrl(String url, String... paramNames) {
    for (String paramName : paramNames) {
      url = url.replaceAll("(&)?" + paramName + "=.*?(&|$)", "$2");
    }

    if (url.endsWith("&")) {
      url = url.substring(0, url.length() - 1);
    }
    // if the replacements made a second parameter the first one now, then we dont need the & that preceded it
    url = url.replace("?&", "?");

    return url;
  }

  /**
   * Adds the given parameter name/value pair to the given url. Note that if the value is replaced the parameter name
   * may not stay in the same location in the URL string.
   * 
   * @param url the url to add the param to
   * @param paramName the name of the parameter to add
   * @param value the value of the parameter
   * @param replaceIfExists if true and the parameter exists in the URL already then the new value replaces the old.
   * @return the url with the parameter added to it
   */
  public static String addParamToUrl(String url, String paramName, String value, boolean replaceIfExists) {
    return addParamToUrl(url, paramName, value, replaceIfExists, false);
  }

  public static String addParamToUrl(String url, String paramName, String value, boolean replaceIfExists,
          boolean useBlankIfNull) {
    if (replaceIfExists) {
      url = removeParamsFromUrl(url, paramName);
    } else {
      if (url.matches(".*?" + paramName + "=.*?(&|$)")) {
        return url;
      }
    }

    if (!url.contains("?")) {
      url = url + "?";
    } else if (!url.endsWith("?")) {
      url = url + "&";
    }

    if (useBlankIfNull && value == null) {
      value = "";
    }

    url = url + paramName + "=" + value;

    return url;
  }

  /**
   * Performs contextual encoding of characters not permitted in the corresponding URI component following the rules of
   * RFC 3986. If the parameter values are empty or can't be encoded, then an empty string is returned
   * 
   * @param path
   * @param queryParams
   * @return String encoded URI composed of the path and query string
   */
  public static String getURIString(String path, String queryParams) {
    String result = "";
    URI uri = null;
    if ((path != null && !path.isEmpty()) || (queryParams != null && !queryParams.isEmpty())) {
      try {
        uri = new URI(null, null, path, queryParams, null);
        result = uri.toASCIIString();
      } catch (URISyntaxException e) {
        logger.error("URISyntaxException exception thrown when parsing path '" + path + "' and queryParams '"
                + queryParams + "'", e);
      }
    }
    return result;
  }

  /**
   * Performs contextual encoding of characters not permitted in the corresponding URI component following the rules of
   * RFC 3986. If the parameter values are empty or can't be encoded, then an empty string is returned
   * 
   * @param queryParams
   * @return String encoded query string in the URI
   */
  public static String getURIStringQueryParams(String queryParams) {
    String result = "";
    URI uri = null;
    if (queryParams != null && !queryParams.isEmpty()) {
      try {
        uri = new URI(null, null, null, queryParams, null);
        result = uri.toASCIIString();
      } catch (URISyntaxException e) {
        logger.error("URISyntaxException exception thrown when parsing queryParams '" + queryParams + "'", e);
      }
    }
    return result;
  }

  /**
   * Performs contextual encoding of characters not permitted in the corresponding URI component following the rules of
   * RFC 3986. If the parameter values are empty or can't be encoded, then an empty string is returned
   * 
   * @param toEncode
   * @return String encoded result of the string
   */
  public static String getURIStringEncode(String toEncode) {
    String result = "";
    URI uri = null;
    if (toEncode != null && !toEncode.isEmpty()) {
      try {
        uri = new URI(null, null, toEncode, null, null);
        result = uri.toASCIIString();
      } catch (URISyntaxException e) {
        logger.error("URISyntaxException exception thrown when parsing path '" + toEncode + "'", e);
      }
    }
    return result;
  }

  /**
   * Performs contextual encoding of characters not permitted in the corresponding URI component following the rules of
   * RFC 3986. If the parameter values are empty or can't be encoded, then an empty string is returned
   * 
   * @param scheme
   * @param authority
   * @param path
   * @param query
   * @param fragment
   * @return String encoded URI composed of all five parts
   */
  public static String getURIString(String scheme, String authority, String path, String query, String fragment) {
    String result = "";
    URI uri = null;
    if (scheme != null || authority != null || path != null || query != null || fragment != null) {
      try {
        uri = new URI(scheme, authority, path, query, fragment);
        result = uri.toASCIIString();
      } catch (URISyntaxException e) {
        logger.error("URISyntaxException exception thrown when parsing a full URI having the following strings: " + scheme
                + ", " + authority + ", " + path + ", " + query + ", " + fragment, e);
      }
    }
    return result;
  }

  /**
   * Converts a space separated sequence of words to database format. Database format meaning suitable for use as a name
   * in a db table or column. Upper case and underscores in place of space or dot, no other punctuation. This method
   * does not try to enforce a length so it may be necessary to truncate the result before actually using it to create a
   * table or column.
   */
  public static String wordsToDatabaseFormat(String words) {
    if (words != null) {
      words = words.replaceAll("[^\\w\\s\\.]", "");
      words = words.replaceAll("[\\s\\.]", "_");
      words = words.toUpperCase();
    }
    return words;
  }

  /**
   * Pluralizes the given word. For example: "family" returns "families"
   * 
   * @param word the word to pluralize. Should not already be plural.
   * @return the pluralized version of the input or the input if it is null or ""
   */
  public static String pluralize(String word) {
    if (word == null || word.length() == 0) {
      return word;
    }

    String pluralWord = word;
    String lowerWord = word.toLowerCase();
    if (lowerWord.endsWith("y")) {
      pluralWord = word.substring(0, word.length() - 1) + "ies";
    } else if (lowerWord.endsWith("status")) {
      pluralWord = word + "es";
    } else if (lowerWord.endsWith("s")) {
      pluralWord = word + "ses";
    } else {
      pluralWord = word + "s";
    }

    return pluralWord;
  }
}
