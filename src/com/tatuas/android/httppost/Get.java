package com.tatuas.android.httppost;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

public class Get {
    private String urlStr;
    private HttpsURLConnection sConn;
    private HttpURLConnection conn;
    private int readTimeOut;
    private int connectTimeOut;
    private boolean isCustomHeader;
    private HashMap<String, String> headerMaps;

    public Get(String urlStr) {
        this.urlStr = urlStr;
        init();
    }

    private void init() {
        // if you do not set timeout then set 0.
        this.readTimeOut = 0;
        this.connectTimeOut = 0;

        this.isCustomHeader = false;
    }

    public void close() {
        if (conn != null) {
            conn.disconnect();
        }
        if (sConn != null) {
            sConn.disconnect();
        }
    }

    public void addRequestHeader(HashMap<String, String> headersMap) {
        this.isCustomHeader = true;
        this.headerMaps = headersMap;
    }

    public void setReadTimeOut(int millTime) {
        this.readTimeOut = millTime;
    }

    public void setConnectTimeOut(int millTime) {
        this.connectTimeOut = millTime;
    }

    public GetResult execOnSSL(final BasicAuth auth) {
        try {
            URL url = getURL();
            sConn = (HttpsURLConnection) url.openConnection();
        } catch (Exception e) {
            return new GetResult("", 0, GetResult.Error.URL_FAILED,
                    e.toString());
        }

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, null, new java.security.SecureRandom());
            sConn.setSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            return new GetResult("", 0, GetResult.Error.SSL_FAILED,
                    e.toString());
        }

        if (isCustomHeader) {
            Iterator<String> it = headerMaps.keySet().iterator();
            while (it.hasNext()) {
                String o = it.next();
                sConn.setRequestProperty(o, headerMaps.get(o));
            }
        }

        if (auth != null) {
            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(auth.getUserName(), auth
                            .getPassWord().toCharArray());
                }
            });
        }

        sConn.setDoOutput(true);
        sConn.setConnectTimeout(connectTimeOut);
        sConn.setReadTimeout(readTimeOut);
        sConn.setUseCaches(false);

        try {
            sConn.connect();
        } catch (Exception e) {
            return new GetResult("", 0, GetResult.Error.NETWORK_DISABLE,
                    e.toString());
        }

        String resultData = "";
        int resultCode = 0;
        InputStream is = null;

        try {
            is = sConn.getInputStream();
            resultCode = sConn.getResponseCode();
        } catch (Exception e) {
            if (sConn != null) {
                sConn.disconnect();
            }
            return new GetResult(resultData, resultCode,
                    GetResult.Error.RESULT_DATA_ERROR, e.toString());
        }

        try {
            if (is != null) {
                resultData = convertToString(is);
            }
            return new GetResult(resultData, resultCode, null, null);
        } catch (Exception e) {
            return new GetResult(resultData, resultCode,
                    GetResult.Error.RESULT_DATA_ERROR, null);
        } finally {
            if (sConn != null) {
                sConn.disconnect();
            }
        }
    }


    public GetResult exec(final BasicAuth auth) {
        try {
            URL url = getURL();
            conn = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            return new GetResult("", 0, GetResult.Error.URL_FAILED, null);
        }

        if (isCustomHeader) {
            Iterator<String> it = headerMaps.keySet().iterator();
            while (it.hasNext()) {
                String o = it.next();
                conn.setRequestProperty(o, headerMaps.get(o));
            }
        }

        if (auth != null) {
            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(auth.getUserName(), auth
                            .getPassWord().toCharArray());
                }
            });
        }

        conn.setConnectTimeout(connectTimeOut);
        conn.setReadTimeout(readTimeOut);
        conn.setUseCaches(false);

        try {
            conn.connect();
        } catch (Exception e) {
            return new GetResult("", 0, GetResult.Error.NETWORK_DISABLE,
                    e.toString());
        }

        String resultData = "";
        int resultCode = 0;
        InputStream is = null;

        try {
            is = conn.getInputStream();
            resultCode = conn.getResponseCode();
        } catch (Exception e) {
            if (conn != null) {
                conn.disconnect();
            }

            return new GetResult(resultData, resultCode,
                    GetResult.Error.REQUEST_ERROR, e.toString());
        }

        try {
            if (is != null) {
                resultData = convertToString(is);
            }
            return new GetResult(resultData, resultCode, null, null);
        } catch (Exception e) {
            return new GetResult(resultData, resultCode,
                    GetResult.Error.RESULT_DATA_ERROR, null);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String convertToString(InputStream stream) throws Exception {
        InputStreamReader streamReader = null;
        BufferedReader bufferReader = null;
        streamReader = new InputStreamReader(stream, "UTF-8");
        bufferReader = new BufferedReader(streamReader);
        StringBuilder builder = new StringBuilder();
        for (String line = null; (line = bufferReader.readLine()) != null;) {
            builder.append(line).append("\n");
        }
        stream.close();
        if (bufferReader != null) {
            bufferReader.close();
        }
        return builder.toString();
    }

    private URL getURL() throws MalformedURLException {
        return new URL(urlStr);
    }
}
