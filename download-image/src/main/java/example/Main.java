package example;
import java.io.*;
import java.nio.file.*;
import javax.net.ssl.*;
import java.net.*;
import java.security.*;
import java.security.cert.*;

public class Main {

    public static void main(String[] args) throws Exception {
        System.out.printf("Downloading %s...\n", args[0]);
        downloadImage(args[0], "output");
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


