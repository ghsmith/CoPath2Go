package edu.emory.c2g;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddDemographicsToCmp26GoManifest {  

    public static void main(String[] args) throws ParseException, IOException, ClassNotFoundException, SQLException {  

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");  
        Connection conn = DriverManager.getConnection("jdbc:sqlserver://xxx:1433;databaseName=xxx;user=xxx;password=xxx");
        
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
        while (((tsvLine = stdIn.readLine()) != null && tsvLine.length() != 0) & !"samples".equals(tsvLine)) {
            List<String> columnValues = Arrays.asList(tsvLine.split("\t"));
            String sampleId = columnValues.get(columnNames.indexOf("sample_id"));
            Pattern patternSampleId = Pattern.compile("^([^-]+)-([0-9]+)-.*$");
            Matcher matcherSampleId = patternSampleId.matcher(sampleId);
            if(!matcherSampleId.matches()) {
                throw new RuntimeException("can't parse sample ID");
            }
            String accessionNumber = String.format("%s-%s", matcherSampleId.group(1).toUpperCase(), new Integer(matcherSampleId.group(2)));
            CaseAttributes caseAttributes = caseAttributesFinder.getByAccessionNumber(accessionNumber);
            for(int columnNumber = 0; columnNumber < columnNames.size(); columnNumber++) {
                if(columnNumber > 0) { System.out.print("\t"); }
                switch(columnNames.get(columnNumber)) {
                    case "mrn": System.out.print(caseAttributes.mrn); break;
                    case "first_name": System.out.print(caseAttributes.firstName); break;
                    case "last_name": System.out.print(caseAttributes.lastName); break;
                    case "middle_initial": System.out.print(caseAttributes.middleName != null && caseAttributes.middleName.length() > 0 ? caseAttributes.middleName.substring(0, 1) : ""); break;
                    case "gender": System.out.print(caseAttributes.gender != null && caseAttributes.gender.length() > 0 ? caseAttributes.gender : ""); break;
                    case "dob": System.out.print(sdf.format(caseAttributes.dob)); break;
                    case "ordering_physician": System.out.print(caseAttributes.orderingProviderLastName + ", " + caseAttributes.orderingProviderFirstName); break;
                    case "specimen_collected": System.out.print(sdf.format(caseAttributes.dateCollected)); break;
                    case "specimen_received": System.out.print(sdf.format(caseAttributes.dateAccessioned)); break;
                    default: System.out.print(columnValues.get(columnNumber)); break;
                }
            }
            System.out.println();
        }
            
        conn.close();

    }  

}
