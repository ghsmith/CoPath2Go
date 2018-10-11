package edu.emory.c2g;

import java.sql.*;

public class CaseAttributes {

    public String specimenId;
    public String accessionNumber;
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
    public String empi;

    public CaseAttributes() {
    }
    
    public CaseAttributes(ResultSet rs) throws SQLException {
        this.specimenId = rs.getString("specimen_id");
        this.accessionNumber = rs.getString("specnum_formatted");
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
        this.empi = rs.getString("empi");
    }

    @Override
    public String toString() {
        return "CaseAttributes{" + "specimenId=" + specimenId + ", accessionNumber=" + accessionNumber + ", mrn=" + mrn + ", dob=" + dob + ", gender=" + gender + ", lastName=" + lastName + ", firstName=" + firstName + ", middleName=" + middleName + ", dateCollected=" + dateCollected + ", dateAccessioned=" + dateAccessioned + ", orderingProviderLastName=" + orderingProviderLastName + ", orderingProviderFirstName=" + orderingProviderFirstName + ", client=" + client + ", empi=" + empi + "}";
    }
        
}
