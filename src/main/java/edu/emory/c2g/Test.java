package edu.emory.c2g;

import java.io.IOException;

public class Test {

    public static void main(String[] args) throws IOException, InterruptedException {
        ArcherSampleFinder asf = new ArcherSampleFinder();
        System.out.println(asf.getByJobNumberAndSampleName(4703, "Burrus-Roscoe-EDTA-M_S12_R1_001"));
    }
    
}
