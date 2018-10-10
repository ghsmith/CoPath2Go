package edu.emory.c2g;

import java.sql.*;

public class MMP75Procedure {

    public String specimenId;
    public Date dateOrdered;

    public MMP75Procedure() {
    }
    
    public MMP75Procedure(ResultSet rs) throws SQLException {
        this.specimenId = rs.getString("specimen_id");
        this.dateOrdered = rs.getDate("order_date");
    }
    
    @Override
    public String toString() {
        return "CMP26Procedure{" + "specimenId=" + specimenId + ", dateOrdered=" + dateOrdered + "}";
    }
    
}
