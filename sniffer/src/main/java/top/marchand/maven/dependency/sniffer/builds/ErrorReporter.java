/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package top.marchand.maven.dependency.sniffer.builds;

/**
 *
 * @author cmarchand
 */
public class ErrorReporter {
    
    public void error(final String message, final Exception exception) {
        System.err.println(message);
        exception.printStackTrace(System.err);
    }
    
    public void info(final String message) {
        System.out.println(message);
    }
    
}
