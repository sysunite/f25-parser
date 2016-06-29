package com.sysunite.rws.deflecties;

import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Vector;

/**
 * @author Mohamad Alamili <mohamad@sysunite.com>
 */
public class ExcelCreator {
  private String dataFilePath;
  private String templateFilePath;
  private static final int SHEET_INDEX = 0; // sheet to write to (0-based)
  private static final int START_ROW = 2; // first row to write to (0-based)
  private static final int FIRST_COL = 0; // first col to write to (0-based)
  private static final int LAST_COL = 24; // last col to write to (0-based)
  // field indexes

  private static final int IDX_FIRST_DIR = 0;
  private static final int IDX_SECOND_DIR = 1;
  private static final int IDX_THIRD_DIR = 2;
  private static final int IDX_KM = 3;
  private static final int IDX_STATION_INFO_COMMENT = 4; // side of road
  private static final int IDX_DATE = 5;
  private static final int IDX_TIME = 6;
  private static final int IDX_TEMP_ASPHALT = 7;
  private static final int IDX_TEMP_SURFACE = 8;
  private static final int IDX_D0BT = 9;
  private static final int IDX_IDK300BT = 10;
  private static final int IDX_D0_BASE = 11; // 11..18
  private static final int IDX_FILE_NAME = 18;
  private static final int IDX_LONGITUDE = 19;
  private static final int IDX_LATITUDE = 20;
  private static final int IDX_ROADWAY_ID = 21;
  private static final int IDX_SUBSECTION_ID = 22;
  private static final int IDX_STROOK = 23;
  private static final int IDX_FILENAME_FROM_25 = 24;



  private int rowIndex;
  private Sheet sheet;
  private List<String> errors;

  public ExcelCreator() {
  }

  public ExcelCreator(String templateFilePath) {
    this.templateFilePath = templateFilePath;
  }

