package edu.emory.c2g;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Make sure that SSH key pair authentication is configured.
 */
public class ArcherSampleFinder {

    public String archerIPAddress;
    
    public ArcherSampleFinder(String archerIPAddress) {
        this.archerIPAddress = archerIPAddress;
    }

    ArcherSampleFinder() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public ArcherSample getByJobNumberAndSampleName(Integer archerJobNumber, String archerSampleName) throws IOException, InterruptedException {

        final ArcherSample[] archerSample = new ArcherSample[1];
        
        String commandLine = String.format(
              "(ssh root@%s << EOF\n"
            + "/var/www/html/archer_web/manage.py dbshell\n"
            + "select id from samples.sample where job_id=%d and name='%s';\n"
            + "\\q\n"
            + "exit\n"
            + "EOF\n"
            + ") 2>&1 | head -5 | tail -1",
            archerIPAddress,
            archerJobNumber,
            archerSampleName
        );
        System.err.println();
        System.err.println(commandLine);
        System.err.println();
        ProcessBuilder pb = new ProcessBuilder(new String[] {"sh"});
        pb.redirectErrorStream(true);
        Process p = pb.start();
        try (BufferedWriter pWriter = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()))) {
            pWriter.write(commandLine);
        }
        (new Thread() {
            BufferedReader pReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            public void run() {
                try {
                    String pLine = pReader.readLine();
                    while (pLine != null) {
                        System.err.println(archerSampleName + ": " + pLine);
                        archerSample[0] = new ArcherSample();
                        archerSample[0].archerJobNumber = archerJobNumber;
                        archerSample[0].archerSampleNumber = new Integer(pLine.trim());
                        pLine = pReader.readLine();
                    }
                    pReader.close();                        
                }
                catch(IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }).start();
        
        p.waitFor();
        
        return archerSample[0];
        
    }

}
