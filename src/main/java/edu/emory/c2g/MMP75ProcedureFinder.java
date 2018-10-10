package edu.emory.c2g;

import java.sql.*;  

public class MMP75ProcedureFinder {

    public Connection conn;
    
    public MMP75ProcedureFinder(Connection conn) {
        this.conn = conn;
    }
    
    public MMP75Procedure getBySpecimenId(String specimenId) throws SQLException {
        
        MMP75Procedure mMP75Procedure = null;
        
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
            mMP75Procedure = new MMP75Procedure(rs);
        }
        rs.close();
        pstmt.close();

        return mMP75Procedure;

    }
    
}
