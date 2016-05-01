package com.sysunite.rws.deflecties;

import java.util.List;
import java.util.Vector;

/**
 * @author Mohamad Alamili <mohamad@sysunite.com>
 */
public class Measurement {
  public StationInfo stationInfo;
  public GpsNavigationResult gpsInfo;
  public List<PeakReadings> peakReadings = new Vector<>();
}
