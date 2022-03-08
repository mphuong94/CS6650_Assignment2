import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;

@WebServlet(name = "SkierServlet", value = "/SkierServlet")
public class SkierServlet extends HttpServlet {
    final static Logger logger = Logger.getLogger(SkierServlet.class.getName());
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/plain");
        String urlPath = req.getPathInfo();

        // check we have a URL!
        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("missing parameters");
            return;
        }

        String[] urlParts = urlPath.split("/");
        // and now validate url path and return the response status code
        // (and maybe also some value if input is valid)

        if (!isUrlValid(urlParts)) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().write("It works!");
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


        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("34.223.239.186)");
        factory.setUsername("guest1");
        factory.setPassword("guest1");
        factory.setVirtualHost("/");
        factory.setPort(15672);

        try {
            Connection conn = factory.newConnection();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        int skierId = Integer.parseInt(request.getParameter("skier_id"));
        int liftId = Integer.parseInt(request.getParameter("lift_id"));
        int minute = Integer.parseInt(request.getParameter("minute"));
        int waitTime = Integer.parseInt(request.getParameter("wait"));

        LiftInfo newInfo = new LiftInfo(skierId,liftId,minute,waitTime);


        response.setStatus(HttpServletResponse.SC_CREATED);
        response.getWriter().write("It works post!");
        // body json
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

