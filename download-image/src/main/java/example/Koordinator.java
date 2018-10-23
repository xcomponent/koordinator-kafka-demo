package example;

import javax.json.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.*;
import java.security.cert.*;
import java.security.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Koordinator {
    public static enum ErrorLevel {
        None, Minor, Critical, Fatal 
    }

    public static enum Status {
        InProgress, Error, Completed 
    }

    public static String Token;
    public static String TaskStatusServiceUrl;
    public static String TaskQueueServiceUrl;
    public static String UploadServiceUrl;

    static {
        Token = System.getenv().get("WORKER_TOKEN");
        TaskStatusServiceUrl = System.getenv().get("TASK_STATUS_URL") + "/api/taskStatus";
        TaskQueueServiceUrl = System.getenv().get("TASK_POLLING_URL");
        UploadServiceUrl = System.getenv().get("UPLOAD_URL");
    }

    public static boolean isTaskCancelled(String taskNamespace, String taskInstanceId) { 
        try {
            URL url = new URL(String.format("%s/api/Cancelled?catalogTaskDefinitionNamespace=%s&taskInstanceId=%s", TaskQueueServiceUrl, taskNamespace, taskInstanceId));

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "bearer " + Token);

            System.out.printf("Response code: %d\n", connection.getResponseCode());
            if (connection.getResponseCode() == 200) {
                JsonObject cancelledObject = Json.createReader(connection.getInputStream()).readObject();
                return "true".equals(cancelledObject.get("isCanceled"));
            }
            return false;
        } catch(Exception e) {
            return false;
        }
    }

    public static JsonObject retrieveTask(String taskNamespace, String taskName) { 
        try {
            URL url = new URL(String.format("%s/api/poll?catalogTaskDefinitionNamespace=%s&catalogTaskDefinitionName=%s", TaskQueueServiceUrl, taskNamespace, taskName));

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "bearer " + Token);

            if (connection.getResponseCode() == 200) {
                return Json.createReader(connection.getInputStream()).readObject();
            }
            return null;
        } catch(Exception e) {
            return null;
        }
    }

    public static JsonObject uploadFile(String filePath) throws IOException {
        File originalFile = new File(filePath);
        byte[] encodedBase64 = null;
        try {
            FileInputStream fileInputStreamReader = new FileInputStream(originalFile);
            byte[] bytes = new byte[(int)originalFile.length()];
            fileInputStreamReader.read(bytes);
            encodedBase64 = bytes;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        URL url = new URL(String.format("%s/api/Upload", UploadServiceUrl));

        System.out.println("Uploading to: "+ url + "...");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "bearer " + Token);
        //connection.setFixedLengthStreamingMode(encodedBase64.length);
        connection.setRequestProperty("Content-Length", ""+encodedBase64.length);
        System.out.println("Content-length: " + encodedBase64.length + "...");
        connection.setRequestProperty("Content-Type", "application/octet-stream");
        //connection.setAllowUserInteraction(true);
        connection.setDoOutput(true);

        //connection.connect();

        OutputStream os = new BufferedOutputStream(connection.getOutputStream());
        os.write(encodedBase64);
        //os.flush();
        //os.close();

        String output = "";

        BufferedReader reader = null;
        InputStream is = null;
        JsonObject ret = null;
        try {
            int responseCode = connection.getResponseCode();
            System.out.println("Response code: "+ responseCode+ "...");
            System.out.println("Response message: "+ connection.getResponseMessage()+ "...");
            if (responseCode == 200) {
                ret = Json.createReader(connection.getInputStream()).readObject();
            } else {
                is = connection.getErrorStream();
            }
        } catch(IOException e) {
            is = connection.getErrorStream();
        }

        if (ret == null) {
            reader =new BufferedReader(new InputStreamReader(is));
            while(true) {
                String line = reader.readLine();
                if (line == null) break;
                output += "\n" + line;
            }
            System.out.println("Response: " + output);
            reader.close();
            return null;
        }
        return ret;
    }

    public static void sendStatus(JsonObject taskInstance, String message, Status status, ErrorLevel errorLevel, JsonObject outputs) {
        try {
            URL url = new URL(TaskStatusServiceUrl);
            JsonObjectBuilder taskStatusBuilder = Json
                    .createObjectBuilder()
                    .add("message", message)
                    .add("status", status.name())
                    .add("errorLevel", errorLevel.name())
                    .add("taskInstanceId", taskInstance.get("id"));

            if (outputs != null) {
                taskStatusBuilder = taskStatusBuilder.add("outputValues", outputs);
            }

            sendJsonObject(url, taskStatusBuilder.build());
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendJsonObject(URL url, JsonObject json) throws IOException {
        System.out.printf("Connecting to %s...\n", url.toString());

        String message = json.toString();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "bearer " + Token);
        connection.setFixedLengthStreamingMode(message.getBytes().length);
        connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
        connection.setDoOutput(true);

        connection.connect();

        OutputStream os = new BufferedOutputStream(connection.getOutputStream());
        os.write(message.getBytes());
        os.flush();
        os.close();
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



