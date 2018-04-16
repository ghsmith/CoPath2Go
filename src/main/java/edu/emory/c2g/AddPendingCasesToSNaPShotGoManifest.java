package edu.emory.c2g;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

public class AddPendingCasesToSNaPShotGoManifest {

    public static void main(String[] args) throws ParseException, IOException, ClassNotFoundException, SQLException {  

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        SimpleDateFormat sdfTimestamp = new SimpleDateFormat("yyyyMMddHHmm");

        String timestamp = sdfTimestamp.format(new java.util.Date());
        
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");  
        Connection conn = DriverManager.getConnection("jdbc:sqlserver://xxx:1433;databaseName=xxx;user=xxx;password=xxx");
        
        SNaPShotProcedureFinder sNaPShotProcedureFinder = new SNaPShotProcedureFinder(conn);
        CaseAttributesFinder caseAttributesFinder = new CaseAttributesFinder(conn);
        
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String tsvLine;
        while (((tsvLine = stdIn.readLine()) != null && tsvLine.length() != 0) & !"samples".equals(tsvLine)) {
            System.out.println(tsvLine);
        }
        System.out.println(tsvLine); // this line is "samples"
        tsvLine = stdIn.readLine();
        System.out.println(tsvLine); // this line is the column headings
        List<String> columnNames = Arrays.asList(tsvLine.split("\t"));
        
        for(SNaPShotProcedure sNaPShotProcedure : sNaPShotProcedureFinder.getPending()) {
            CaseAttributes caseAttributes = caseAttributesFinder.getBySpecimenId(sNaPShotProcedure.specimenId);
            for(int columnNumber = 0; columnNumber < columnNames.size(); columnNumber++) {
                if(columnNumber > 0) { System.out.print("\t"); }
                switch(columnNames.get(columnNumber)) {
                    case "sample_id": System.out.print(caseAttributes.accessionNumber); break;
                    case "sample_category": System.out.print("Patient Sample"); break;
                    case "order_id": System.out.print(caseAttributes.accessionNumber + "_" + timestamp); break;
                    case "order_date": System.out.print(sdf.format(sNaPShotProcedure.dateOrdered)); break;
                    case "test": System.out.print("SNaPshot Panel"); break;
                    case "disease_name": System.out.print("Tumor of Unknown Origin"); break;
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
        
    }
    
}
