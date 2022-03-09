import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeoutException;

public class Main {
    private static int NUMTHREADS = 256;
    private static ConcurrentLinkedQueue<util.LiftInfo> liftInfos;

    public static void main(String[] args){
        liftInfos = new ConcurrentLinkedQueue<>();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("34.223.239.186");
        factory.setUsername("guest");
        factory.setPassword("guest");
        try{
            Connection newConnection = factory.newConnection();
            ConsumerThread[] consumers = new ConsumerThread[NUMTHREADS];
            for (int i = 0; i < NUMTHREADS; i++)
            {consumers[i] = new ConsumerThread(liftInfos,newConnection);
            }

            Thread threads[] = new Thread[NUMTHREADS];
            for (int i=0; i < NUMTHREADS ; i++ )
            {threads[i] = new Thread (consumers[i]);
                threads[i].start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
