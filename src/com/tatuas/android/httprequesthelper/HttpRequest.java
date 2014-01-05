package com.tatuas.android.httprequesthelper;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;

public class HttpRequest {
    private HttpURLConnection conn;
    private String boundary;
    private String urlStr;
    private List<NameValuePair> postDatas = null;
    private HashMap<String, String> headerMaps = null;
    private List<UploadFile> uploadFiles = null;

    private boolean isCustomHeader = false;
    private boolean isUploadFiles = false;

    // if you unset timeout then set 0.
    private int readTimeOut = 0;
    private int connectTimeOut = 0;

    private StringBuffer mainBoundary = new StringBuffer();
    private WeakReference<HttpRequestEventListener> httpListener;
    private WeakReference<HttpBasicAuthEventListener> baListener;

    public HttpRequest(String urlStr, List<NameValuePair> postDatas) {
        this.urlStr = urlStr;
        this.postDatas = postDatas;
        setBoundary();
    }

    private void setBoundary() {
        boundary = "--" + Long.toHexString(System.currentTimeMillis());
    }

    public void setOnHttpRequestListener(HttpRequestEventListener listener) {
        this.httpListener = new WeakReference<HttpRequestEventListener>(
                listener);
    }

    public void setOnBasicAuthListener(HttpBasicAuthEventListener listener) {
        this.baListener = new WeakReference<HttpBasicAuthEventListener>(
                listener);
    }

    public void addRequestHeader(HashMap<String, String> headersMap) {
        this.isCustomHeader = true;
        this.headerMaps = headersMap;
    }

    public void setUploadFiles(List<UploadFile> uploadFiles) {
        this.isUploadFiles = true;
        this.uploadFiles = uploadFiles;
    }

    public void setReadTimeOut(int millTime) {
        readTimeOut = millTime;
    }

    public void setConnectTimeOut(int millTime) {
        connectTimeOut = millTime;
    }

    private void exportLog(Exception e) {
        StackTraceElement[] ee = e.getStackTrace();
        for (int i = 0; i < ee.length; i++) {
        }
    }

    private String exportExceptionString(Exception e) {
        String msg = "";
        StackTraceElement[] ee = e.getStackTrace();
        for (int i = 0; i < ee.length; i++) {
            msg += ee[i].toString();
        }
        return msg;
    }

    private String exportErrorString(Error e) {
        String msg = "";
        StackTraceElement[] ee = e.getStackTrace();
        for (int i = 0; i < ee.length; i++) {
            msg += ee[i].toString();
        }
        return msg;
    }

    public void execRequest() {
        try {
            startPostRequest(false, false, null);
        } catch (Exception e) {
            exportLog(e);
        }
    }

    public void execRequest(BasicAuth auth) {
        try {
            startPostRequest(false, true, auth);
        } catch (Exception e) {
            exportLog(e);
        }
    }

    public void execSSLRequest() {
        try {
            startPostRequest(true, false, null);
        } catch (Exception e) {
            exportLog(e);
        }
    }

    public void execSSLRequest(BasicAuth auth) {
        try {
            startPostRequest(true, true, auth);
        } catch (Exception e) {
            exportLog(e);
        }
    }

