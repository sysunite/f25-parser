package com.sysunite.rws.deflecties;

import java.math.BigDecimal;

/**
 * @author Mohamad Alamili <mohamad@sysunite.com>
 */
public class SD {
  public String id;
  public Integer type;
  public BigDecimal relativeGain;
  public BigDecimal absoluteGain;

  public SD(String id, Integer type, BigDecimal r, BigDecimal a) {
    this.id = id;
    this.type = type;
    relativeGain = r;
    absoluteGain = a;
  }

  @Override
  public String toString() {
    return id;
  }
}
