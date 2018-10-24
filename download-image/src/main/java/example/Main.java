package example;

public class Main {
    public static void main(String[] args) throws Exception {
        if ("producer".equals(args[0])) {
            Kafka.producer(args[1], args[2], args[3], args[4], Integer.parseInt(args[5]));
        }
        if ("consumer".equals(args[0])) {
            Kafka.consumer(args[1], args[2], args[3], args[4]);
        }
        if ("producerWorker".equals(args[0])) {
            Worker.producer();
        }
        if ("consumerWorker".equals(args[0])) {
            Worker.consumer();
        }
        if ("zipDownloader".equals(args[0])) {
            Utils.zipImages(args[1]);
        }
        if ("zipWorker".equals(args[0])) {
            Worker.zipImages();
        }
    }
}


