package edu.emory.c2g;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateCmp26GoManifest {  

    public static String goMount = "/mount/illumina_runs01";
    
    public static List<String> columnNames = Arrays.asList(new String[] {
        "run_id"
        ,"sample_category"
        ,"sample_id"
        ,"stabilization"
        ,"order_id"
        ,"order_date"
        ,"test"
        ,"disease_name"
        ,"mrn"
        ,"first_name"
        ,"last_name"
        ,"middle_initial"
        ,"gender"
        ,"dob"
        ,"ordering_physician"
        ,"ordering_physician_suffix"
        ,"ordering_physician_institute"
        ,"diagnosis"
        ,"specimen_type"
        ,"specimen_collected"
        ,"specimen_received"
        ,"emory_run_id"
        ,"emory_order_id"
    });
    
    // current directory must be root of Illumina run directory
    // standard input = Illumina sample sheet
    // standard output = GO manifest
    // args[0] = JDBC URL for CoPath database
    // args[1] = GO merge utility (e.g., "python merge.pex")
    public static void main(String[] args) throws ParseException, IOException, ClassNotFoundException, SQLException, InterruptedException {  

        String pythonMerge = null;
        if(args.length > 1) {
            pythonMerge = args[1];
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        SimpleDateFormat sdfTimestamp = new SimpleDateFormat("yyyyMMddHHmm");

        String timestamp = sdfTimestamp.format(new java.util.Date());

        String illuminaRunName = Paths.get(System.getProperty("user.dir")).getFileName().toString();
        String platform = "Unknown";
        if(illuminaRunName.contains("M01382")) {
            platform = "MiSeq";
        }
        else if(illuminaRunName.contains("NS500796")) {
            platform = "NextSeq";
        }
        
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");  
        Connection conn = DriverManager.getConnection(args[0]);
        
        CaseAttributesFinder caseAttributesFinder = new CaseAttributesFinder(conn);

        System.out.println("runs");
        System.out.println(String.format("%s\t%s\t%s\t%s", "run_id", "platform", "run_type", "run_data_location"));
        System.out.println(String.format("%s\t%s\t%s\t%s", illuminaRunName + "_" + timestamp, platform, platform, goMount));
        
        System.out.println("samples");
        for(int columnNumber = 0; columnNumber < columnNames.size(); columnNumber++) {
            if(columnNumber > 0) { System.out.print("\t"); }
            System.out.print(columnNames.get(columnNumber));
        }
        System.out.println();

        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String inLine;
        while(((inLine = stdIn.readLine()) != null) && !"[Data]".equals(inLine)) {
        }
        stdIn.readLine();
        String lastSampleName = null;
        while(((inLine = stdIn.readLine()) != null)) {
            String sampleName = inLine.split(",")[1];
            // Illumina samples are paired (pool A, pool B) and appear contiguously
            if(lastSampleName != null && lastSampleName.equals(sampleName)) {
                continue;
            }
            else {
                lastSampleName = sampleName;
            }
            Pattern patternSampleName = Pattern.compile("^([^-]+)-([0-9]+)-.*$");
            Matcher matcherSampleName = patternSampleName.matcher(sampleName);
            if(!matcherSampleName.matches()) {
                throw new RuntimeException("can't parse sample name " + sampleName);
            }
            String accessionNumber = String.format("%s-%s", matcherSampleName.group(1).toUpperCase(), new Integer(matcherSampleName.group(2)));
            CaseAttributes caseAttributes = caseAttributesFinder.getByAccessionNumber(accessionNumber);
            for(int columnNumber = 0; columnNumber < columnNames.size(); columnNumber++) {
                if(columnNumber > 0) { System.out.print("\t"); }
                switch(columnNames.get(columnNumber)) {
                    case "run_id": System.out.print(illuminaRunName + "_" + timestamp); break;
                    case "sample_category": System.out.print("Patient Sample"); break;
                    case "sample_id": System.out.print(sampleName); break;
                    case "stabilization": System.out.print("default"); break;
                    case "order_id": System.out.print(sampleName + "_" + timestamp); break;
                    case "test": System.out.print("Cancer Mutation Panel 26 (" + platform + ")"); break;
                    case "disease_name": System.out.print("Tumor of Unknown Origin"); break;
                    case "emory_run_id": System.out.print(illuminaRunName); break;
                    case "emory_order_id": System.out.print(sampleName); break;
                    case "mrn": System.out.print(caseAttributes.mrn); break;
                    case "first_name": System.out.print(caseAttributes.firstName); break;
                    case "last_name": System.out.print(caseAttributes.lastName); break;
                    case "middle_initial": System.out.print(caseAttributes.middleName != null && caseAttributes.middleName.length() > 0 ? caseAttributes.middleName.substring(0, 1) : ""); break;
                    case "gender": System.out.print(caseAttributes.gender != null && caseAttributes.gender.length() > 0 ? caseAttributes.gender : ""); break;
                    case "dob": System.out.print(sdf.format(caseAttributes.dob)); break;
                    case "ordering_physician": System.out.print(caseAttributes.orderingProviderLastName + ", " + caseAttributes.orderingProviderFirstName); break;
                    case "ordering_physician_institute": System.out.print(caseAttributes.client); break;
                    case "specimen_collected": System.out.print(sdf.format(caseAttributes.dateCollected)); break;
                    case "specimen_received": System.out.print(sdf.format(caseAttributes.dateAccessioned)); break;
                    default: System.out.print(""); break;
                }
            }
            System.out.println();
            if(pythonMerge != null) {
                String mergeCommandLine = String.format(
                    pythonMerge + " --bam_path %s" + " --bam_path %s" + " --frebayes_vcf_path %s" + " --gatk_vcf_path %s" + " --varscan_vcf_path %s" + " %s %s",
                    "Data/Intensities/BaseCalls/Alt_Alignment/" + sampleName + ".A.bwa-mem.final.bam",
                    "Data/Intensities/BaseCalls/Alt_Alignment/" + sampleName + ".B.bwa-mem.final.bam",
                    "Data/Intensities/BaseCalls/Alt_Alignment/" + sampleName + ".freebayes_all.bwa-mem.final.vcf",
                    "Data/Intensities/BaseCalls/Alt_Alignment/" + sampleName + ".gatk_all.bwa-mem.final.vcf",
                    "Data/Intensities/BaseCalls/Alt_Alignment/" + sampleName + ".varscan_all.bwa-mem.final.vcf",
                    "Data/Intensities/BaseCalls/Alt_Alignment/" + sampleName + ".merged.go.bam",
                    "Data/Intensities/BaseCalls/Alt_Alignment/" + sampleName + ".merged.go.vcf"
                );
                System.err.println(mergeCommandLine);
                Process p = Runtime.getRuntime().exec(mergeCommandLine);
                int mergeReturnCode = p.waitFor();
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;	
                while((line = reader.readLine()) != null) {
                    System.err.println(line);
                }                    
                if(mergeReturnCode != 0) {
                    throw new RuntimeException("merge error");
                }
            }
        }
            
        conn.close();

    }  

}
