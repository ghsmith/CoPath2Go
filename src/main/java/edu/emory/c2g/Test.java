package edu.emory.c2g;

import java.io.IOException;

public class Test {

    public static void main(String[] args) throws IOException, InterruptedException {
        ArcherSampleFinder asf = new ArcherSampleFinder("192.168.122.40", "22");
        System.out.println(asf.getByJobNumberAndSampleName(4702, "Burrus-Roscoe-EDTA-M_S12_R1_001"));
    }
    
}