    private void startPostRequest(boolean useSSL, boolean useBasicAuth,
            final BasicAuth auth) {
        String resultData = "";
        URL url = null;
        SSLContext sc;

        if (useBasicAuth && auth == null) {
            if (baListener != null) {
                baListener.get().onBasicAuthError();
                return;
            }
        }

        try {
            url = new URL(urlStr);
            if (useSSL) {
                conn = (HttpsURLConnection) url.openConnection();
                sc = SSLContext.getInstance("TLS");
                sc.init(null, null, new java.security.SecureRandom());
                ((HttpsURLConnection) conn).setSSLSocketFactory(sc
                        .getSocketFactory());
            } else {
                conn = (HttpURLConnection) url.openConnection();
            }
        } catch (Exception e) {
            this.httpListener.get().onRequestDataError(e.getMessage());
            return;
        }

        if (isCustomHeader) {
            Iterator<String> it = headerMaps.keySet().iterator();
            while (it.hasNext()) {
                String o = it.next();
                conn.setRequestProperty(o, headerMaps.get(o));
            }
        }

        if (useBasicAuth) {
            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(auth.getUserName(), auth
                            .getPassWord().toCharArray());
                }
            });
        }

        conn.setRequestProperty("Content-Type", new StringBuffer(
                "multipart/form-data; boundary=").append(boundary).toString());
        try {
            conn.setRequestMethod("POST");
        } catch (Exception e) {
            return;
        }

        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setConnectTimeout(connectTimeOut);
        conn.setReadTimeout(readTimeOut);
        conn.setUseCaches(false);

        try {
            conn.connect();
        } catch (Exception e) {
            if (httpListener != null) {
                httpListener.get().onNetworkDisable();
                return;
            }
        }

        try {
            OutputStream os = conn.getOutputStream();
            byte[] startBoundary = new StringBuffer("--").append(boundary)
                    .append("\r\n").toString().getBytes();
            byte[] endBoundary = new StringBuffer().append("--")
                    .append(boundary)
                    .append("\r\n").toString().getBytes();

            os.write(startBoundary);
            os.write(getPostDatasBytes());
            if (isUploadFiles) {
                os = (getUploadFilesBytes(os));
            }
            os.write(endBoundary);
            os.close();

            if (conn.getResponseCode() == HttpStatus.SC_OK) {
                InputStream is = conn.getInputStream();
                resultData = convertToString(is);
                httpListener.get().onRequestSuccess(resultData);
            } else {
                httpListener.get().onRequestError(conn.getResponseCode());
                return;
            }
        } catch (Exception e) {
            this.httpListener.get().onRequestDataError(e.getMessage());
            return;
        }

        if (conn != null) {
            conn.disconnect();
        }
    }

    private byte[] getFileBytes(File file) throws HttpRequestException {
        byte[] b = new byte[10];
        FileInputStream fis = null;
        ByteArrayOutputStream bo = new ByteArrayOutputStream();

        if (file == null) {
            throw new HttpRequestException("File is null.");
        }
        if (!file.exists()) {
            throw new HttpRequestException("File is not exists.");
        }
        if (!file.canRead()) {
            throw new HttpRequestException("File can not read.");
        }

        try {
            fis = new FileInputStream(file);
            while (fis.read(b) > 0) {
                bo.write(b);
            }
            bo.close();
            fis.close();
        } catch (Exception e) {
            throw new HttpRequestException(exportExceptionString(e));
        } catch (Error e) {
            throw new HttpRequestException("out of memory");
        }
        return bo.toByteArray();
    }

    private String convertToString(InputStream stream)
            throws HttpRequestException {
        InputStreamReader streamReader = null;
        BufferedReader bufferReader = null;
        try {
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
        } catch (Exception e) {
            throw new HttpRequestException(e.getMessage());
        }
    }

    private OutputStream getUploadFilesBytes(OutputStream os)
            throws HttpRequestException {
        if (uploadFiles == null) {
            throw new HttpRequestException("uploadFilesMap is null.");
        }

        String inputTagFieldName = "";
        String mimeType = "";
        File f;

        for (UploadFile element : uploadFiles) {
            mimeType = element.getMimeType();
            inputTagFieldName = element.getTagName();
            f = element.getUploadFile();

            try {
                StringBuffer buff = new StringBuffer();
                buff.append("Content-Disposition: form-data; name=\"")
                        .append(inputTagFieldName).append("\"; filename=\"")
                        .append(f.getName()).append("\"\r\n")
                        .append("Content-Type: ").append(mimeType)
                        .append("\r\n\r\n");

                os.write(buff.toString().getBytes());
                os.write(getFileBytes(f));

                StringBuffer buff2 = new StringBuffer();
                buff2.append("--").append(boundary).append("\r\n");

                os.write(buff2.toString().getBytes());
            } catch (Exception e) {
                throw new HttpRequestException(exportExceptionString(e));
            } catch (Error e) {
                throw new HttpRequestException(exportErrorString(e));
            }
        }
        return os;
    }

    private byte[] getPostDatasBytes() throws HttpRequestException {
        if (postDatas != null) {
            for (NameValuePair nv : postDatas) {
                mainBoundary.append("Content-Disposition: form-data; name=\"")
                        .append(nv.getName()).append("\"\r\n").append("\r\n")
                        .append(nv.getValue()).append("\r\n").append("--")
                        .append(boundary).append("\r\n");
            }
        } else {
            throw new HttpRequestException("PostDataList is null.");
        }
        return mainBoundary.toString().getBytes();
    }

    public void close() {
        if (conn != null) {
            conn.disconnect();
        }
    }
}
