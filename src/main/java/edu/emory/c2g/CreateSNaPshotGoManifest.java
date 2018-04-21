package edu.emory.c2g;

import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

public class CreateSNaPshotGoManifest {

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
        ,"percent_tumor_nuclei"
        ,"diagnosis"
        ,"specimen_type"
        ,"specimen_collected"
        ,"specimen_received"
        ,"emory_run_id"
        ,"emory_order_id"            
    });

    // standard output = GO manifest
    // arg[0] = JDBC URL for CoPath database
    public static void main(String[] args) throws ParseException, IOException, ClassNotFoundException, SQLException {  

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        SimpleDateFormat sdfTimestamp = new SimpleDateFormat("yyyyMMddHHmm");

        String timestamp = sdfTimestamp.format(new java.util.Date());
        
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");  
        Connection conn = DriverManager.getConnection(args[0]);
        
        SNaPshotProcedureFinder sNaPshotProcedureFinder = new SNaPshotProcedureFinder(conn);
        CaseAttributesFinder caseAttributesFinder = new CaseAttributesFinder(conn);

        System.out.println("samples");
        for(int columnNumber = 0; columnNumber < columnNames.size(); columnNumber++) {
            if(columnNumber > 0) { System.out.print("\t"); }
            System.out.print(columnNames.get(columnNumber));
        }
        System.out.println();
        
        for(SNaPshotProcedure sNaPshotProcedure : sNaPshotProcedureFinder.getPending()) {
            CaseAttributes caseAttributes = caseAttributesFinder.getBySpecimenId(sNaPshotProcedure.specimenId);
            for(int columnNumber = 0; columnNumber < columnNames.size(); columnNumber++) {
                if(columnNumber > 0) { System.out.print("\t"); }
                switch(columnNames.get(columnNumber)) {
                    case "sample_id": System.out.print(caseAttributes.accessionNumber); break;
                    case "sample_category": System.out.print("Patient Sample"); break;
                    case "order_id": System.out.print(caseAttributes.accessionNumber + "_" + timestamp); break;
                    case "order_date": System.out.print(sdf.format(sNaPshotProcedure.dateOrdered)); break;
                    case "test": System.out.print("SNaPshot Panel"); break;
                    case "disease_name": System.out.print("Tumor of Unknown Origin"); break;
                    case "emory_order_id": System.out.print(caseAttributes.accessionNumber); break;
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
        }

        conn.close();
        
        System.err.println("SNaPshot GO Manifest creation complete");
        System.exit(0);

    }
    
}
