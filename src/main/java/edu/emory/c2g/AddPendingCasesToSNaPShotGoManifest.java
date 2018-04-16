package edu.emory.c2g;

import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class AddPendingCasesToSNaPShotGoManifest {

    public static void main(String[] args) throws ParseException, IOException, ClassNotFoundException, SQLException {  

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");  
        Connection conn = DriverManager.getConnection("jdbc:sqlserver://xxx:1433;databaseName=xxx;user=xxx;password=xxx");
        
        SNaPShotProcedureFinder sNaPShotProcedureFinder = new SNaPShotProcedureFinder(conn);
        CaseAttributesFinder caseAttributesFinder = new CaseAttributesFinder(conn);

        for(SNaPShotProcedure sNaPShotProcedure : sNaPShotProcedureFinder.getPending()) {
            System.out.println(sNaPShotProcedure);
        }
        
    }
    
}
