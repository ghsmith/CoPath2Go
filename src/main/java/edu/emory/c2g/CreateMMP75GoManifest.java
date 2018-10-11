package edu.emory.c2g;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateMMP75GoManifest {  

    public static String goMount = "/illumina_runs01";
    
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
        ,"emory_base_file_url"
        ,"emory_coverage_statement"
        ,"emory_facility_mrn"
        ,"emory_archer_case_url"
    });
    
    // current directory must be root of Illumina run directory
    // standard input = Illumina sample sheet
    // standard output = GO manifest
    // args[0] = JDBC URL for CoPath database
    // args[1] = Archer job numbers (separated by commas)
    // args[2] = IP address of Archer VM
    // args[3] = GO merge utility (e.g., "python merge.pex")
    public static void main(String[] args) throws ParseException, IOException, ClassNotFoundException, SQLException, InterruptedException {  

        List<Process> processList = new ArrayList<>();

        Integer[] archerJobNumbers = Arrays.stream(args[1].split(",")).map((archerJobNumber) -> new Integer(archerJobNumber)).toArray((size) -> new Integer[size]);
        
        String pythonMerge = null;
        if(args.length > 3) {
            pythonMerge = args[3];
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
        MMP75ProcedureFinder mMP75ProcedureFinder = new MMP75ProcedureFinder(conn);
        ArcherSampleFinder archerSampleFinder = new ArcherSampleFinder(args[2]);

        System.out.println("runs");
        System.out.println(String.format("%s\t%s\t%s\t%s", "run_id", "platform", "run_type", "run_data_location"));
        System.out.println(String.format("%s\t%s\t%s\t%s", illuminaRunName + "_" + timestamp, platform, platform + "-Myeloid", goMount + "/" + illuminaRunName));
        
        System.out.println("samples");
        for(int columnNumber = 0; columnNumber < columnNames.size(); columnNumber++) {
            if(columnNumber > 0) { System.out.print("\t"); }
            System.out.print(columnNames.get(columnNumber));
        }
        System.out.println();

        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String inLine;
        while(((inLine = stdIn.readLine()) != null) && !inLine.startsWith("[Data]")) {
        }
        stdIn.readLine();
        while(((inLine = stdIn.readLine()) != null)) {
            if(inLine.split(",").length == 0) {
                continue;
            }
            String sampleName = inLine.split(",")[1];
            //Pattern patternSampleName = Pattern.compile("^([^-]+)-([0-9]+)-.*$");
            //Matcher matcherSampleName = patternSampleName.matcher(sampleName);
            //if(!matcherSampleName.matches()) {
            //    throw new RuntimeException("can't parse sample name " + sampleName);
            //}
            //String accessionNumber = String.format("%s-%s", matcherSampleName.group(1).toUpperCase(), new Integer(matcherSampleName.group(2)));
            //CaseAttributes caseAttributes = caseAttributesFinder.getByAccessionNumber(accessionNumber);
            //CMP26Procedure cMP26Procedure = cMP26ProcedureFinder.getBySpecimenId(caseAttributes.specimenId);

            CaseAttributes caseAttributes = new CaseAttributes();
            caseAttributes.mrn = "0";
            caseAttributes.empi = "0";
            caseAttributes.firstName = "MMP75";
            caseAttributes.lastName = "VALIDATION";
            caseAttributes.dob = new Date(new java.util.Date().getTime());
            caseAttributes.dateCollected = new Date(new java.util.Date().getTime());
            caseAttributes.dateCollected = new Date(new java.util.Date().getTime());
            caseAttributes.dateAccessioned = new Date(new java.util.Date().getTime());
            MMP75Procedure mMP75Procedure = new MMP75Procedure();
            
            ArcherSample archerSample = null;
            for(Integer archerJobNumber: archerJobNumbers) {
                archerSample = archerSampleFinder.getByJobNumberAndSampleName(archerJobNumber, sampleName);
                if(archerSample != null) {
                    break;
                }
            }
            
            for(int columnNumber = 0; columnNumber < columnNames.size(); columnNumber++) {
                if(columnNumber > 0) { System.out.print("\t"); }
                switch(columnNames.get(columnNumber)) {
                    case "run_id": System.out.print(illuminaRunName + "_" + timestamp); break;
                    case "sample_category": System.out.print("Patient Sample"); break;
                    case "sample_id": System.out.print(sampleName); break;
                    case "stabilization": System.out.print("default"); break;
                    case "order_id": System.out.print(sampleName + "_" + timestamp); break;
                    case "test": System.out.print("Myeloid Mutation Panel 75 (" + platform + ")"); break;
                    case "disease_name": System.out.print("Tumor of Unknown Origin"); break;
                    case "emory_run_id": System.out.print(illuminaRunName); break;
                    case "emory_order_id": System.out.print(sampleName); break;
                    case "emory_base_file_url": System.out.print("https://patheuhmollabserv2.eushc.org/illumina_runs01/" + illuminaRunName + "/Data/Intensities/BaseCalls/Archer_Run"); break;
                    case "mrn": System.out.print(caseAttributes.empi); break;
                    case "emory_facility_mrn": System.out.print(caseAttributes.mrn); break;
                    case "first_name": System.out.print(caseAttributes.firstName); break;
                    case "last_name": System.out.print(caseAttributes.lastName); break;
                    case "middle_initial": System.out.print(caseAttributes.middleName != null && caseAttributes.middleName.length() > 0 ? caseAttributes.middleName.substring(0, 1) : ""); break;
                    case "gender": System.out.print(caseAttributes.gender != null && caseAttributes.gender.length() > 0 ? caseAttributes.gender : ""); break;
                    case "dob": System.out.print(sdf.format(caseAttributes.dob)); break;
                    case "ordering_physician": System.out.print(caseAttributes.orderingProviderLastName + ", " + caseAttributes.orderingProviderFirstName); break;
                    case "ordering_physician_institute": System.out.print(caseAttributes.client); break;
                    case "specimen_collected": System.out.print(sdf.format(caseAttributes.dateCollected)); break;
                    case "specimen_received": System.out.print(sdf.format(caseAttributes.dateAccessioned)); break;
                    case "order_date": System.out.print(mMP75Procedure != null && mMP75Procedure.dateOrdered != null ? sdf.format(mMP75Procedure.dateOrdered) : ""); break;
                    case "emory_archer_case_url": System.out.print("https://patheuhmollabserv2.eushc.org:14443/job/" + archerSample.archerJobNumber + "/sample/" + archerSample.archerSampleNumber + "/read-statistics"); break;
                    case "emory_coverage_statement": System.out.print("[NO COVERAGE STATEMENT]"); break;
                    default: System.out.print(""); break;
                }
            }
            System.out.println();
            /*if(pythonMerge != null) {
                Files.deleteIfExists(new File("Data/Intensities/BaseCalls/Alt_Alignment/" + sampleName + ".merged.go.bam").toPath());
                Files.deleteIfExists(new File("Data/Intensities/BaseCalls/Alt_Alignment/" + sampleName + ".merged.go.bam.bai").toPath());
                Files.deleteIfExists(new File("Data/Intensities/BaseCalls/Alt_Alignment/" + sampleName + ".merged.go.vcf").toPath());
                String mergeCommandLine = String.format(
                    pythonMerge + " --bam_path %s" + " --bam_path %s" + " --freebayes_vcf_path %s" + " --gatk_vcf_path %s" + " --varscan_vcf_path %s" + " %s %s",
                    "Data/Intensities/BaseCalls/Alt_Alignment/" + sampleName + ".A.bwa-mem.final.bam",
                    "Data/Intensities/BaseCalls/Alt_Alignment/" + sampleName + ".B.bwa-mem.final.bam",
                    "Data/Intensities/BaseCalls/Alt_Alignment/" + sampleName + ".freebayes_all.bwa-mem.final.vcf",
                    "Data/Intensities/BaseCalls/Alt_Alignment/" + sampleName + ".gatk_all.bwa-mem.final.vcf",
                    "Data/Intensities/BaseCalls/Alt_Alignment/" + sampleName + ".varscan_all.bwa-mem.final.vcf",
                    "Data/Intensities/BaseCalls/Alt_Alignment/" + sampleName + ".merged.go.bam",
                    "Data/Intensities/BaseCalls/Alt_Alignment/" + sampleName + ".merged.go.vcf"
                );
                System.err.println();
                System.err.println(mergeCommandLine);
                System.err.println();
                ProcessBuilder pb = new ProcessBuilder(Arrays.asList(mergeCommandLine.split(" ")));
                pb.redirectErrorStream(true);
                Process p = pb.start();
                (new Thread() {
                    BufferedReader pReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    public void run() {
                        try {
                            String pLine = pReader.readLine();
                            while (pLine != null) {
                                System.err.println(sampleName + ": " + pLine);
                                pLine = pReader.readLine();
                            }
                            pReader.close();                        
                        }
                        catch(IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }).start();
                processList.add(p);
            }*/
        }
            
        //conn.close();

        for(Process p : processList) {
          if(p.waitFor() != 0) {
            throw new RuntimeException("merge error");
          }
        }
        
        System.err.println("MMP75 GO Manifest creation complete");
        System.exit(0);

    }  

}
