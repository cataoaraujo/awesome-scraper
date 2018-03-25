package awesome.scraper;

import jdk.incubator.http.HttpClient;
import jdk.incubator.http.HttpResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class AwesomeScrapper {

    /**
     * HttpClient to be used for all request in this scraper
     */
    private static final HttpClient currentClient = HttpClient.newHttpClient();

    /**
     * Store the last response
     */
    private HttpResponse<String> response;

    /**
     * Current URI.
     * Started by the constructor and modified by the following requests
     */
    private String uri;

    /**
     * Store the current document (page).
     * This will be filled when calling getDocument()
     * And cleared when a request is made
     */
    private Document document;

    /**
     * Store the input data sent by the user
     */
    private final Map<String, String> formData = new HashMap<>();

    /**
     * Creates a new instance, fill the current URI and GET the current page
     *
     * @param uri
     */
    public AwesomeScrapper(String uri) {
        this.uri = uri;
        this.get(uri);
    }

    /**
     * GET the current URI, does not send any query parameters
     *
     * @param uri
     */
    private void get(String uri) {
        this.get(uri, Collections.emptyMap());
    }

    /**
     * GET the current URI, sending query parameters
     *
     * @param uri
     * @param data
     */
    private void get(String uri, Map<String, String> data) {
        this.clearRequest(uri);
        try {
            response = new AwesomeRequest(uri).get(this.currentClient, data);
            //Should follow redirects
            while (response.statusCode() == 302) {
                response = new AwesomeRequest(response.headers().firstValue("location").get()).get(this.currentClient);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * POST the current URI, sending form data
     *
     * @param uri
     * @param data
     */
    private void post(String uri, Map<String, String> data) {
        this.clearRequest(uri);
        try {
            response = new AwesomeRequest(uri).post(this.currentClient, data);
            //Should follow redirects
            while (response.statusCode() == 302) {
                response = new AwesomeRequest(response.headers().firstValue("location").get()).get(this.currentClient);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Clear all attributes related to the request, update the current URI
     *
     * @param uri
     */
    private void clearRequest(String uri) {
        this.uri = uri;
        this.document = null;
        this.response = null;
        formData.clear();
    }

    /**
     * Parse the current page
     *
     * @return
     */
    public Document getDocument() {
        if (document == null) {
            document = Jsoup.parse(response.body());
        }
        return document;
    }

    /**
     * Input a value to the form data
     *
     * @param inputName
     * @param value
     */
    public void input(String inputName, String value) {
        formData.put(inputName, value);
    }

    /**
     * Click in a specific element (using CSS Selector)
     *
     * @param selector
     */
    public void click(String selector) {
        Element inputElement = this.getDocument().select(selector).first();

        //Verify if it is a link
        if ("a".equalsIgnoreCase(inputElement.nodeName())) {
            String href = inputElement.attr("href");
            if (href != null && !href.isEmpty()) {
                if (href.startsWith("http")) {
                    this.get(href);
                } else {
                    this.get(this.uri + href);
                }
                return;
            }
        }

        //Send form
        Element formElement = this.findForm(inputElement);
        Map<String, String> data = this.getDefaultFormData(formElement);
        data.putAll(this.formData);

        String action = formElement.attr("action");
        String method = formElement.attr("method");

        if ("POST".equalsIgnoreCase(method)) {
            this.post(this.uri + action, data);
        } else {
            this.get(this.uri + action, data);
        }
    }

    /**
     * Search the form element for a designated input
     *
     * @param inputElement a input tag
     * @return the form element, or null if the form is not found
     */
    private Element findForm(Element inputElement) {
        boolean isForm = false;
        while (inputElement.hasParent() && !isForm) {
            inputElement = inputElement.parent();
            if (inputElement.nodeName().equalsIgnoreCase("form")) {
                isForm = true;
            }
        }
        if (isForm) return inputElement;
        return null;
    }

    /**
     * From a given form, search all input tags and returns a Map with its name and default value
     *
     * @param formElement
     * @return a Map<String, String> with the input name as its key, and the dafault value of the input
     */
    private Map<String, String> getDefaultFormData(Element formElement) {
        return formElement.getElementsByTag("input")
                .parallelStream()
                .collect(Collectors.toMap(element -> element.attr("name"), Element::val));
    }
}
