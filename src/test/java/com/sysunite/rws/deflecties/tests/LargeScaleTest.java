package com.sysunite.rws.deflecties.tests;

import com.sysunite.rws.deflecties.F25File;
import org.junit.Test;

import java.io.File;

/**
 * @author Mohamad Alamili <mohamad@sysunite.com>
 */
public class LargeScaleTest {
  // change this to the dir where you have a lot of data files
  private static final String DATA_DIR = "../data/f25/";

  /*
   * Recursively parses each f25 file in a directory, writes errors and
   * number of warnings.
   */
  @Test
  public void test() {
    File f = new File(DATA_DIR);
    recurse(f);
  }

  private void recurse(File f) {
    if (f.isDirectory()) {
      for (File f2 : f.listFiles()) {
        recurse(f2);
      }
    } else {
      if (f.getName().toLowerCase().endsWith(".f25")) {
        System.out.println("-------\n" + f.getAbsolutePath());
        try {
          F25File f25 = new F25File("", f);
          if (!f25.errors.isEmpty()) {
            System.out.println("Errors:");
            for (String err : f25.errors) System.out.println("  " + err);
          }
          System.out.println("Warnings: " + f25.warnings.size());
        } catch (Exception e) {
          System.out.println("Exception: " + e.getMessage());
        }
      }
    }
  }

}
