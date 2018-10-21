package example;
import java.security.*;
import java.security.cert.*;
import javax.net.ssl.*;
import java.net.*;
import javax.json.*;

public class Worker {
    public static void consumer() throws Exception {
        while (true) {
           JsonObject task = Koordinator.retrieveTask("Meetup", "UrlsConsumer");

           if (task != null) {
                String broker = ((JsonString)((JsonObject)task.get("inputData")).get("broker")).getString();
                String groupId = ((JsonString)((JsonObject)task.get("inputData")).get("groupId")).getString();
                String inputTopic = ((JsonString)((JsonObject)task.get("inputData")).get("input-topic")).getString();
                String outputDir = ((JsonString)((JsonObject)task.get("inputData")).get("output-dir")).getString();

                Kafka.consumer(broker, groupId, inputTopic, outputDir);

                Koordinator.sendStatus(task, "Finished", Koordinator.Status.Completed, Koordinator.ErrorLevel.None);
           }

           Thread.sleep(5000);
        }
    }

    public static void producer() throws Exception {
        while (true) {
           JsonObject task = Koordinator.retrieveTask("Meetup", "UrlsProducer");

           if (task != null) {
                String broker = ((JsonString)((JsonObject)task.get("inputData")).get("broker")).getString();
                String clientId = ((JsonString)((JsonObject)task.get("inputData")).get("clientId")).getString();
                String outputTopic = ((JsonString)((JsonObject)task.get("inputData")).get("output-topic")).getString();
                String inputFile = ((JsonString)((JsonObject)task.get("inputData")).get("input-file")).getString();
                int limit = Integer.parseInt(((JsonString)((JsonObject)task.get("inputData")).get("limit")).getString());

                Kafka.producer(broker, clientId, outputTopic, inputFile, limit);

                Koordinator.sendStatus(task, "Finished", Koordinator.Status.Completed, Koordinator.ErrorLevel.None);
           }

           Thread.sleep(5000);
        }
    }

}
