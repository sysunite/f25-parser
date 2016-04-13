package com.sysunite.rws.deflecties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

public class F25File {
  private static final String NO_DATA = "N0";

  public List<String> errors = new Vector<>();
  public List<String> warnings = new Vector<>();

  public String fileName;
  public String fileNameOriginal;
  public GregorianCalendar dateTime;
  public List<SD> sds = new Vector<>();
  public String operatorName = "";
  public String roadwayId = "";
  public String subsectionId = "";
  public StationInfo stationInfo;
  public List<Measurement> measurements = new Vector<>();

  private Set<Object> objectsWithParsingErrors = new HashSet<>();

  public F25File(File file) throws FileNotFoundException {
    if (file == null || !file.exists()) {
      throw new FileNotFoundException();
    }
    parse(file);
  }

  public F25File(String path) throws FileNotFoundException {
    this(new File(path));
  }

  private void parse(File file) {
    fileName = file.getName();
    int lineCnt = 0;
    String line = null;
    try {
      InputStream is = new FileInputStream(file);
      InputStreamReader isr = new InputStreamReader(is, "UTF-8");
      LineNumberReader lnr = new LineNumberReader(isr);
      StationInfo currentStationInfo = null;
      Measurement currentMeasurement = null;
      GpsNavigationResult currentGpsInfo = null;

      while (true) {
        line = lnr.readLine();
        if (line == null) break;
        lineCnt++;
        if (line.trim().isEmpty()) {
          warnings.add("Empty line at " + lineCnt);
          continue;
        }

        boolean inHeader = lineCnt <= 40;

        String[] parts = line.split(",");
        for (int i = 0; i < parts.length; i++) {
          parts[i] = parts[i].trim();
        }

        int lineId = Integer.parseInt(parts[0]);
        if (lineId == 5003) {
          parseNames(lineCnt, line, parts);
          continue;
        }
        if (lineId == 5011) {
          parseDateAndTime(lineCnt, line, parts);
          continue;
        }
        if (lineId >= 5201 && lineId <= 5218) {
          parseSD(lineCnt, line, parts);
          continue;
        }
        if (lineId == 5030) {
          operatorName = getText(parts[1]);
          continue;
        }
        if (lineId == 5031) {
          roadwayId = getText(parts[1]);
          continue;
        }
        if (lineId == 5032) {
          subsectionId = getText(parts[1]);
          continue;
        }
        if (lineId == 5301) {
          currentStationInfo = new StationInfo();
          // make it the global one if we are in the header
          if (inHeader) {
            stationInfo = currentStationInfo;
          } else {
            /* Or start setting up a new measurement. Note this
						 * can't be done when seeing a new gps info line as
						 * that one is optional.
						 */
            currentMeasurement = new Measurement();
            currentMeasurement.stationInfo = currentStationInfo;
            if (currentGpsInfo != null) { // attach GpsInfo found already
              currentMeasurement.gpsInfo = currentGpsInfo;
              currentGpsInfo = null; // so that we don't reattach this elsewhere
            }
            measurements.add(currentMeasurement);
          }
          parseStationInfo1(currentStationInfo, lineCnt, line, parts);
          continue;
        }
        if (lineId == 5302) {
          parseStationInfo2(currentStationInfo, lineCnt, line, parts);
          continue;
        }
        if (lineId == 5303) {
          parseStationInfo3(currentStationInfo, lineCnt, line, parts);
          continue;
        }
        if (lineId == 5280) {
          currentGpsInfo = new GpsNavigationResult();
          parseGpsInfo(currentGpsInfo, lineCnt, line, parts);
          continue;
        }
        if (lineId < 5000) {
          parsePeakReadings(currentMeasurement, lineCnt, line, parts);
          continue;
        }
        // line was not parsed, add a warning
        addWarning("Unused line", lineCnt, line);
      }
      lnr.close();

      // go through measurements and delete objects with parsing errors
      for (int i = measurements.size() - 1; i >= 0; i--) {
        Measurement m = measurements.get(i);
        boolean ok = true;
        if (objectsWithParsingErrors.contains(m)) ok = false;
        if (objectsWithParsingErrors.contains(m.peakReadings)) ok = false;
        if (objectsWithParsingErrors.contains(m.stationInfo)) ok = false;
        if (m.gpsInfo != null && objectsWithParsingErrors.contains(m.gpsInfo)) ok = false;
        if (!ok) {
          measurements.remove(i);
        }
      }
    } catch (Exception e) {
      errors.add("Error in line " + lineCnt + ": " + line + " Message: " + e.getMessage());
    }
  }

  private void parseNames(int lineCnt, String line, String[] parts) {
    try {
      // 0    1           2          3         4
      // 5003,"OPERATOR","STANDARD","RWO1 27-","F25"
      String fName = parts[3];
      String ext = parts[4];
      fileNameOriginal = fName + "." + ext;
    } catch (Exception e) {
      addError("Error parsing names", lineCnt, line);
    }
  }

  private void parsePeakReadings(Measurement m, int lineCnt,
                                 String line, String[] parts) {
    try {
      //    0    1    2      3      4      5      6      7       8
      //    1,   721, 292.6, 240.7, 219.8, 186.2, 154.4, 104.8,  74.8, ...
      PeakReadings pr = new PeakReadings();
      pr.sequenceId = toInteger(parts[0]);
      pr.peakLoad = toDouble(parts[1]);
      for (int i = 2; i < parts.length; i++) {
        Double d = toDouble(parts[i]);
        pr.deflections.add(d);
      }
      m.peakReadings.add(pr);
    } catch (Exception e) {
      addError("Error parsing peak readings", lineCnt, line);
      objectsWithParsingErrors.add(m);
    }
  }

