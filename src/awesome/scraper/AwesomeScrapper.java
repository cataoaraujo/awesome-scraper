package awesome.scraper;

import jdk.incubator.http.HttpClient;
import jdk.incubator.http.HttpResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AwesomeScrapper {

    private static final BlockingQueue<HttpClient> httpClients = new LinkedBlockingQueue<>(Collections.singletonList(HttpClient.newHttpClient()));

    private HttpClient currentClient;

    public AwesomeScrapper(String uri) {
        try {
            this.currentClient = httpClients.take();
            httpClients.put(this.currentClient);

            try {
                HttpResponse<String> response = new AwesomeRequest(uri).get(this.currentClient);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
