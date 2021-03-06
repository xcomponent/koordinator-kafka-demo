package example;

import java.security.*;
import java.security.cert.*;
import javax.net.ssl.*;
import java.net.*;
import javax.json.*;
import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.util.zip.*;

public class Worker {
    public static void consumer() throws Exception {
        while (true) {
           JsonObject task = Koordinator.retrieveTask("Demo", "UrlsConsumer");

           if (task != null) {
                String broker = ((JsonString)((JsonObject)task.get("inputData")).get("broker")).getString();
                String groupId = ((JsonString)((JsonObject)task.get("inputData")).get("groupId")).getString();
                String inputTopic = ((JsonString)((JsonObject)task.get("inputData")).get("input-topic")).getString();
                String outputDir = ((JsonString)((JsonObject)task.get("inputData")).get("output-dir")).getString();

                Kafka.consumer(broker, groupId, inputTopic, outputDir);

                Koordinator.sendStatus(task, "Finished", Koordinator.Status.Completed, Koordinator.ErrorLevel.None, null);
           }

           Thread.sleep(5000);
        }
    }

    public static void producer() throws Exception {
        while (true) {
           JsonObject task = Koordinator.retrieveTask("Demo", "UrlsProducer");

           if (task != null) {
                String broker = ((JsonString)((JsonObject)task.get("inputData")).get("broker")).getString();
                String clientId = ((JsonString)((JsonObject)task.get("inputData")).get("clientId")).getString();
                String outputTopic = ((JsonString)((JsonObject)task.get("inputData")).get("output-topic")).getString();
                String inputFile = ((JsonString)((JsonObject)task.get("inputData")).get("input-file")).getString();
                int limit = Integer.parseInt(((JsonString)((JsonObject)task.get("inputData")).get("limit")).getString());

                Kafka.producer(broker, clientId, outputTopic, inputFile, limit);

                Koordinator.sendStatus(task, "Finished", Koordinator.Status.Completed, Koordinator.ErrorLevel.None, null);
           }

           Thread.sleep(5000);
        }
    }

    public static void zipImages() throws Exception {
        while (true) {
           JsonObject task = Koordinator.retrieveTask("Demo", "ZipImages");

           if (task != null) {
                String dir = ((JsonString)((JsonObject)task.get("inputData")).get("dir")).getString();


                String uploadId = Utils.zipImages(dir);
                String downloadLink = Koordinator.UploadServiceUrl + "/api/Upload/" + uploadId;

                JsonObject output = Json
                        .createObjectBuilder()
                        .add("zip", downloadLink)
                        .build();

                Koordinator.sendStatus(task, "[Zip]("+downloadLink+")" , Koordinator.Status.Completed, Koordinator.ErrorLevel.None, output);
           }

           Thread.sleep(5000);
        }
    }
}
