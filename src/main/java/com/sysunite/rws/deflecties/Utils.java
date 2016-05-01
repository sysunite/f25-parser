package com.sysunite.rws.deflecties;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Mohamad Alamili <mohamad@sysunite.com>
 */
public class Utils {
  private static DecimalFormat df;

  static {
    String pattern = "0.###";
    df = new DecimalFormat(pattern);
    DecimalFormatSymbols dfs = new DecimalFormatSymbols();
    dfs.setDecimalSeparator('.');
    df.setDecimalFormatSymbols(dfs);
  }

  /**
   * Pretty print a double
   */
  public static String f(double d) {
    return df.format(d);
  }

  public static String f(BigDecimal d) {
    return d.toString();
  }

  private static DateFormat ymdFormat = new SimpleDateFormat("yyyy.MM.dd");
  private static DateFormat hmsFormat = new SimpleDateFormat("HH:mm:ss");

  public static String toYearMonthDay(Calendar dateTime) {
    Date date = dateTime.getTime();
    return ymdFormat.format(date);
  }

  public static String toHourMinSec(Calendar dateTime) {
    Date date = dateTime.getTime();
    return hmsFormat.format(date);
  }
}
