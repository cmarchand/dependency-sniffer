/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, 
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can 
 * obtain one at http://mozilla.org/MPL/2.0/.
 */
package top.marchand.maven.dependency.sniffer.builds;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Scans a URL, and construct sub-urls or dependency files.
 * @author cmarchand
 */
public class DependencyScanner implements Runnable {
    private final String urlToScan;
    private final ExecutorService service;
    private final ErrorReporter reporter;

    public DependencyScanner(final String urlToScan, 
            final ExecutorService service,
            final ErrorReporter reporter) {
        super();
        this.urlToScan=urlToScan;
        this.service=service;
        this.reporter=reporter;
    }

    @Override
    public void run() {
        reporter.info("loonking at "+urlToScan);
        try {
            URL url = new URL(urlToScan);
            Document doc = Jsoup.parse(url, 1000);
            // it's not XPath, see https://jsoup.org/cookbook/extracting-data/selector-syntax
            Elements links = doc.select("a[href]");
            for(int i=0;i<links.size();i++) {
                Element el = links.get(i);
                String target = el.attr("href");
                if(!"../".equals(target)) {
                    String newUrl = urlToScan.endsWith("/") ? urlToScan+target : urlToScan+"/"+target;
                    if(newUrl.endsWith("-tree.txt")) {
                        // TODO
                        reporter.info("found "+newUrl);
                    } else if(newUrl.endsWith("/") && !newUrl.equals(urlToScan)) {
                        service.submit(new DependencyScanner(newUrl, service, reporter));
                    }
                }
            }
        } catch(IOException ex) {
            reporter.error(urlToScan, ex);
        }
    }
}
