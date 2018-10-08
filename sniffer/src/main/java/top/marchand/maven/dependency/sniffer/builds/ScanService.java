/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, 
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can 
 * obtain one at http://mozilla.org/MPL/2.0/.
 */
package top.marchand.maven.dependency.sniffer.builds;

/**
 *
 * @author cmarchand
 */
public interface ScanService {
    /**
     * Submits a new Http Scan task
     * @param t 
     */
    public void submitScantask(Runnable t);
    
    /**
     * Notifies that a scan task is completed
     */
    public void scanTaskCompleted();
}
