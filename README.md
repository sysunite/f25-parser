# F25 Parser - Rijkswaterstaat
This is a F25 parser for deflection measurements

## How to run
Download the latest release: https://github.com/sysunite/f25-parser/releases/download/v0.6.2/f25-parser.zip

After unpacking, you should have:

- f25-parser.jar
- template.xsl
- data directory (empty)

Then add any number of (recursive directory structure) F25 files into the data directory.

Finally, in a command prompt, run:
```
java -jar f25-parser.jar
```

## Options
Use the command line options --template, --source, and --destination to change either location.
There is a help printout after running for more information.

## Prerequisites

- Java 8
