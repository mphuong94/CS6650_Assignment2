import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import util.ChannelFactory;
import util.LiftInfo;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

@WebServlet(name = "SkierServlet", value = "/SkierServlet")
public class SkierServlet extends HttpServlet {
    public static ConnectionFactory factory;
    public static ObjectPool<Channel> pool;
    public GsonBuilder builder;
    public Gson gson;
    private static String QUEUE_NAME = "postRequest";

    @Override
    public void init() {
        factory = new ConnectionFactory();
        factory.setHost("34.223.239.186");
        try {
            Connection newConn = factory.newConnection();
            GenericObjectPoolConfig<Channel> config = new GenericObjectPoolConfig<>();
            config.setMaxTotal(200);
            config.setMinIdle(100);
            config.setMaxIdle(200);
            pool = new GenericObjectPool<>(new ChannelFactory(newConn), config);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        builder = new GsonBuilder();
        builder.setPrettyPrinting();
        gson = builder.create();
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        String urlPath = request.getPathInfo();

        // check we have a URL!
        if (urlPath == null || urlPath.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("missing parameters");
            return;
        }

        String[] urlParts = urlPath.split("/");
        // and now validate url path and return the response status code
        // (and maybe also some value if input is valid)

        if (!isUrlValid(urlParts)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("It works!");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        String urlPath = request.getPathInfo();
//        logger.log(Level.INFO,urlPath);
        // check we have a URL!
        if (urlPath == null || urlPath.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("missing parameters");
            return;
        }

//        logger.log(Level.INFO, request.getParameter("skier_id"));
        int skierId = Integer.parseInt(request.getParameter("skier_id"));
        int liftId = Integer.parseInt(request.getParameter("lift_id"));
        int minute = Integer.parseInt(request.getParameter("minute"));
        int waitTime = Integer.parseInt(request.getParameter("wait"));
        LiftInfo newInfo = new LiftInfo(skierId,liftId,minute,waitTime);
        try {
            Channel channel = pool.borrowObject();
            String jsonString = gson.toJson(newInfo);
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            channel.basicPublish("", QUEUE_NAME, null, jsonString.getBytes("UTF-8"));
            pool.returnObject(channel);
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write("It works post!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isUrlValid(String[] urlPath) {
        // urlPath  = "/1/seasons/2019/day/1/skier/123"
        // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
        if (urlPath.length == 8){
            return true;
        }
        return false;
    }
}
