package edu.emory.c2g;

import java.sql.*;  

public class CaseAttributesFinder {

    public Connection conn;
    
    public CaseAttributesFinder(Connection conn) {
        this.conn = conn;
    }
    
    public CaseAttributes getByAccessionNumber(String accessionNumber) throws SQLException {

        CaseAttributes caseAttributes = null;
        
        // @todo This is may return the wrong dates for a multipart specimen.
        //       Note the "top 1" restriction. Since we don't have easy access
        //       to the part that is the subject of the molecular testing and
        //       the collection dates for the parts are generally the same,
        //       this doesn't seem like a big deal to me. -GHS 4/11/18
        String SQL =
          " select top 1 "
        + "   dbo.c_specimen.specnum_formatted, "
        + "   dbo.r_medrec.medrec_num, "
        + "   dbo.r_pat_demograph.date_of_birth, "
        + "   dbo.r_pat_demograph.gender, "
        + "   dbo.r_pat_demograph.lastname, "
        + "   dbo.r_pat_demograph.firstname, "
        + "   dbo.r_pat_demograph.middlename, "
        + "   dbo.c_specimen.accession_date, "
        + "   dbo.p_part.datetime_taken, "
        + "   dbo.c_d_person.lastname ordering_provider_lastname, "
        + "   dbo.c_d_person.firstname ordering_provider_firstname, " 
        + "   dbo.c_d_person.middlename, "
        + "   dbo.c_d_client.name client "
        + " from "
        + "   dbo.c_specimen "
        + "     left outer join dbo.r_encounter on dbo.c_specimen.encounter_id = dbo.r_encounter.encounter_id "
        + "     left outer join dbo.c_d_person on dbo.c_specimen.ord_person_id = dbo.c_d_person.id "
        + "     left outer join dbo.c_d_location on dbo.c_specimen.patloc_atacc_id = dbo.c_d_location.id, "
        + "   dbo.r_pat_demograph, "
        + "   dbo.c_d_specpriority, "
        + "   dbo.c_d_specclass, "
        + "   dbo.p_part, "
        + "   dbo.r_medrec, "
        + "   dbo.c_d_client "
        + " where "
        + "   dbo.c_specimen.specimen_id = dbo.p_part.specimen_id "
        + "   and dbo.c_specimen.patdemog_id = dbo.r_pat_demograph.patdemog_id "
        + "   and dbo.c_specimen.specpriority_id = dbo.c_d_specpriority.id "
        + "   and dbo.c_d_specclass.id = dbo.c_specimen.specclass_id "
        + "   and dbo.c_specimen.specimen_id  = dbo.p_part.specimen_id "
        + "   and dbo.r_medrec.client_id = dbo.c_specimen.client_id "
        + "   and dbo.r_medrec.patdemog_id = dbo.r_pat_demograph.patdemog_id "
        + "   and dbo.c_d_client.id = dbo.c_specimen.client_id "
        + "   and dbo.c_specimen.specnum_formatted = ? ";

        PreparedStatement pstmt = conn.prepareStatement(SQL);
        pstmt.setString(1, accessionNumber);
        ResultSet rs = pstmt.executeQuery();
        if(rs.next()) {
            caseAttributes = new CaseAttributes(rs);
        }
        rs.close();
        pstmt.close();

        return caseAttributes;

    }
    
    public CaseAttributes getBySpecimenId(String specimenId) throws SQLException {

        CaseAttributes caseAttributes = null;
        
        // @todo This is may return the wrong dates for a multipart specimen.
        //       Note the "top 1" restriction. Since we don't have easy access
        //       to the part that is the subject of the molecular testing and
        //       the collection dates for the parts are generally the same,
        //       this doesn't seem like a big deal to me. -GHS 4/11/18
        String SQL =
          " select top 1 "
        + "   dbo.c_specimen.specnum_formatted, "
        + "   dbo.r_medrec.medrec_num, "
        + "   dbo.r_pat_demograph.date_of_birth, "
        + "   dbo.r_pat_demograph.gender, "
        + "   dbo.r_pat_demograph.lastname, "
        + "   dbo.r_pat_demograph.firstname, "
        + "   dbo.r_pat_demograph.middlename, "
        + "   dbo.c_specimen.accession_date, "
        + "   dbo.p_part.datetime_taken, "
        + "   dbo.c_d_person.lastname ordering_provider_lastname, "
        + "   dbo.c_d_person.firstname ordering_provider_firstname, " 
        + "   dbo.c_d_person.middlename, "
        + "   dbo.c_d_client.name client "
        + " from "
        + "   dbo.c_specimen "
        + "     left outer join dbo.r_encounter on dbo.c_specimen.encounter_id = dbo.r_encounter.encounter_id "
        + "     left outer join dbo.c_d_person on dbo.c_specimen.ord_person_id = dbo.c_d_person.id "
        + "     left outer join dbo.c_d_location on dbo.c_specimen.patloc_atacc_id = dbo.c_d_location.id, "
        + "   dbo.r_pat_demograph, "
        + "   dbo.c_d_specpriority, "
        + "   dbo.c_d_specclass, "
        + "   dbo.p_part, "
        + "   dbo.r_medrec, "
        + "   dbo.c_d_client "
        + " where "
        + "   dbo.c_specimen.specimen_id = dbo.p_part.specimen_id "
        + "   and dbo.c_specimen.patdemog_id = dbo.r_pat_demograph.patdemog_id "
        + "   and dbo.c_specimen.specpriority_id = dbo.c_d_specpriority.id "
        + "   and dbo.c_d_specclass.id = dbo.c_specimen.specclass_id "
        + "   and dbo.c_specimen.specimen_id  = dbo.p_part.specimen_id "
        + "   and dbo.r_medrec.client_id = dbo.c_specimen.client_id "
        + "   and dbo.r_medrec.patdemog_id = dbo.r_pat_demograph.patdemog_id "
        + "   and dbo.c_d_client.id = dbo.c_specimen.client_id "
        + "   and dbo.c_specimen.specimen_id = ? ";

        PreparedStatement pstmt = conn.prepareStatement(SQL);
        pstmt.setString(1, specimenId);
        ResultSet rs = pstmt.executeQuery();
        if(rs.next()) {
            caseAttributes = new CaseAttributes(rs);
        }
        rs.close();
        pstmt.close();

        return caseAttributes;

    }

}
