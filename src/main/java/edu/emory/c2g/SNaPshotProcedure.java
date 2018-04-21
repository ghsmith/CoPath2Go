package edu.emory.c2g;

import java.sql.*;

public class SNaPshotProcedure {

    public String specimenId;
    public Date dateOrdered;

    public SNaPshotProcedure(ResultSet rs) throws SQLException {
        this.specimenId = rs.getString("specimen_id");
        this.dateOrdered = rs.getDate("order_date");
    }
    
    @Override
    public String toString() {
        return "SNaPshotProcedure{" + "specimenId=" + specimenId + ", dateOrdered=" + dateOrdered + '}';
    }
    
}
