/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package top.marchand.maven.dependency.sniffer.builds;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A reporter
 * @author cmarchand
 */
public class ErrorReporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorReporter.class);
    
    public void error(final String message, final Throwable exception) {
        LOGGER.error(message, exception);
    }
    
    public void info(final String message) {
        LOGGER.info(message);
    }
    
    public void debug(String message) {
        LOGGER.debug(message);
    }
    
}
