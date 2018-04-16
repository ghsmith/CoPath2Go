package edu.emory.c2g;

import java.sql.*;

public class SNaPShotProcedure {

    public String specimenId;
    public Date dateOrdered;

    public SNaPShotProcedure(ResultSet rs) throws SQLException {
       this.specimenId = rs.getString("specimen_id");
       this.dateOrdered = rs.getDate("order_date");
    }
    
    @Override
    public String toString() {
        return "SNaPShotProcedure{" + "specimenId=" + specimenId + ", dateOrdered=" + dateOrdered + '}';
    }
    
}
