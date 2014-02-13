package com.tatuas.android.httprequest;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import org.apache.http.NameValuePair;

import android.os.Build;

public class Post {
    private HttpURLConnection conn;
    private HttpsURLConnection sConn;
    private String boundary;
    private String urlStr;
    private List<NameValuePair> postDatas;
    private HashMap<String, String> headerMaps;
    private List<PostFile> uploadFiles;

    private boolean isCustomHeader;
    private boolean isUploadFiles;

    private int readTimeOut;
    private int connectTimeOut;

    private StringBuffer mainBoundary;

    public Post(String urlStr, List<NameValuePair> postDatas) {
        this.urlStr = urlStr;
        this.postDatas = postDatas;
        init();
        setBoundary();
    }

    private void init() {
        this.mainBoundary = new StringBuffer();

        // if you do not set timeout then set 0.
        this.readTimeOut = 0;
        this.connectTimeOut = 0;

        this.isCustomHeader = false;
        this.isUploadFiles = false;
    }

    private void setBoundary() {
        this.boundary = Long.toHexString(System.currentTimeMillis());
    }

    public void addRequestHeader(HashMap<String, String> headersMap) {
        this.isCustomHeader = true;
        this.headerMaps = headersMap;
    }

    public void setUploadFiles(List<PostFile> uploadFiles) {
        this.isUploadFiles = true;
        this.uploadFiles = uploadFiles;
    }

    public void setReadTimeOut(int millTime) {
        this.readTimeOut = millTime;
    }

    public void setConnectTimeOut(int millTime) {
        this.connectTimeOut = millTime;
    }

    public void close() {
        if (conn != null) {
            conn.disconnect();
        }
        if (sConn != null) {
            sConn.disconnect();
        }
    }

