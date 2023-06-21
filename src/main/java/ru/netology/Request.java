package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class Request {
    private String requestMethod;
    private String path;
    private URI url;
    private List<NameValuePair> list;

    public Request(String requestMethod, String path, URI url) {
        this.requestMethod = requestMethod;
        this.path = path;
        this.url = url;
        this.list = URLEncodedUtils.parse(url, StandardCharsets.UTF_8);

    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getPath() {
        return path;
    }

    public List<NameValuePair> getQueryParams() {
        if (list.size() != 0) {
            return list;
        } else {
            return null;
        }
    }

    public String getQueryParam(String name) {
        Optional<NameValuePair> pair = list.stream()
                .filter(o -> o.getName().equals(name))
                .findFirst();
        if (!pair.isEmpty()) {
            NameValuePair np = pair.get();
            return np.getValue();
        } else {
            return null;
        }
    }

}
