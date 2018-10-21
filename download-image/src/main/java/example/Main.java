package example;
import java.io.*;
import java.nio.file.*;
import javax.net.ssl.*;
import java.util.*;
import java.net.*;
import java.security.*;
import java.security.cert.*;
import javax.json.*;
import org.apache.kafka.common.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.*;

public class Main {
    private static final String KILL_MESSAGE = "KILL";

    public static void main(String[] args) throws Exception {
        if ("producer".equals(args[0])) {
            producer(args[1], args[2], args[3], args[4], Integer.parseInt(args[5]));
        }
        if ("consumer".equals(args[0])) {
            consumer(args[1], args[2], args[3], args[4]);
        }
        if ("producerWorker".equals(args[0])) {
            producerWorker();
        }
        if ("consumerWorker".equals(args[0])) {
            consumerWorker();
        }
    }

    private static void consumerWorker() throws Exception {
        while (true) {
           JsonObject task = Koordinator.retrieveTask("Meetup", "UrlsConsumer");

           if (task != null) {
                String broker = ((JsonString)((JsonObject)task.get("inputData")).get("broker")).getString();
                String groupId = ((JsonString)((JsonObject)task.get("inputData")).get("groupId")).getString();
                String inputTopic = ((JsonString)((JsonObject)task.get("inputData")).get("input-topic")).getString();
                String outputDir = ((JsonString)((JsonObject)task.get("inputData")).get("output-dir")).getString();

                consumer(broker, groupId, inputTopic, outputDir);

                Koordinator.sendStatus(task, "Finished", Koordinator.Status.Completed, Koordinator.ErrorLevel.None);
           }

           Thread.sleep(5000);
        }
    }

    private static void producerWorker() throws Exception {
        while (true) {
           JsonObject task = Koordinator.retrieveTask("Meetup", "UrlsProducer");

           if (task != null) {
                String broker = ((JsonString)((JsonObject)task.get("inputData")).get("broker")).getString();
                String clientId = ((JsonString)((JsonObject)task.get("inputData")).get("clientId")).getString();
                String outputTopic = ((JsonString)((JsonObject)task.get("inputData")).get("output-topic")).getString();
                String inputFile = ((JsonString)((JsonObject)task.get("inputData")).get("input-file")).getString();
                int limit = Integer.parseInt(((JsonString)((JsonObject)task.get("inputData")).get("limit")).getString());

                producer(broker, clientId, outputTopic, inputFile, limit);

                Koordinator.sendStatus(task, "Finished", Koordinator.Status.Completed, Koordinator.ErrorLevel.None);
           }

           Thread.sleep(5000);
        }
    }

    private static void consumer(String broker, String groupId, String topicToRead, String outputDir) throws Exception {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, broker+":9092");
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, UUID.randomUUID().toString());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        consumer.subscribe(Arrays.asList(topicToRead)); 

        try {
            boolean stopPolling = false;
            while (!stopPolling) {
                ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);
                int count = 0;
                for (ConsumerRecord<String, String> record : records) {
                    String url = record.value();
                    Integer partition = record.partition();
                    System.out.println(partition + " - " + record.offset() + ": " + url);

                    if (KILL_MESSAGE.equals(record.key())) {
                        stopPolling = true;
                        break;
                    }
                    try {
                        downloadImage(url, outputDir + "/image-" + partition + "-" + count);
                        count++;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } finally {
          consumer.close();
        }

    }

    private static void producer(String broker, String clientId, String topicToWrite, String urlList, int limit) throws Exception {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, broker+":9092");
        props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        BufferedReader br = new BufferedReader(new FileReader(urlList));

        while(limit>0) {
            String line = br.readLine();
            if (line == null) break;

            // no key, it will be distributed in round robin through partitions
            System.out.printf("Url: %s\n", line);
            producer.send(new ProducerRecord<>(topicToWrite, line.trim()));
            limit--;
        }

        for(PartitionInfo partition : producer.partitionsFor(topicToWrite)) {
            System.out.printf("Sending KILL message to partition: %d\n", partition.partition());
            producer.send(new ProducerRecord<>(
                        topicToWrite,
                        partition.partition(),
                        KILL_MESSAGE, 
                        KILL_MESSAGE));
        }
            

        br.close();
        producer.close();
    }

    private static void downloadImage(String url, String outputFilename) throws Exception {
        URLConnection connection = new URL(url).openConnection();

        connection.setConnectionTimeout(3000);
        connection.setReadTimeout(3000);
        connection.connect();

        String contentType = connection.getContentType();
        System.out.printf("Content type %s...\n", contentType);

        InputStream in = connection.getInputStream();
        Files.copy(in, Paths.get(outputFilename + getExtensionFromContentType(contentType)), StandardCopyOption.REPLACE_EXISTING);
        in.close();
    }

    private static String getExtensionFromContentType(String contentType) {
        if ("image/jpeg".equals(contentType)) {
            return ".jpg";
        }
        if ("image/png".equals(contentType)) {
            return ".png";
        }
        return "";
    }

    static {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        X509Certificate[] myTrustedAnchors = new X509Certificate[0];
                        return myTrustedAnchors;
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
                };

                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String arg0, SSLSession arg1) {
                        return true;
                    }
                });
            } catch (Exception e) {
            }
    }

}


