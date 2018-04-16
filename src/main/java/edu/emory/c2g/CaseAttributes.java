package edu.emory.c2g;

import java.sql.*;

public class CaseAttributes {

    public String mrn;
    public Date dob;
    public String gender;
    public String lastName;
    public String firstName;
    public String middleName;
    public Date dateCollected;
    public Date dateAccessioned;
    public String orderingProviderLastName;
    public String orderingProviderFirstName;
    public String client;

    public CaseAttributes(ResultSet rs) throws SQLException {
       this.mrn = rs.getString("medrec_num");
       this.dob = rs.getDate("date_of_birth");
       this.gender = rs.getString("gender");
       this.lastName = rs.getString("lastName");
       this.firstName = rs.getString("firstName");
       this.middleName = rs.getString("middleName");
       this.dateAccessioned = rs.getDate("accession_date");
       this.dateCollected = rs.getDate("datetime_taken");
       this.orderingProviderLastName = rs.getString("ordering_provider_lastname");
       this.orderingProviderFirstName = rs.getString("ordering_provider_firstname");
       this.client = rs.getString("client");
    }
    
    @Override
    public String toString() {
        return "CaseAttributes{" + "mrn=" + mrn + ", lastName=" + lastName + ", firstName=" + firstName + ", dob=" + dob + ", gender=" + gender + ", client=" + client + ", dateCollected=" + dateCollected + ", dateAccessioned=" + dateAccessioned + ", orderingProviderLastName=" + orderingProviderLastName + ", orderingProviderFirstName=" + orderingProviderFirstName + '}';
    }
    
}
