import com.fastcgi.FCGIInterface;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.io.InputStreamReader;
public class Server {
    public static void main (String[] args) {
        FCGIInterface fcgiInterface = new FCGIInterface();
        while(fcgiInterface.FCGIaccept() >= 0) {
            try {
                String method = FCGIInterface.request.params.getProperty("REQUEST_METHOD");

                if (!"POST".equals(method)) {
                    System.out.println("Content-Type: application/json\n");
                    System.out.println("{\"error\":\"Server only accepts POST requests\"}");
                    System.out.flush();
                    continue;
                }

                long startTime = System.nanoTime();
                String contentLengthStr = System.getProperty("CONTENT_LENGTH");
                int contentLength = contentLengthStr != null ? Integer.parseInt(contentLengthStr) : 0;

                char[] buffer = new char[contentLength];
                InputStreamReader reader = new InputStreamReader(System.in);
                int bytesRead = reader.read(buffer, 0, contentLength);
                String requestBody = new String(buffer, 0, bytesRead);

                if (requestBody.isEmpty()) {
                    System.out.println("Content-Type: application/json\n");
                    System.out.println("{\"error\":\"Request body is empty\"}");
                    System.out.flush();
                    continue;
                }

                Map<String, String> params = parseUrlEncoded(requestBody);

                String rStr = params.get("R");
                String xStr = params.get("X");
                String yStr = params.get("Y");

                if (rStr == null || xStr == null || yStr == null) {
                    System.out.println("Content-Type: application/json\n");
                    System.out.println("{\"error\":\"Missing required parameters\"}");
                    System.out.flush();
                    continue;
                }

                boolean isHit = checkHit(rStr, xStr, yStr);
                String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy"));

                long endTime = System.nanoTime();
                long duration = (endTime - startTime);

                String jsonResponse = String.format(
                        "{\n  \"r\":%s,\n  \"x\":%s,\n  \"y\":%s,\n  \"hit\":%b,\n  \"currentTime\":\"%s\",\n  \"executionTime\":%d\n}",
                        rStr, xStr, yStr, isHit, currentTime, duration
                );

                System.out.println("Content-Type: application/json\n");
                System.out.println(jsonResponse);
                System.out.flush();

            } catch (Exception e) {
                System.out.println("Content-Type: application/json\n");
                System.out.println("{\"error\":\"" + e.getMessage() + "\"}");
                System.out.flush();
            }
        }
    }

    private static Map<String, String> parseUrlEncoded(String body) {
        Map<String, String> params = new HashMap<>();
        if (body == null || body.isEmpty()) return params;

        for (String pair : body.split("&")) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                params.put(keyValue[0], keyValue[1]);
            }
        }
        return params;
    }

    private static boolean checkHit(String rStr, String xStr, String yStr) {
        double r = Double.parseDouble(rStr);
        int x = Integer.parseInt(xStr);
        double y = Double.parseDouble(yStr);

        boolean inRectangle = (x >= -r && x <= 0) && (y >= 0 && y <= r);
        boolean inCircle = (x <= 0 && y <= 0) && (x * x + y * y <= (r/2)*(r/2));
        boolean inTriangle = (x >= 0 && y <= 0) && (y >= 2 * x - r);

        return inRectangle || inCircle || inTriangle;
    }
}