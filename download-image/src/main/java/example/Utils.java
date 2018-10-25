package example;

import javax.json.*;
import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.util.zip.*;

public class Utils {
    public static String zipImages(String dir) throws Exception {
        System.out.println("Zip images in directory: " + dir + "...");
        File zipFilePath = File.createTempFile("worker", ".zip");

        zipFolder(dir, zipFilePath.getAbsolutePath(), 5);

        System.out.println("Zip file created " + zipFilePath.getAbsolutePath() + "...");

        JsonObject uploadDescriptor = Koordinator.uploadFile(zipFilePath.getAbsolutePath());
        String uploadId = ((JsonString)uploadDescriptor.get("fileId")).getString();

        System.out.println("Uploaded file id: " + uploadId);
        return uploadId;
    }

    private static void zipFolder(String sourceDirPath, String zipFilePath, int limit) throws IOException {
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
