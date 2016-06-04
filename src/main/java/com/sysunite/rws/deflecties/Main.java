package com.sysunite.rws.deflecties;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Mohamad Alamili <mohamad@sysunite.com>
 */
public class Main {

  public static void main(String[] args) throws IOException {
    final String VERSION = "0.7.0";
    final String ART =
      "                                                          _________________________   \n" +
      "                     /\\\\      _____          _____       |   |     |     |    | |  \\  \n" +
      "      ,-----,       /  \\\\____/__|__\\_    ___/__|__\\___   |___|_____|_____|____|_|___\\ \n" +
      "   ,--'---:---`--,    |  _     |     `| |      |      `| |                    | |    \\\n" +
      "  ==(o)-----(o)==J    `(o)-------(o)=   `(o)------(o)'   `--(o)(o)--------------(o)--'  \n" +
      " `````````````````````````````````````````````````````````````````````````````````````";

    System.out.println("GisParser v" + VERSION);
    System.out.println(ART + "\n");

    final String CURRENT_DIR = System.getProperty("user.dir");

    File template    = new File(CURRENT_DIR, "template.xls");
    File source      = new File(CURRENT_DIR, "data");
    File destination = new File(CURRENT_DIR, "output.xls");

    OptionParser p = new OptionParser();

    p.accepts("template")
    .withRequiredArg()
    .ofType(File.class)
    .describedAs("the excel template path")
    .defaultsTo(template);

    p.accepts("source")
    .withRequiredArg()
    .ofType(File.class)
    .describedAs("directory to recursively process f25 files")
    .defaultsTo(source);

    p.accepts("destination")
    .withRequiredArg()
    .ofType(File.class)
    .describedAs("excel file to write to")
    .defaultsTo(destination);

    // Print usage
    p.printHelpOn(System.out);
    System.out.println();

    OptionSet opt = p.parse(args);

    if(opt.has("template")){
      template = (File) opt.valueOf("template");
    }
    if(opt.has("source")){
      source = (File) opt.valueOf("source");
    }
    if(opt.has("destination")){
      destination = (File) opt.valueOf("destination");
    }

    System.out.println("Running..");
    ExcelCreator ec = new ExcelCreator(template.getAbsolutePath());
    List<String> errors = ec.convertF25Files(source.getAbsolutePath(), destination.getAbsolutePath());

    // Print errors if any
    for (String e : errors)
      System.out.println(e);

    System.out.println("Completed");
  }
}