package edu.emory.c2g;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateTSO26GoManifest {  

    public static String goMount = "/BaseSpace/Projects";
 
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
    // args[1] = Illumina run name (the one specified by Abi)
    public static void main(String[] args) throws ParseException, IOException, ClassNotFoundException, SQLException, InterruptedException {  

        List<Process> processList = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        SimpleDateFormat sdfTimestamp = new SimpleDateFormat("yyyyMMddHHmm");

        String timestamp = sdfTimestamp.format(new java.util.Date());

        //String illuminaRunName = Paths.get(System.getProperty("user.dir")).getFileName().toString();
        String illuminaRunName = args[1];
        String platform = "NextSeq";
 
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");  
        Connection conn = DriverManager.getConnection(args[0]);
 
        CaseAttributesFinder caseAttributesFinder = new CaseAttributesFinder(conn);
        CMP26ProcedureFinder cMP26ProcedureFinder = new CMP26ProcedureFinder(conn);

        System.out.println("runs");
        System.out.println(String.format("%s\t%s\t%s\t%s", "run_id", "platform", "run_type", "run_data_location"));
        System.out.println(String.format("%s\t%s\t%s\t%s", illuminaRunName + "-TSO26_" + timestamp, platform, "TSO500 Flow Cell", goMount + "/" + illuminaRunName));

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
        //int illuminaSampleNumber = 0;
        while(((inLine = stdIn.readLine()) != null)) {
            //illuminaSampleNumber++;
            if(inLine.split(",").length == 0) {
                continue;
            }
            String sampleName = inLine.split(",")[0];
            String accessionNumber = null;
            try {
                Pattern patternSampleName = Pattern.compile("^([^-]+)-([0-9]+)-.*$");
                Matcher matcherSampleName = patternSampleName.matcher(sampleName);
                if(!matcherSampleName.matches()) {
                    //patternSampleName = Pattern.compile("(?i)^val(?:idation)?-([^-]+)-([0-9]+)-.*$");
                    patternSampleName = Pattern.compile("^([^-]+)-([0-9]+)$");
                    matcherSampleName = patternSampleName.matcher(sampleName);
                    if(!matcherSampleName.matches()) {
                        throw new ParseException("can't parse sample name " + sampleName, 0);
                    }
                }
                accessionNumber = String.format("%s-%s", matcherSampleName.group(1).toUpperCase(), new Integer(matcherSampleName.group(2)));
            }
            catch(ParseException e) {
                //throw new RuntimeException("can't parse sample name " + sampleName);
            }
            if(sampleName.matches("(?i)^val(?:idation)?-.*$")) {
                System.err.println(String.format("*** '%s' is a validation sample but will use '%s' demographics ***", sampleName, accessionNumber));
            }
            CaseAttributes caseAttributes;
            CMP26Procedure cMP26Procedure = null;
            caseAttributes = caseAttributesFinder.getByAccessionNumber(accessionNumber);
            if(caseAttributes == null) {
                caseAttributes = new CaseAttributes();
                caseAttributes.accessionNumber = sampleName;
                caseAttributes.client = "unknown";
                caseAttributes.dateAccessioned = new java.sql.Date(0);
                caseAttributes.dateCollected = new java.sql.Date(0);
                caseAttributes.dob = new java.sql.Date(0);
                caseAttributes.empi = "unknown";
                caseAttributes.firstName = "unknown";
                caseAttributes.gender = "M";
                caseAttributes.lastName = "unknown";
                caseAttributes.middleName = "unknown";
                caseAttributes.mrn = "unknown";
                caseAttributes.orderingProviderFirstName = "unknown";
                caseAttributes.orderingProviderLastName = "unknown";
                caseAttributes.specimenId = "unknown";
            }
            else {
                cMP26Procedure = cMP26ProcedureFinder.getBySpecimenId(caseAttributes.specimenId);
            }
                    
            for(int columnNumber = 0; columnNumber < columnNames.size(); columnNumber++) {
                if(columnNumber > 0) { System.out.print("\t"); }
                switch(columnNames.get(columnNumber)) {
                    case "run_id": System.out.print(illuminaRunName + "-TSO26_" + timestamp); break;
                    case "sample_category": System.out.print("Patient Sample"); break;
                    case "sample_id": System.out.print(sampleName); break;
                    case "stabilization": System.out.print("default"); break;
                    case "order_id": System.out.print(sampleName + "_" + timestamp); break;
                    case "test":
                        System.out.print("TSO26");
                        break;
                    case "disease_name": System.out.print("Tumor of Unknown Origin"); break;
                    case "emory_run_id": System.out.print(illuminaRunName); break;
                    case "emory_order_id": System.out.print(sampleName); break;
                    case "emory_base_file_url": System.out.print("https://patheuhmollabserv3.eushc.org/tso500-ruo-2.1.0.60/" + illuminaRunName); break;
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
                    case "order_date": System.out.print(cMP26Procedure != null && cMP26Procedure.dateOrdered != null ? sdf.format(cMP26Procedure.dateOrdered) : sdf.format(new java.util.Date())); break;
                    case "emory_archer_case_url": System.out.print(""); break;
                    case "emory_coverage_statement": System.out.print("[NO COVERAGE STATEMENT]"); break;
                    default: System.out.print(""); break;
                }
            }
            System.out.println();
        }
 
        conn.close();

        System.err.println("TSO500 - SNV GO Manifest creation complete");
        System.exit(0);

    }  

}
