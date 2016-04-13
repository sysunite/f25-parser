package com.sysunite.rws.deflecties.tests;

import com.sysunite.rws.deflecties.F25File;
import com.sysunite.rws.deflecties.Measurement;
import com.sysunite.rws.deflecties.PeakReadings;
import com.sysunite.rws.deflecties.Utils;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class F25FileTest {

  private static final String TEST_FILE_NAME = "test.f25";

  @Test
  public void testUsage() {
    /*
		 * Actually not a test but an usage demo. The f25->excel conversion
		 * will look something like this. Now we simply output some info about
		 * each measurement in a tab-separated style.
		 */
    try {
      F25File f = new F25File(getTestFile());
      for (Measurement m : f.measurements) {
        StringBuilder sb = new StringBuilder();
        sb.append(Utils.f(m.stationInfo.station)).append("\t");
        sb.append(Utils.toYearMonthDay(m.stationInfo.dateTime)).append("\t");
        sb.append(Utils.toHourMinSec(m.stationInfo.dateTime)).append("\t");
        sb.append(Utils.f(m.stationInfo.temperatureAir)).append("\t");
        sb.append(Utils.f(m.gpsInfo.longitude)).append("\t");
        sb.append(Utils.f(m.gpsInfo.latitude)).append("\t");
        PeakReadings pr = m.peakReadings.get(0);
        sb.append(Utils.f(pr.peakLoad)).append("\t");
        for (double d : pr.deflections) {
          sb.append(Utils.f(d)).append("\t");
        }
        System.out.println(sb.toString());
      }
    } catch (Exception e) {
      fail("Exception: " + e.getMessage());
      e.printStackTrace();
    }
  }

  @Test
  public void testCreationFromFile() {
    try {
      F25File f = new F25File(getTestFile());
      assertNotNull(f);
    } catch (Exception e) {
      fail("Exception: " + e.getMessage());
      e.printStackTrace();
    }
  }

  @Test
  public void testCreationFromString() {
    try {
      F25File f = new F25File(getTestFile().getAbsolutePath());
      assertNotNull(f);
    } catch (Exception e) {
      fail("Exception: " + e.getMessage());
      e.printStackTrace();
    }
  }

  @Test
  public void testCreationMissingFile() {
    try {
      @SuppressWarnings("unused")
      F25File f = new F25File("no such file");
      fail("Should have thrown exception");
    } catch (Exception e) {
      // ok
    }
  }

  private File getTestFile() {
    String url = getClass().getClassLoader().getResource(TEST_FILE_NAME).getFile();
    File file = new File(url);
    return file;
  }

}
