import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.concurrent.ConcurrentHashMap;

public class Main {
    private static final int NUMTHREADS = 128;
    private static ConcurrentHashMap<Integer, util.LiftInfo> liftInfos;

    public static void main(String[] args) {
        liftInfos = new ConcurrentHashMap<>();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("34.223.239.186");
        factory.setUsername("guest");
        factory.setPassword("guest");
        try {
            Connection newConnection = factory.newConnection();
            ConsumerThread[] consumers = new ConsumerThread[NUMTHREADS];
            for (int i = 0; i < NUMTHREADS; i++) {
                consumers[i] = new ConsumerThread(liftInfos, newConnection);
            }

            Thread[] threads = new Thread[NUMTHREADS];
            for (int i = 0; i < NUMTHREADS; i++) {
                threads[i] = new Thread(consumers[i]);
                threads[i].start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
