package example;
import org.apache.kafka.common.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.*;
import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.net.*;

public class Kafka {
    private static final String KILL_MESSAGE = "KILL";

    public static void consumer(String broker, String groupId, String topicToRead, String outputDir) throws Exception {
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

    public static void producer(String broker, String clientId, String topicToWrite, String urlList, int limit) throws Exception {
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
}
