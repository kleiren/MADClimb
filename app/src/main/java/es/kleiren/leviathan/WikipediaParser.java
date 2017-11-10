package es.kleiren.leviathan;


import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * Created by carlos on 4/10/17.
 */

public class WikipediaParser extends AsyncTask<String, Void, String> {

    private final String baseUrl;

    public WikipediaParser(String lang) {
        this.baseUrl = String.format("http://%s.wikipedia.org/wiki/", lang);
    }

    public String fetchFirstParagraph(String article) throws IOException {
        String url = baseUrl + article;
        Document doc = Jsoup.connect(url).get();
        Elements paragraphs = doc.select(".mw-content-ltr p");

        Element firstParagraph = paragraphs.first();

        return firstParagraph.text();
    }

    @Override
    protected String doInBackground(String... params) {
        WikipediaParser parser = new WikipediaParser("en");
        try {
            return parser.fetchFirstParagraph(params[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }
}
