package awesome.scraper;

import jdk.incubator.http.HttpClient;
import jdk.incubator.http.HttpRequest;
import jdk.incubator.http.HttpResponse;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class AwesomeRequest {

    private HttpRequest.Builder requestBuilder = null;

    private final Map<String, String> cookies = new HashMap<>();
    private final Map<String, String> headers = new HashMap<>();

    public AwesomeRequest(String uri) {
        try {
            this.requestBuilder = HttpRequest.newBuilder().uri(new URI(uri));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public AwesomeRequest timeout(Duration duration) {
        this.requestBuilder = this.requestBuilder.timeout(duration);
        return this;
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
        this.generateHeaders();
        return client.send(this.requestBuilder.GET().build(), HttpResponse.BodyHandler.asString());
    }

    private void generateHeaders() {
        String cookiesString = this.cookies.entrySet().parallelStream().map(ks -> ks.getKey() + "=" + ks.getValue()).reduce((""), (cks, current) -> cks + current + ";");
        this.requestBuilder.setHeader("cookie", cookiesString);
        this.headers.entrySet().parallelStream().forEach(ks -> this.requestBuilder.setHeader(ks.getKey(), ks.getValue()));
    }

}