  /*
     * Recursively processes each f25 file in a directory and writes a row for
     * each measurement into an Excel file. The second parameter is the full
     * path and name of the Excel to be written. The method returns a List of
     * error messages. Note that some errors are fatal (like missing input
     * directory) but some are not (like a missing GPS value in a measurement)
     * so the output file may be created even in case of errors.
     */
  public List<String> convertF25Files(String inputDirPath, String outputFilePath) {
    dataFilePath = inputDirPath;
    errors = new Vector<>();
    Workbook excel = null;
    FileInputStream xlsStream = null;

    File inputDir = new File(inputDirPath);
    File xlsFile = new File(outputFilePath);
    File outputDir = xlsFile.getParentFile();
    if (!inputDir.exists() || !inputDir.isDirectory()) {
      errors.add("Input directory does not exist");
      return errors;
    }

    try {
      if (!outputDir.exists()) outputDir.mkdirs();
      // copy template to output directory
      String url;
      if (templateFilePath == null) {
        url = getClass().getClassLoader().getResource("template.xls").getFile();
      }
      else {
        url = templateFilePath;
      }

      File templateFile = new File(url);
      Files.copy(templateFile.toPath(), xlsFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
      xlsStream = new FileInputStream(xlsFile);
      excel = WorkbookFactory.create(xlsStream);
      sheet = excel.getSheetAt(SHEET_INDEX);
      rowIndex = START_ROW;

      recurse(inputDir);
    } catch (Exception e) {
      errors.add("Exception in convertF25Files: " + e.getMessage());
    } finally {
      if (excel != null) {
        try {
          xlsStream.close();
          FileOutputStream out = new FileOutputStream(xlsFile);
          excel.write(out);
          out.close();
        } catch (Exception e) {
          errors.add("Exception when writing excel file: " + e.getMessage());
        }
      }
    }

    return errors;
  }

  private void recurse(File f) throws Exception {
    if (f.isDirectory()) {
      for (File f2 : f.listFiles()) {
        recurse(f2);
      }
    } else {
      if (!f.getName().toLowerCase().endsWith(".f25")) return;
      F25File f25 = new F25File(dataFilePath, f);
      String name = f.getName();
      if (!f25.errors.isEmpty()) {
        for (String e : f25.errors) {
          errors.add("Parsing error in " + name + ": " + e);
        }
        return;
      }

      int mCnt = f25.measurements.size();
      List<NormalizedDeflections> ndList = NormalizedDeflections.getDeflections(f25);
      String ndErr = NormalizedDeflections.error;
      if (ndErr != null) {
        errors.add("Error getting normalized deflections in " + name + ": " + ndErr);
        return;
      }

      for (int i = 0; i < mCnt; i++) {
        // add a new row to be used for the NEXT measurement
        // assuming the current row is already created
        Row row = sheet.getRow(rowIndex);
        Row nextRow = sheet.createRow(rowIndex + 1);
        for (int c = FIRST_COL; c <= LAST_COL; c++) {
          Cell oldCell = row.getCell(c);
          Cell nextCell = nextRow.createCell(c);
          nextCell.setCellType(oldCell.getCellType());
          CellStyle style = oldCell.getCellStyle();
          nextCell.setCellStyle(style);
        }
        rowIndex++;

        Measurement m = f25.measurements.get(i);
        NormalizedDeflections nd = ndList.get(i);

        writeMeasurement(i, row, f25, m, nd);
      }
    }
  }

  private void writeMeasurement(int i, Row row,
                                F25File f25, Measurement m, NormalizedDeflections nd) {
    try {
      Cell c;

      // IDX_ROADWAY_ID
      c = row.getCell(IDX_ROADWAY_ID);
      setCellData(c, f25.roadwayId);

      // IDX_SUBSECTION_ID
      c = row.getCell(IDX_SUBSECTION_ID);
      setCellData(c, m.stationInfo.sideOfRoad);

      // IDX_STROOK
      c = row.getCell(IDX_STROOK);
      setCellData(c, m.stationInfo.lane);

      // IDX_KM
      c = row.getCell(IDX_KM);
      boolean isInteger = m.stationInfo.station.scale() <= 0;

      BigDecimal km;
      if (isInteger) {
        km = m.stationInfo.station.divide(new BigDecimal(1000));
        km = km.setScale(3, RoundingMode.FLOOR);
      }
      else {
        km = m.stationInfo.station;
      }
      setCellData(c, km);

      // IDX_STATION_INFO_COMMENT
      c = row.getCell(IDX_STATION_INFO_COMMENT);
      setCellData(c, m.stationInfo.comment);

      // IDX_DATE
      c = row.getCell(IDX_DATE);
      setCellData(c, Utils.toYearMonthDay(m.stationInfo.dateTime));

      // IDX_TIME
      c = row.getCell(IDX_TIME);
      setCellData(c, Utils.toHourMinSec(m.stationInfo.dateTime));

      // IDX_TEMP_ASPHALT
      c = row.getCell(IDX_TEMP_ASPHALT);
      setCellData(c, m.stationInfo.temperatureAsphalt.setScale(1, RoundingMode.FLOOR));

      // IDX_TEMP_SURFACE
      c = row.getCell(IDX_TEMP_SURFACE);
      setCellData(c, m.stationInfo.temperatureSurface.setScale(1, RoundingMode.FLOOR));

      // IDX_D0BT
      c = row.getCell(IDX_D0BT);
      setCellData(c, new BigDecimal(nd.D0bt).setScale(1, RoundingMode.FLOOR));

      // IDX_IDK300BT
      c = row.getCell(IDX_IDK300BT);
      setCellData(c, new BigDecimal(nd.IDK300bt).setScale(1, RoundingMode.FLOOR));

      // IDX_D0_BASE = 12; // 12..18
      for (int j = 0; j < nd.D0bs.length; j++) {
        c = row.getCell(IDX_D0_BASE + j);
        setCellData(c, new BigDecimal(nd.D0bs[j]).setScale(1, RoundingMode.FLOOR));
      }

      // IDX_FILE_NAME
      c = row.getCell(IDX_FILE_NAME);
      setCellData(c, f25.fileName);

      if (m.gpsInfo != null) {
        // IDX_LONGITUDE
        c = row.getCell(IDX_LONGITUDE);
        setCellData(c, m.gpsInfo.longitude);

        // IDX_LATITUDE
        c = row.getCell(IDX_LATITUDE);
        setCellData(c, m.gpsInfo.latitude);
      } else {
        errors.add("No GPS data for measurement " + i + " in file " + f25.fileName);
      }

      // DIRECTORIES
      c = row.getCell(IDX_FIRST_DIR);
      setCellData(c, f25.firstDir);

      c = row.getCell(IDX_SECOND_DIR);
      setCellData(c, f25.secondDir);

      c = row.getCell(IDX_THIRD_DIR);
      setCellData(c, f25.thirdDir);

      c = row.getCell(IDX_FILENAME_FROM_25);
      setCellData(c, f25.fileNameFromF25);

    } catch (Exception e) {
      errors.add("Error with measurement " + i + " in file " + f25.fileName);
    }
  }

  private void setCellData(Cell c, String s) {
    c.setCellValue(s);
  }

  private void setCellData(Cell c, double d) {
    if (c.getCellType() == Cell.CELL_TYPE_STRING) c.setCellValue(Utils.f(d));
    else c.setCellValue(d);
  }

  private void setCellData(Cell c, BigDecimal b) {
    if (c.getCellType() == Cell.CELL_TYPE_STRING) c.setCellValue(b.toString());
    else c.setCellValue(b.doubleValue());
  }
}