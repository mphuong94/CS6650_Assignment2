import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import util.LiftInfo;

public class ConsumerThread implements Runnable {
    private final ConcurrentLinkedQueue<LiftInfo> liftInfos;
    private final Connection connection;
    private static String QUEUE_NAME = "postRequest";
    private final GsonBuilder builder;
    private final Gson gson;

    public ConsumerThread(ConcurrentLinkedQueue<LiftInfo> liftInfos, Connection connection) {
        this.liftInfos = liftInfos;
        this.connection = connection;
        this.builder = new GsonBuilder();
        builder.setPrettyPrinting();
        this.gson = builder.create();
    }

    @Override
    public void run() {
        Channel channel = null;
        try {
            channel = connection.createChannel();
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
            channel.basicQos(1); // accept only one unack-ed message at a time
            Channel finalChannel = channel;
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println(" [x] Received '" + message + "'");
                try {
                    processMessage(message);
                } finally {
                    System.out.println(" [x] Done");
                    finalChannel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                }
            };
            boolean autoAck = false;
            channel.basicConsume(QUEUE_NAME, autoAck, deliverCallback, consumerTag -> { });
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    void processMessage(String message){
        LiftInfo receivedInfo = gson.fromJson(message, LiftInfo.class);
        this.liftInfos.add(receivedInfo);
    }

}
