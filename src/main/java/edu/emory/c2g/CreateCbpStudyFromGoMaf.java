package edu.emory.c2g;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.xml.bind.DatatypeConverter;

public class CreateCbpStudyFromGoMaf {  
 
    // current directory must be root of Illumina run directory
    // standard input = Illumina sample sheet
    // standard output = GO manifest
    // args[0] = JDBC URL for CoPath database
    // args[1] = hash seed
    public static void main(String[] args) throws ParseException, IOException, ClassNotFoundException, SQLException, InterruptedException, NoSuchAlgorithmException {  

        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");  
        Connection conn = DriverManager.getConnection(args[0]);
 
        CaseAttributesFinder caseAttributesFinder = new CaseAttributesFinder(conn);

        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        
        PrintStream fileDme = new PrintStream(new File("data_mutations_extended.txt"));

        Pattern patternGoOrder = Pattern.compile("^([A-Z][A-Z]?[A-Z]?[0-9][0-9])-([0-9]+)-.*_([0-9]{12})$");

        Map<String, String> accNoCbpEmpiMap = new HashMap<>();
        
        String inLine;
        fileDme.println(stdIn.readLine());
        while(((inLine = stdIn.readLine()) != null)) {
            String goOrder = inLine.split("\t")[15];
            Matcher matcherGoOrder = patternGoOrder.matcher(goOrder);
            if(!matcherGoOrder.matches()) {
                System.err.println("skipping: " + goOrder);
                continue;
            }
            String accNo = String.format("%s-%s", matcherGoOrder.group(1), matcherGoOrder.group(2));
            String accNoCbp = String.format("%s-%s-%s", matcherGoOrder.group(1), matcherGoOrder.group(2), matcherGoOrder.group(3));
            String empi = accNoCbpEmpiMap.get(accNoCbp);
            if(empi == null) {
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update((caseAttributesFinder.getByAccessionNumber(accNo).empi + args[1]).getBytes());
                byte[] digest = md.digest();
                empi = DatatypeConverter.printHexBinary(digest).substring(0,8);
                accNoCbpEmpiMap.put(accNoCbp, empi);
            }
            System.err.println(String.format("%s: %s", accNoCbp, empi));
            fileDme.println(inLine.replace(goOrder, accNoCbp));
        }
        
        fileDme.close();
 
        conn.close();

        PrintStream fileDc = new PrintStream(new File("data_clinical.txt"));
        fileDc.println("#Patient Identifier\tSample Identifier\tSubtype");
        fileDc.println("#Patient identifier\tSample Identifier\tSubtype description");
        fileDc.println("#STRING\tSTRING\tSTRING");
        fileDc.println("#1\t1\t1");
        fileDc.println("PATIENT_ID\tSAMPLE_ID\tSUBTYPE");
        for(String accNoCbp : accNoCbpEmpiMap.keySet()) {
            fileDc.println(String.format("%s\t%s\t", accNoCbpEmpiMap.get(accNoCbp), accNoCbp));
        }
        fileDc.close();

        PrintStream fileC = new PrintStream(new File("case_.txt"));
        fileC.print(String.format("case_list_ids: %s", accNoCbpEmpiMap.keySet().stream().collect(Collectors.joining("\t"))));
        fileC.close();
        
        
        /*PrintStream fileDts = new PrintStream(new File("data_timeline_specimen.txt"));
        fileDc.println("PATIENT_ID\tSTART_DATE\tSTOP_DATER\tEVENT_TYPE\tSPECIMEN_REFERENCE_NUMBER");
        for(String accNoCbp : accNoCbpEmpiMap.keySet()) {
            fileDc.println(String.format("%s\t%s\t%s\t%s\t%s", accNoCbpEmpiMap.get(accNoCbp), , "", "specimen", accNoCbp));
        }
        fileDc.close();*/
        
        System.exit(0);

    }  

}
