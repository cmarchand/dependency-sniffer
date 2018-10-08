/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, 
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can 
 * obtain one at http://mozilla.org/MPL/2.0/.
 */
package top.marchand.maven.dependency.sniffer;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import top.marchand.maven.dependency.config.Config;
import top.marchand.maven.dependency.sniffer.builds.DependencyDocumentBuilder;
import top.marchand.maven.dependency.sniffer.builds.DependencyService;
import top.marchand.maven.dependency.sniffer.builds.HttpScanner;
import top.marchand.maven.dependency.sniffer.builds.ErrorReporter;
import top.marchand.maven.dependency.sniffer.builds.ScanService;
import top.marchand.org.basex.api.BaseXClient;

/**
 *
 * @author cmarchand
 */
public class Sniffer implements ScanService, DependencyService {
    
    public static void main(String[] args) throws IOException, ParsingException {
        new Sniffer(parseArguments(args)).run();
    }
    private final Config config;
    private final DependencyDocumentBuilder dependencyDocumentBuidler;
    private final ErrorReporter reporter;
    private ExecutorService scannerService;
    private ExecutorService dependencyService;
    private BaseXClient client;
//    private long lastSubmit = 0L;
    private Document tree;
    private AtomicInteger httpScansCount;
    private AtomicInteger dependencyScansCount;
    
    public Sniffer(Config config) {
        super();
        this.config=config;
        this.dependencyDocumentBuidler = new DependencyDocumentBuilder();
        this.reporter = new ErrorReporter();
    }
    
    public void run() throws IOException {
        dependencyScansCount = new AtomicInteger(0);
        httpScansCount = new AtomicInteger(0);
        final ScheduledExecutorService surveillor = Executors.newSingleThreadScheduledExecutor();
        scannerService = Executors.newFixedThreadPool(config.getNbThreads());
        dependencyService = Executors.newSingleThreadExecutor();
        client = new BaseXClient(
                config.getBasexHost(), 
                config.getBasexPort(), 
                config.getBasexUser(), 
                config.getBasexPassword());
        // sets the collection to use
        client.execute("CHECK DEPS;");
        // gets the tree.xml document
        BaseXClient.Query query = client.query("db:open('DEPS','tree.xml')");
        tree = null;
        String content="";
        try {
            content = query.execute();
            tree = new nu.xom.Builder().build(content, "/tree.xml");
        } catch(ParsingException | IOException ex) {
            reporter.error(content, ex);
            tree = new Document(new Element("tree"));
        }

        for(String root: config.getNexusRoots()) {
            submitScantask(new HttpScanner(root, this, reporter, this));
        }
        surveillor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                reporter.debug("httpScans: "+httpScansCount.get()+" dependencyScans: "+dependencyScansCount.get());
                if(httpScansCount.get()==0 && dependencyScansCount.get()==0) {
                    scannerService.shutdown();
                    surveillor.shutdown();
                    dependencyService.shutdown();
                    try {
                        scannerService.awaitTermination(1l, TimeUnit.HOURS);
                        dependencyService.awaitTermination(1l, TimeUnit.HOURS);
                        client.close();
                    } catch(InterruptedException ex) {
                        reporter.error("HTTP scanning took more than 1 hour", ex);
                    } catch (IOException ex) {
                        reporter.error("while closing BaseXClient", ex);
                    }
                }
            }
        }, 0, 1, TimeUnit.SECONDS);
    }
    
    @Override
    public void submitScantask(Runnable t) {
        //lastSubmit = System.currentTimeMillis();
        httpScansCount.incrementAndGet();
        scannerService.submit(t);
    }
            
    @Override
    public void submitDependencyTask(Runnable r) {
        //lastSubmit=System.currentTimeMillis();
        //reporter.info("received dependency task");
        dependencyScansCount.incrementAndGet();
        dependencyService.submit(r);
    }

    @Override
    public BaseXClient getDbClient() {
        return client;
    }
    
    @Override
    public Document getTreeDocument() {
        return tree;
    }

    protected static Config parseArguments(String[] args) throws IOException, ParsingException {
        if(args.length<2) {
            System.err.println(SYNTAX);
            throw new IllegalArgumentException("At least 2 arguments are required");
        }
        boolean isConfigFile = false;
        Config config = null;
        for(String s: args) {
            if("-c".equals(s)) {
                isConfigFile = true; 
                continue;
            }
            if(isConfigFile) {
                try {
                    config = Config.parseConfigFile(new File(s));
                } catch(IOException | ParsingException ex) {
                    System.err.println("while reading config");
                    throw ex;
                }
                isConfigFile = !isConfigFile;
            }
        }
        return config;
    }
    
    public static final String SYNTAX = "java top.marchand.maven.dependency.sniffer.Sniffer -c config <fileName>";

    @Override
    public void scanTaskCompleted() {
        httpScansCount.decrementAndGet();
    }

    @Override
    public void dependencyTaskCompleted() {
        dependencyScansCount.decrementAndGet();
    }

}
