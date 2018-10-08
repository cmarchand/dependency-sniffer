/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, 
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can 
 * obtain one at http://mozilla.org/MPL/2.0/.
 */
package top.marchand.maven.dependency.sniffer.builds;

import java.io.IOException;
import java.net.URL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Scans a URL, looks for sub-pages, or dependencies.
 * @author cmarchand
 */
public class HttpScanner implements Runnable {
    private final String urlToScan;
    private final ScanService scannerService;
    private final ErrorReporter reporter;
    private final DependencyService dependencyService;

    public HttpScanner(final String urlToScan, 
            final ScanService scannerService,
            final ErrorReporter reporter,
            final DependencyService dependencyService) {
        super();
        this.urlToScan=urlToScan;
        this.scannerService=scannerService;
        this.reporter=reporter;
        this.dependencyService=dependencyService;
    }

    @Override
    public void run() {
//        reporter.info("looking at "+urlToScan);
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
                        reporter.info("\tfound "+target);
                        dependencyService.submitDependencyTask(new DependencyScanner(target, reporter, dependencyService));
                    } else if(newUrl.endsWith("/") && !newUrl.equals(urlToScan) && !target.startsWith(urlToScan)) {
                        scannerService.submitScantask(new HttpScanner(newUrl, scannerService, reporter, dependencyService));
                    }
                }
            }
        } catch(IOException ex) {
            reporter.error(urlToScan, ex);
        } finally {
            scannerService.scanTaskCompleted();
        }
    }
}
