package edu.emory.c2g;

import java.sql.*;  
import java.util.ArrayList;
import java.util.List;

public class SNaPShotProcedureFinder {

    public Connection conn;
    
    public SNaPShotProcedureFinder(Connection conn) {
        this.conn = conn;
    }
    
    public List<SNaPShotProcedure> getPending() throws SQLException {
        
        List<SNaPShotProcedure> sNaPShotProcedures = new ArrayList<>();
        
        String SQL =
          " select "
        + "   dbo.p_special_proc.specimen_id, "
        + "   dbo.p_special_proc.order_date "
        + " from "
        + "   dbo.p_special_proc "
        + " where "
        + "   dbo.p_special_proc.sprotype_id = 'emy84' "
        + "   and dbo.p_special_proc.sprostatus_id != '$S/O' "
        + "   and dbo.p_special_proc.order_date > dateadd(day, -30, getdate()) "
        + " order by "
        + "   2";

        PreparedStatement pstmt = conn.prepareStatement(SQL);
        ResultSet rs = pstmt.executeQuery();
        while(rs.next()) {
            sNaPShotProcedures.add(new SNaPShotProcedure(rs));
        }
        rs.close();
        pstmt.close();

        return sNaPShotProcedures;

    }
    
}
