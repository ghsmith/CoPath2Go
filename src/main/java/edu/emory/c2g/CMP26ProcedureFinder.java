package edu.emory.c2g;

import java.sql.*;  

public class CMP26ProcedureFinder {

    public Connection conn;
    
    public CMP26ProcedureFinder(Connection conn) {
        this.conn = conn;
    }
    
    public CMP26Procedure getBySpecimenId(String specimenId) throws SQLException {
        
        CMP26Procedure cMP26Procedure = null;
        
        String SQL =
          " select top 1"
        + "   dbo.p_special_proc.specimen_id, "
        + "   dbo.p_special_proc.order_date "
        + " from "
        + "   dbo.p_special_proc "
        + " where "
        + "   dbo.p_special_proc.specimen_id = ? "
        + "   and dbo.p_special_proc.sprotype_id = 'emy45' "
        + " order by "
        + "   2";

        PreparedStatement pstmt = conn.prepareStatement(SQL);
        pstmt.setString(1, specimenId);
        ResultSet rs = pstmt.executeQuery();
        if(rs.next()) {
            cMP26Procedure = new CMP26Procedure(rs);
        }
        rs.close();
        pstmt.close();

        return cMP26Procedure;

    }
    
}
