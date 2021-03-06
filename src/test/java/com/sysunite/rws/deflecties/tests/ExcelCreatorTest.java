package com.sysunite.rws.deflecties.tests;

import com.sysunite.rws.deflecties.ExcelCreator;

import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @author Mohamad Alamili <mohamad@sysunite.com>
 */
public class ExcelCreatorTest {
  // change this to the dir where you have a lot of data files
  private static final String DATA_DIR = "../data/f25";

  public void test() {
    ExcelCreator ec = new ExcelCreator();
    List<String> errors = ec.convertF25Files(DATA_DIR, "../output/out.xls");
    for (String e : errors) System.out.println(e);
    // this assertion may fail if you have bad sample files, but the output
    // file may still be written
    assertTrue(errors.isEmpty());
  }
}