    private TrustManager[] getTrustManager() {

        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[] {};
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] chain,
                            String authType) throws CertificateException {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain,
                            String authType) throws CertificateException {
                        // TODO Auto-generated method stub
                    }
                }
        };

        return trustAllCerts;
    }

    public PostResult execOnSSL(final BasicAuth auth) {
        try {
            URL url = getURL();
            sConn = (HttpsURLConnection) url.openConnection();
            sConn.setRequestProperty("Content-Type", new StringBuffer(
                    "multipart/form-data; boundary=").append(boundary)
                    .toString());
        } catch (Exception e) {
            return new PostResult("", 0, PostResult.Error.URL_FAILED,
                    e.toString());
        }

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.FROYO) {
                sc.init(null, getTrustManager(), new java.security.SecureRandom());
            } else {
                sc.init(null, null, new java.security.SecureRandom());
            }
            sConn.setSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            return new PostResult("", 0, PostResult.Error.SSL_FAILED,
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
            return new PostResult("", 0, PostResult.Error.NETWORK_DISABLE,
                    e.toString());
        }

        String resultData = "";
        int resultCode = 0;
        InputStream is = null;
        try {
            OutputStream os = sConn.getOutputStream();
            byte[] startBoundary = new StringBuffer("--").append(boundary)
                    .append("\r\n").toString().getBytes();
            byte[] endBoundary = new StringBuffer("--").append(boundary)
                    .append("\r\n").toString().getBytes();

            os.write(startBoundary);
            os.write(getPostDatasBytes());
            if (isUploadFiles) {
                os = (getUploadFilesBytes(os));
            }
            os.write(endBoundary);
            os.close();

            is = sConn.getInputStream();
            resultCode = sConn.getResponseCode();
        } catch (Exception e) {
            if (sConn != null) {
                sConn.disconnect();
            }
            return new PostResult(resultData, resultCode,
                    PostResult.Error.REQUEST_DATA_ERROR, e.toString());
        }

        try {
            resultData = convertToString(is);
            return new PostResult(resultData, resultCode, null,
                    null);
        } catch (Exception e) {
            return new PostResult(resultData, resultCode,
                    PostResult.Error.RESULT_DATA_ERROR,
                    null);
        } finally {
            if (sConn != null) {
                sConn.disconnect();
            }
        }
    }

    public PostResult exec(final BasicAuth auth) {
        try {
            URL url = getURL();
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", new StringBuffer(
                    "multipart/form-data; boundary=").append(boundary)
                    .toString());
        } catch (Exception e) {
            return new PostResult("", 0, PostResult.Error.URL_FAILED,
                    e.toString());
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

        conn.setDoOutput(true);
        conn.setConnectTimeout(connectTimeOut);
        conn.setReadTimeout(readTimeOut);
        conn.setUseCaches(false);

        try {
            conn.connect();
        } catch (Exception e) {
            return new PostResult("", 0, PostResult.Error.NETWORK_DISABLE,
                    e.toString());
        }

        String resultData = "";
        int resultCode = 0;
        InputStream is = null;
        try {
            OutputStream os = conn.getOutputStream();
            byte[] startBoundary = new StringBuffer("--").append(boundary)
                    .append("\r\n").toString().getBytes();
            byte[] endBoundary = new StringBuffer("--").append(boundary)
                    .append("\r\n").toString().getBytes();

            os.write(startBoundary);
            os.write(getPostDatasBytes());
            if (isUploadFiles) {
                os = (getUploadFilesBytes(os));
            }
            os.write(endBoundary);
            os.close();

            is = conn.getInputStream();
            resultCode = conn.getResponseCode();
        } catch (Exception e) {
            if (conn != null) {
                conn.disconnect();
            }
            return new PostResult(resultData, resultCode,
                    PostResult.Error.REQUEST_DATA_ERROR, e.toString());
        }

        try {
            resultData = convertToString(is);
            return new PostResult(resultData, resultCode, null,
                    null);
        } catch (Exception e) {
            return new PostResult(resultData, resultCode,
                    PostResult.Error.RESULT_DATA_ERROR,
                    null);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private byte[] getFileBytes(File file) throws Exception {
        byte[] b = new byte[10];
        FileInputStream fis = null;
        ByteArrayOutputStream bo = new ByteArrayOutputStream();

        if (file == null) {
            throw new Exception("File is null.");
        }
        if (!file.exists()) {
            throw new Exception("File is not exists.");
        }
        if (!file.canRead()) {
            throw new Exception("File can not read.");
        }

        try {
            fis = new FileInputStream(file);
            while (fis.read(b) > 0) {
                bo.write(b);
            }
            bo.close();
            fis.close();
        } catch (Error e) {
            throw new Exception(e.toString() + " : " + e.getMessage()
                    + " Out of memory.");
        }

        return bo.toByteArray();
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

    private OutputStream getUploadFilesBytes(OutputStream os) throws Exception {
        if (uploadFiles == null) {
            throw new Exception("UploadFilesMap is null.");
        }

        String inputTagFieldName = "";
        String mimeType = "";
        File f;

        for (PostFile element : uploadFiles) {
            mimeType = element.getMimeType();
            inputTagFieldName = element.getTagName();
            f = element.getUploadFile();

            try {
                StringBuffer buff = new StringBuffer();
                buff.append("Content-Disposition: form-data; name=\"")
                        .append(inputTagFieldName).append("\"; filename=\"")
                        .append(f.getName()).append("\"\r\n")
                        .append("Content-Type: ").append(mimeType)
                        .append("\r\n").append("\r\n");

                os.write(buff.toString().getBytes());
                os.write(getFileBytes(f));

                StringBuffer buff2 = new StringBuffer();
                buff2.append("\r\n").append("\r\n").append("--")
                        .append(boundary).append("\r\n");

                os.write(buff2.toString().getBytes());
            } catch (Error e) {
                throw new Exception(e.toString() + " : " + e.getMessage()
                        + " Out of memory.");
            }
        }

        return os;
    }

    private byte[] getPostDatasBytes() throws Exception {
        if (postDatas == null) {
            throw new Exception("PostDataList is null.");
        }

        for (NameValuePair nv : postDatas) {
            mainBoundary.append("Content-Disposition: form-data; name=\"")
                    .append(nv.getName()).append("\"\r\n").append("\r\n")
                    .append(nv.getValue()).append("\r\n").append("--")
                    .append(boundary).append("\r\n");
        }

        return mainBoundary.toString().getBytes();
    }

    private URL getURL() throws MalformedURLException {
        return new URL(urlStr);
    }

    /*
     * // 認証局の追加 private TrustManagerFactory createTrustManagerFactory() throws
     * CertificateException, IOException, KeyStoreException,
     * NoSuchAlgorithmException { CertificateFactory cf =
     * CertificateFactory.getInstance("X.509"); // From
     * https://www.washington.edu/itconnect/security/ca/load-der.crt InputStream
     * caInput = new BufferedInputStream(new FileInputStream(
     * "/storage/emulated/0/ca-bundle.crt")); Certificate ca; try { ca =
     * cf.generateCertificate(caInput); Log.e("washinton", "ca=" +
     * ((X509Certificate) ca).getSubjectDN()); } finally { caInput.close(); }
     * 
     * // Create a KeyStore containing our trusted CAs String keyStoreType =
     * KeyStore.getDefaultType(); KeyStore keyStore =
     * KeyStore.getInstance(keyStoreType); keyStore.load(null, null);
     * keyStore.setCertificateEntry("ca", ca);
     * 
     * // Create a TrustManager that trusts the CAs in our KeyStore String
     * tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
     * TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
     * tmf.init(keyStore);
     * 
     * return tmf; }
     */
}
