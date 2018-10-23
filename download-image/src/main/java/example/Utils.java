package example;

import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.util.zip.*;

public class Utils {
    // from: https://stackoverflow.com/questions/15968883/how-to-zip-a-folder-itself-using-java
    public static void zipFolder(String sourceDirPath, String zipFilePath, int limit) throws IOException {
        Path p = Paths.get(zipFilePath);
        try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(p))) {
            Path pp = Paths.get(sourceDirPath);
            Files.walk(pp)
              .filter(path -> !Files.isDirectory(path))
              .limit(limit)
              .forEach(path -> {
                  System.out.println(path + "...");
                  ZipEntry zipEntry = new ZipEntry(pp.relativize(path).toString());
                  try {
                    zs.putNextEntry(zipEntry);
                    Files.copy(path, zs);
                    zs.closeEntry();
                  } catch (IOException e) {
                      System.err.println(e);
                  }
              });
        }
        System.out.println("DONE");
    }
}
