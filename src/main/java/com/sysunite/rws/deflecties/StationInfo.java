package com.sysunite.rws.deflecties;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.GregorianCalendar;

/**
 * @author Mohamad Alamili <mohamad@sysunite.com>
 */
public class StationInfo {
  //TODO unclear what else is needed here
  public Integer sideOfRoad;
  public BigDecimal station; // converted always to km
  public String lane;
  public GregorianCalendar dateTime;
  public String comment = "";
  public boolean isTemperatureInCelsius = true;
  public BigDecimal temperatureAsphalt;
  public BigDecimal temperatureSurface;
  public BigDecimal temperatureAir;

  @Override
  public String toString() {
    return station.toString();
  }
}
