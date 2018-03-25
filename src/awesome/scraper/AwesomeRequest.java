package awesome.scraper;

import jdk.incubator.http.HttpClient;
import jdk.incubator.http.HttpRequest;
import jdk.incubator.http.HttpResponse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AwesomeRequest {

    private HttpRequest.Builder requestBuilder = null;

    private final Map<String, String> cookies = new HashMap<>();
    private final Map<String, String> headers = new HashMap<>();

    private String uri;

    public AwesomeRequest(String uri) {
        this.uri = uri;
    }

    private void generateBuilder(String uri) {
        try {
            this.requestBuilder = HttpRequest.newBuilder().uri(new URI(uri));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public AwesomeRequest header(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    public AwesomeRequest headers(Map<String, String> headers) {
        this.headers.clear();
        this.headers.putAll(headers);
        return this;
    }

    public AwesomeRequest cookie(String key, String value) {
        this.cookies.put(key, value);
        return this;
    }

    public AwesomeRequest cookies(Map<String, String> cookies) {
        this.cookies.clear();
        this.cookies.putAll(cookies);
        return this;
    }

    public HttpResponse<String> get(HttpClient client) throws IOException, InterruptedException {
        this.generateBuilder(this.uri);
        return this.get(client, Collections.emptyMap());
    }

    public HttpResponse<String> get(HttpClient client, Map<String, String> data) throws IOException, InterruptedException {
        String formData = data.entrySet().parallelStream().map(AwesomeRequest::generateParams).reduce((""), (cks, current) -> cks + current + "&");
        if(formData.isEmpty()){
            this.generateBuilder(this.uri);
        }else{
            this.generateBuilder(this.uri + "?" + formData);
        }

        this.generateHeaders();
        return client.send(this.requestBuilder.GET().build(), HttpResponse.BodyHandler.asString());
    }

    public HttpResponse<String> post(HttpClient client, Map<String, String> data) throws IOException, InterruptedException {
        this.generateBuilder(this.uri);
        this.generateHeaders();
        String formData = data.entrySet().parallelStream().map(AwesomeRequest::generateParams).reduce((""), (cks, current) -> cks + current + "&");
        return client.send(this.requestBuilder.POST(HttpRequest.BodyProcessor.fromString(formData)).build(), HttpResponse.BodyHandler.asString());
    }

    private void generateHeaders() {
        String cookiesString = this.cookies.entrySet().parallelStream().map(ks -> ks.getKey() + "=" + ks.getValue()).reduce((""), (cks, current) -> cks + current + ";");
        this.requestBuilder.setHeader("cookie", cookiesString);
        this.headers.entrySet().parallelStream().forEach(ks -> this.requestBuilder.setHeader(ks.getKey(), ks.getValue()));
    }

    private static String generateParams(Map.Entry<String, String> ks) {
        try {
            return URLEncoder.encode(ks.getKey(), "UTF-8") + "=" + URLEncoder.encode(ks.getValue(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

}
