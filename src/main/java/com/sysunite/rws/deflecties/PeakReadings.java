package com.sysunite.rws.deflecties;

import java.math.BigDecimal;
import java.util.List;
import java.util.Vector;

/**
 * @author Mohamad Alamili <mohamad@sysunite.com>
 */
public class PeakReadings {
  public Integer sequenceId;
  public BigDecimal peakLoad;
  public List<BigDecimal> deflections = new Vector<>();
}
