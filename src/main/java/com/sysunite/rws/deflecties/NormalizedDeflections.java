package com.sysunite.rws.deflecties;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Vector;

/**
 * @author Mohamad Alamili <mohamad@sysunite.com>
 */
public class NormalizedDeflections {
  public static final double[] DXs = {0, 300, 600, 900, 1200, 1500, 1800};

  public double D0bt;
  public double IDK300bt;
  public double[] D0bs = new double[DXs.length];

  public static String error; // used for reporting error of latest parse

  public static List<NormalizedDeflections> getDeflections(F25File file) {
    error = null;
    int D = DXs.length;
    int[] xIndexes = new int[D];
    for (int i = 0; i < D; i++) {
      BigDecimal x = new BigDecimal(DXs[i]);
      boolean found = false;
      for (int j = 0; j < file.plateXpositions.size(); j++) {
        BigDecimal xPosition = file.plateXpositions.get(j);
        if (Math.abs(x.subtract(xPosition).doubleValue()) < 0.1) {
          found = true;
          xIndexes[i] = j;
          break;
        }
      }
      if (!found) {
        error = "Plate x position not found for x=" + x;
        return null;
      }
    }

    BigDecimal plateR = file.plateRadius.divide(new BigDecimal(1000));
    BigDecimal sref = new BigDecimal(50000).divide(plateR.multiply(plateR), 100, RoundingMode.HALF_UP).divide(new BigDecimal(Math.PI), 100, RoundingMode.HALF_UP).divide(new BigDecimal(1000));
    List<NormalizedDeflections> ret = new Vector<>();
    try {
      for (Measurement m : file.measurements) {
        NormalizedDeflections nd = new NormalizedDeflections();
        int P = m.peakReadings.size();
        for (int i = 0; i < D; i++) {
          int index = xIndexes[i];
          double sum = 0;
          for (int p = 1; p < P; p++) { // reading #1 is ignored?
            PeakReadings pr = m.peakReadings.get(p);
            BigDecimal v = pr.deflections.get(index);
            v = v.multiply(sref.divide(pr.peakLoad, 100, RoundingMode.HALF_UP));
            sum += v.doubleValue();
          }
          double avg = sum / (P - 1);
          nd.D0bs[i] = avg;
        } // for deflections

        double tNorm = m.stationInfo.temperatureAsphalt.subtract(new BigDecimal(20)).doubleValue();
        double d0b = nd.D0bs[0];
        nd.D0bt = d0b / (1 + 0.013926 * tNorm + 0.0002298 * (tNorm * tNorm));

        double idk300b = d0b - nd.D0bs[1];
        nd.IDK300bt = idk300b / (1 + 0.043535 * tNorm + 0.00098467 * (tNorm * tNorm));

        ret.add(nd);
      } // for measurements
    } catch (Exception e) {
      error = "Error during deflection calculation from peak readings";
      return null;
    }

    return ret;
  }

}
