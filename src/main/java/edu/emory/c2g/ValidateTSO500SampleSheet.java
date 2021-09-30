package edu.emory.c2g;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValidateTSO500SampleSheet {

    public static class SampleSheet {
        
        String readsSectionUnparsed;
        String settingsSectionUnparsed;
        String dataSectionHeaderUnparsed;
        String[] dataSectionHeaders;
        List<Map<String, String>> dataSectionRows;
        String adapterR1;
        String adapterR2;
        
        public SampleSheet(BufferedReader br) throws IOException {
            String line;
            while((line = br.readLine()) != null) {
                if(line.startsWith("[Reads]")) {
                    readsSectionUnparsed += line;
                    while(!(line = br.readLine()).startsWith("[Settings]")) {
                        readsSectionUnparsed += line;
                    }
                }
                if(line.startsWith("[Settings]")) {
                    settingsSectionUnparsed += line;
                    while(!(line = br.readLine()).startsWith("[Data]")) {
                        settingsSectionUnparsed += line;
                        if(line.startsWith("Adapter,") || line.startsWith("AdapterRead1")) {
                            adapterR1 = line.split(",")[1];
                        }
                        if(line.startsWith("AdapterRead2,")) {
                            adapterR2 = line.split(",")[1];
                        }
                    }
                }
                if(line.startsWith("[Data]")) {
                    dataSectionHeaderUnparsed += line;
                    line = br.readLine();
                    dataSectionHeaderUnparsed += line;
                    dataSectionHeaders = line.split(",");
                    dataSectionRows = new ArrayList<>();
                    while((line = br.readLine()) != null) {
                        Map<String, String> dataSectionRow = new HashMap<>();
                        dataSectionRows.add(dataSectionRow);
                        String[] vals = line.split(",");
                        for(int x = 0; x < vals.length; x++) {
                            dataSectionRow.put(dataSectionHeaders[x], vals[x]);
                        }
                    }
                }
            }
        }
        
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {

        Boolean validationFailure = null;
        
        BufferedReader brTemplate = new BufferedReader(new FileReader(args[0]));
        BufferedReader brBaseSpace = new BufferedReader(new FileReader(args[1]));
        BufferedReader brLocalApp = new BufferedReader(new FileReader(args[2]));
        
        SampleSheet ssTemplate = new SampleSheet(brTemplate);
        SampleSheet ssBaseSpace = new SampleSheet(brBaseSpace);
        SampleSheet ssLocalApp = new SampleSheet(brLocalApp);

        Map<String, String[]> indicesByUpNo = new HashMap<>();
        for(Map<String, String> dataSectionRow : ssTemplate.dataSectionRows) {
            indicesByUpNo.put(dataSectionRow.get("Index_ID"), new String[] { dataSectionRow.get("index"), dataSectionRow.get("index2") });
        }
        
        System.out.println(String.format("Template : %s", args[0]));
        System.out.println(String.format("BaseSpace: %s", args[1]));
        System.out.println(String.format("LocalApp : %s", args[2]));

        System.out.print("1. Template 'Reads' section == BaseSpace 'Reads' section: ");
        if(ssTemplate.readsSectionUnparsed.equals(ssBaseSpace.readsSectionUnparsed)) {
            System.out.println("YES");
        }
        else {
            System.out.println("NO");
            System.out.println("*** VALIDATION FAILS ***");
            validationFailure = true;
        }

        System.out.print("2. Template 'Reads' section == LocalApp 'Reads' section: ");
        if(ssTemplate.readsSectionUnparsed.equals(ssLocalApp.readsSectionUnparsed)) {
            System.out.println("YES");
        }
        else {
            System.out.println("NO");
            System.out.println("*** VALIDATION FAILS ***");
            validationFailure = true;
        }

        System.out.print("3. Template 'Settings' section == BaseSpace 'Settings' section: ");
        if(ssTemplate.settingsSectionUnparsed.equals(ssBaseSpace.settingsSectionUnparsed)) {
            System.out.println("YES");
            System.out.println("*** VALIDATION FAILS ***");
            validationFailure = true;
        }
        else {
            System.out.println("NO");
            System.out.print("... 3(a). Template 'AdapterRead1' == BaseSpace 'Adapter': ");
            if(ssTemplate.adapterR1.equals(ssBaseSpace.adapterR1)) {
                System.out.println("YES");
            }
            else {
                System.out.println("NO");
                System.out.println("*** VALIDATION FAILS ***");
                validationFailure = true;
            }
            System.out.print("... 3(b). Template 'AdapterRead2' == BaseSpace 'AdapterRead2': ");
            if(ssTemplate.adapterR2.equals(ssBaseSpace.adapterR2)) {
                System.out.println("YES");
            }
            else {
                System.out.println("NO");
                System.out.println("*** VALIDATION FAILS ***");
                validationFailure = true;
            }
        }

        System.out.print("4. Template 'Settings' section == LocalApp 'Settings' section: ");
        if(ssTemplate.settingsSectionUnparsed.equals(ssLocalApp.settingsSectionUnparsed)) {
            System.out.println("YES");
        }
        else {
            System.out.println("NO");
            System.out.println("*** VALIDATION FAILS ***");
            validationFailure = true;
        }

        System.out.print("5. Template 'Data' section header == BaseSpace 'Data' section header: ");
        if(ssTemplate.dataSectionHeaderUnparsed.equals(ssBaseSpace.dataSectionHeaderUnparsed)) {
            System.out.println("YES");
            System.out.println("*** VALIDATION FAILS ***");
            validationFailure = true;
        }
        else {
            System.out.println("NO");
            for(Map<String, String> dataSectionRow : ssBaseSpace.dataSectionRows) {
                System.out.print(String.format("... 5(a) [%-30s] BaseSpace 'Sample_ID' == 'Sample_Name': ", dataSectionRow.get("Sample_ID")));
                if(dataSectionRow.get("Sample_ID").equals(dataSectionRow.get("Sample_Name"))) {
                    System.out.println("YES");
                }
                else {
                    System.out.println("NO");
                    System.out.println("*** VALIDATION FAILS ***");
                    validationFailure = true;
                }
            }
            for(Map<String, String> dataSectionRow : ssBaseSpace.dataSectionRows) {
                System.out.print(String.format("... 5(b) [%-30s] BaseSpace UP number and index sequences are concordant with Template: ", dataSectionRow.get("Sample_ID")));
                if(
                    dataSectionRow.get("index").equals(indicesByUpNo.get(dataSectionRow.get("Index_ID"))[0])
                    && dataSectionRow.get("index2").equals(indicesByUpNo.get(dataSectionRow.get("Index_ID"))[1])
                ) {
                    System.out.println("YES");
                }
                else {
                    System.out.println("NO");
                    System.out.println("*** VALIDATION FAILS ***");
                    validationFailure = true;
                }
            }
        }
        
        System.out.print("6. Template 'Data' section header == LocalApp 'Data' section header: ");
        if(ssTemplate.dataSectionHeaderUnparsed.equals(ssLocalApp.dataSectionHeaderUnparsed)) {
            System.out.println("YES");
            for(Map<String, String> dataSectionRow : ssLocalApp.dataSectionRows) {
                System.out.print(String.format("... 6(a) [%-30s] LocalApp 'Sample_ID' == 'Pair_ID': ", dataSectionRow.get("Sample_ID")));
                if(dataSectionRow.get("Sample_ID").equals(dataSectionRow.get("Pair_ID"))) {
                    System.out.println("YES");
                }
                else {
                    System.out.println("NO");
                    System.out.println("*** VALIDATION FAILS ***");
                    validationFailure = true;
                }
            }
            for(Map<String, String> dataSectionRow : ssLocalApp.dataSectionRows) {
                System.out.print(String.format("... 6(a) [%-30s] LocalApp UP number and index sequences are concordant with Template: ", dataSectionRow.get("Sample_ID")));
                if(
                    dataSectionRow.get("index").equals(indicesByUpNo.get(dataSectionRow.get("Index_ID"))[0])
                    && dataSectionRow.get("index2").equals(indicesByUpNo.get(dataSectionRow.get("Index_ID"))[1])
                ) {
                    System.out.println("YES");
                }
                else {
                    System.out.println("NO");
                    System.out.println("*** VALIDATION FAILS ***");
                    validationFailure = true;
                }
            }
        }
        else {
            System.out.println("NO");
            System.out.println("*** VALIDATION FAILS ***");
            validationFailure = true;
        }

        for(Map<String, String> dataSectionRow : ssBaseSpace.dataSectionRows) {
            System.out.print(String.format("7. [%-30s] BaseSpace 'Data' section concordant with LocalApp 'Data' section: ", dataSectionRow.get("Sample_ID")));
            long matchCount = ssLocalApp.dataSectionRows.stream().filter(a -> a.get("Sample_ID").equals(dataSectionRow.get("Sample_ID"))).count();
            if(matchCount == 1) {
                Map<String, String> matchedDataSectionRow = ssLocalApp.dataSectionRows.stream().filter(a -> a.get("Sample_ID").equals(dataSectionRow.get("Sample_ID"))).findFirst().get();
                if(
                    dataSectionRow.get("Index_ID").equals(matchedDataSectionRow.get("Index_ID"))
                    && dataSectionRow.get("index").equals(matchedDataSectionRow.get("index"))
                    && dataSectionRow.get("index2").equals(matchedDataSectionRow.get("index2"))
                ) {
                    System.out.println("YES");
                }
                else {
                    System.out.println("NO");
                    System.out.println("*** VALIDATION FAILS ***");
                    validationFailure = true;
                }
            }
            else {
                System.out.println("NO");
                System.out.println("*** VALIDATION FAILS ***");
                validationFailure = true;
            }
        }
        
        for(Map<String, String> dataSectionRow : ssLocalApp.dataSectionRows) {
            System.out.print(String.format("8. [%-30s] LocalApp 'Data' section concordant with BaseSpace 'Data' section: ", dataSectionRow.get("Sample_ID")));
            long matchCount = ssBaseSpace.dataSectionRows.stream().filter(a -> a.get("Sample_ID").equals(dataSectionRow.get("Sample_ID"))).count();
            if(matchCount == 1) {
                Map<String, String> matchedDataSectionRow = ssBaseSpace.dataSectionRows.stream().filter(a -> a.get("Sample_ID").equals(dataSectionRow.get("Sample_ID"))).findFirst().get();
                if(
                    dataSectionRow.get("Index_ID").equals(matchedDataSectionRow.get("Index_ID"))
                    && dataSectionRow.get("index").equals(matchedDataSectionRow.get("index"))
                    && dataSectionRow.get("index2").equals(matchedDataSectionRow.get("index2"))
                ) {
                    System.out.println("YES");
                }
                else {
                    System.out.println("NO");
                    System.out.println("*** VALIDATION FAILS ***");
                    validationFailure = true;
                }
            }
            else {
                System.out.println("NO");
                System.out.println("*** VALIDATION FAILS ***");
                validationFailure = true;
            }
        }
        
        if(validationFailure != null && validationFailure) {
            System.exit(1);
        }
        
    }
    
}