  private void parseGpsInfo(GpsNavigationResult gInfo, int lineCnt,
                            String line, String[] parts) {
    try {
      // 0    1      2 3           4               5
      // 5280,0,     0,+52.2097983,+005.3552517,   3.7
      Integer err = toInteger(parts[1]);
      gInfo.failureCause = err;
      if (err != null && err == 0) {
        gInfo.latitude = toDouble(parts[3]);
        gInfo.longitude = toDouble(parts[4]);
        gInfo.height = toDouble(parts[5]);
        //TODO what are fields 6,7,...?
      }
    } catch (Exception e) {
      addError("Error parsing gps info", lineCnt, line);
      objectsWithParsingErrors.add(gInfo);
    }
  }

  private void parseStationInfo3(StationInfo sInfo, int lineCnt,
                                 String line, String[] parts) {
    try {
      // 5303,0, 12.3, 18.7, 12.4
      sInfo.isTemperatureInCelsius = parts[1].equals("0");
      sInfo.temperatureAsphalt = toDouble(parts[2]);
      sInfo.temperatureSurface = toDouble(parts[3]);
      sInfo.temperatureAir = toDouble(parts[4]);
    } catch (Exception e) {
      addError("Error parsing station info", lineCnt, line);
      objectsWithParsingErrors.add(sInfo);
    }
  }

  private void parseStationInfo2(StationInfo sInfo, int lineCnt,
                                 String line, String[] parts) {
    try {
      // 0    1 2 3 4 5 6 7 8 9
      // 5302,0,0,A,p,0,1,0,0,"Comment     "
      if (parts.length > 9) sInfo.comment = getText(parts[9]);
    } catch (Exception e) {
      addError("Error parsing station info", lineCnt, line);
      objectsWithParsingErrors.add(sInfo);
    }
  }

  private void parseStationInfo1(StationInfo sInfo, int lineCnt,
                                 String line, String[] parts) {
    try {
      // 0    1 2 3 4    5     6 7 8          9    10 11 12 13
      // 5301,2,1,4,2,   41000,1,1,"M       ",2013,06,28,04,33
      sInfo.sideOfRoad = toInteger(parts[1]);

			/*
			 * Converting distance to km. Supporting conversions from meters,
			 * and km. TODO anything else needed?
			 */
      double distanceMultiplier = 1; // default for type=3
      Integer distanceUnit = toInteger(parts[4]);
      if (distanceUnit != null) {
        if (distanceUnit == 2) distanceMultiplier = 0.001; // m -> km
      }
      double distance = toDouble(parts[5]);
      sInfo.station = distance * distanceMultiplier;

      sInfo.lane = getText(parts[8]);
      Integer y = toInteger(parts[9]);
      Integer m = toInteger(parts[10]);
      Integer d = toInteger(parts[11]);
      Integer h = toInteger(parts[12]);
      Integer min = toInteger(parts[13]);
      Integer s = 0;
      // last element is sometimes missing
      if (parts.length > 14) s = toInteger(parts[14]);
      sInfo.dateTime = new GregorianCalendar(y, m, d, h, min, s);
    } catch (Exception e) {
      addError("Error parsing station info", lineCnt, line);
      objectsWithParsingErrors.add(sInfo);
    }
  }

  private void parseSD(int lineCnt, String line, String[] parts) {
    try {
      // 5201,"SD 1    ",4,1.000,1.000
      String id = getText(parts[1]);
      Integer type = toInteger(parts[2]);
      Double relativeGain = toDouble(parts[3]);
      Double absoluteGain = toDouble(parts[4]);
      SD sd = new SD(id, type, relativeGain, absoluteGain);
      sds.add(sd);
    } catch (Exception e) {
      addError("Error parsing SD", lineCnt, line);
    }
  }

  private void parseDateAndTime(int lineCnt, String line, String[] parts) {
    try {
      // 5011,0,1,2012,09,05,13,58,3,"Wed",249
      Integer y = toInteger(parts[3]);
      Integer m = toInteger(parts[4]);
      Integer d = toInteger(parts[5]);
      Integer h = toInteger(parts[6]);
      Integer min = toInteger(parts[7]);
      Integer s = toInteger(parts[8]);
      dateTime = new GregorianCalendar(y, m, d, h, min, s);
    } catch (Exception e) {
      addError("Error parsing date", lineCnt, line);
    }
  }

  private Double toDouble(String s) {
    if (s.isEmpty() || s.equals(NO_DATA)) return null;
    return Double.parseDouble(s);
  }

  private Integer toInteger(String s) {
    if (s.isEmpty() || s.equals(NO_DATA)) return null;
    return Integer.parseInt(s);
  }

  // remove starting and closing ", trim
  private String getText(String s) {
    if (s.startsWith("\"")) s = s.substring(1);
    if (s.endsWith("\"")) s = s.substring(0, s.length() - 1);
    s = s.trim();
    return s;
  }

  private void addError(String msg, int lineCnt, String line) {
    String err = msg + " at line " + lineCnt + ": " + line;
    errors.add(err);
  }

  private void addWarning(String msg, int lineCnt, String line) {
    String err = msg + " at line " + lineCnt + ": " + line;
    warnings.add(err);
  }
}