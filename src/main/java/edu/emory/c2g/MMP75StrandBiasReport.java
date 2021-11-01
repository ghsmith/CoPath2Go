package edu.emory.c2g;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MMP75StrandBiasReport {

    public static SimpleDateFormat sdf1 = new SimpleDateFormat("MM/dd/yyyy kk:mm");
    
    public static String[] consequenceFilters = {
"5_prime_UTR_variant",
"coding_sequence_variant",
"feature_elongation",
"feature_truncation",
"frameshift_variant",
"incomplete_terminal_codon_variant",
"inframe_deletion",
"inframe_insertion",
"missense_variant",
"protein_altering_variant",
"splice_acceptor_variant",
"splice_donor_variant",
"splice_region_Variant",
"start_lost",
"stop_gained",
"stop_list",
"transcript_ablation",
"transcript_amplification"
    };
    
    public static class TsvRecord {
        int lineNo;
        String chromosome;
        String position;
        String PreferredSymbol;
        String reference;
        String mutation;
        String AO;
        String UAO;
        String AF;
        String gnomAD_AF;
        String consequence;
        String HasSampleStrandBias;
        String HasSeqDirBias;
        public TsvRecord(Map<String, Integer> tsvColMap, String tsvLine, int lineNo) {
            this.lineNo = lineNo;
            String[] fields = tsvLine.split("\t");
            chromosome = fields[tsvColMap.get("chromosome")];
            position = fields[tsvColMap.get("position")];
            PreferredSymbol = fields[tsvColMap.get("PreferredSymbol")];
            reference = fields[tsvColMap.get("reference")];
            mutation = fields[tsvColMap.get("mutation")];
            AO = fields[tsvColMap.get("AO")];
            UAO = fields[tsvColMap.get("UAO")];
            AF = fields[tsvColMap.get("AF")];
            gnomAD_AF = fields[tsvColMap.get("gnomAD_AF")];
            consequence = fields[tsvColMap.get("consequence")];
            HasSampleStrandBias = fields[tsvColMap.get("HasSampleStrandBias")];
            HasSeqDirBias = fields[tsvColMap.get("HasSeqDirBias")];
        }
        public boolean isStrandBiasHighQuality() {
            if(
                (HasSampleStrandBias != null && HasSampleStrandBias.length() > 0 && HasSampleStrandBias.equals("Yes"))
                && (AO == null || AO.length() == 0 || Integer.valueOf(AO) >= 5)
                && (UAO == null || UAO.length() == 0 || Integer.valueOf(UAO) >= 3)
                && (gnomAD_AF == null || gnomAD_AF.length() == 0 || Float.valueOf(gnomAD_AF) <= 0.05f)
                && (AF == null || AF.length() == 0 || Float.valueOf(AF) >= 0.027f)
                && (HasSeqDirBias == null || HasSeqDirBias.length() == 0 || !HasSeqDirBias.equals("Yes"))
            ) {
                for(String consequenceFilter : consequenceFilters) {
                    if(consequence == null || consequence.contains(consequenceFilter)) {
                        return true;
                    }
                }
            }      
            return false;
        }
        public String getUniqueConsequences() {
            List<String> uniqueConsequences = new ArrayList<>();
            for(String consequenceItem : consequence.split("\\|")) {
                if(!uniqueConsequences.contains(consequenceItem)) {
                    uniqueConsequences.add(consequenceItem);
                }
            }
            return uniqueConsequences.toString();
        }
    }
    
    public static void main(String[] args) throws FileNotFoundException, IOException {
        
        String runName = args[0];
        Boolean addAsterisk = args.length >=2 && args[1].equals("add-asterisk");
        
        BufferedReader brIn = new BufferedReader(new InputStreamReader(System.in));        

        System.out.println("<html></head><style>table { border-collapse: collapse; border:1px solid black; } table td{ border:1px solid black; }</style></head><body><pre>");
        System.out.println(String.format("MMP75 fragment length and strand bias QC report"));
        System.out.println(String.format("Run name: %s", runName));
        System.out.println(String.format("Date generated: %s", sdf1.format(new Date())));
        System.out.println(String.format(""));
        System.out.println(String.format("1. Archer strand bias calls becomes less reliable with lower fragment sizes (ie, closer to 150 bp)."));
        System.out.println(String.format(""));
        System.out.println(String.format("2. The variants in this report have Archer strand bias calls but are otherwise high quality:"));
        System.out.println(String.format("  AO >= 5 (or absent)"));
        System.out.println(String.format("  UAO >= 3 (or absent)"));
        System.out.println(String.format("  gnomAD_AF <= 0.05 (or absent)"));
        System.out.println(String.format("  Consequence like 5_prime_UTR_variant, coding_sequence_variant, ... (or absent) [see Note 1]"));
        System.out.println(String.format("  AF >= 0.027 (or absent)"));
        System.out.println(String.format("  HasSeqDirBias is not Yes (or absent)"));
        System.out.println(String.format(""));
        System.out.println(String.format("3. Start IGV on your PC before clicking the IGV links."));
        
        String sampleName;
        int sampleNo = 0;
        
        while((sampleName = brIn.readLine()) != null) {
            
            sampleNo++;
            
            List<TsvRecord> tsvRecords = new ArrayList<>();
            {
                BufferedReader brTsv = new BufferedReader(new FileReader(sampleName + ".vcf.summary.tsv"));
                PrintStream brTsvOut = null;
                if(addAsterisk) {
                    brTsvOut = new PrintStream(new FileOutputStream(sampleName + ".vcf.summary.tsv.001"));
                }
                Map<String, Integer> tsvColMap = new HashMap<>();
                int x = 0;
                String colLine = brTsv.readLine();
                if(addAsterisk) {
                    brTsvOut.println(colLine);
                }
                for(String col : colLine.split("\t")) {
                    tsvColMap.put(col, x++);
                }
                if(!(
                    tsvColMap.get("chromosome") == 2
                    && tsvColMap.get("position") == 3
                    && tsvColMap.get("PreferredSymbol") == 51
                    && tsvColMap.get("reference") == 4
                    && tsvColMap.get("mutation") == 5
                    && tsvColMap.get("AO") == 10
                    && tsvColMap.get("UAO") == 14
                    && tsvColMap.get("AF") == 11
                    && tsvColMap.get("gnomAD_AF") == 73
                    && tsvColMap.get("consequence") == 58
                    && tsvColMap.get("HasSampleStrandBias") == 33
                    && tsvColMap.get("HasSeqDirBias") == 40
                )) {
                    throw new RuntimeException("error - fields numbers not as expected");
                }
                int lineNo = 1;
                String tsvLine;
                while((tsvLine = brTsv.readLine()) != null) {
                    lineNo++;
                    TsvRecord tsvRecord = new TsvRecord(tsvColMap, tsvLine, lineNo);
                    if(tsvRecord.isStrandBiasHighQuality()) {
                        tsvRecords.add(tsvRecord);
                        if(addAsterisk) {
                            String[] fields = tsvLine.split("\t");
                            for(int fieldNo = 0; fieldNo < fields.length; fieldNo++) {
                                if(fieldNo > 0) {
                                    brTsvOut.print("\t");
                                }
                                if(fieldNo == tsvColMap.get("HasSampleStrandBias")) {
                                    if(!fields[fieldNo].equals("Yes")) {
                                        throw new RuntimeException("error - HasSampleStrandBias not as expected");
                                    }
                                    brTsvOut.print("Yes*");
                                }
                                else {
                                    brTsvOut.print(fields[fieldNo]);
                                }
                            }
                            brTsvOut.println();
                        }
                    }
                    else {
                        if(addAsterisk) {
                            brTsvOut.println(tsvLine);
                        }
                    }
                }
                brTsv.close();
                if(addAsterisk) {
                    brTsvOut.close();
                }
            }

            String meanFragLength = null;
            String medianFragLength = null;
            {
                BufferedReader brFullResults = new BufferedReader(new FileReader(sampleName + "_full_results.txt"));
                String frLine;
                while((frLine = brFullResults.readLine()) != null) {
                    if(frLine.startsWith("UNIQUE_DNA_FRAGMENT_MEAN_LENGTH")) {
                        meanFragLength = frLine.split("\t")[1];
                    }
                    if(frLine.startsWith("UNIQUE_DNA_FRAGMENT_MEDIAN_LENGTH")) {
                        medianFragLength = frLine.split("\t")[1];
                    }
                }
                brFullResults.close();
            }

            System.out.println(String.format(""));
            System.out.println(String.format("Sample name: <b>%s</b> (med frag = %s / mean frag = %s)", sampleName, medianFragLength, meanFragLength));
            System.out.println(String.format("<table><tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>",
                "Line No",
                "IGV",
                "Chr",
                "Pos",
                "Symbol",
                "Ref",
                "Mut",
                "AF",
                "Consequence (for all transcripts)"
            ));
            for(TsvRecord tsvRecord : tsvRecords) {
                System.out.println(String.format("<tr><td>%s</td><td><a onclick='{ x = new XMLHttpRequest(); x.open(\"GET\", this.href); x.send(); return false; }' href='%s'>[link]</a></td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>",
                    tsvRecord.lineNo,
                    String.format("http://127.0.0.1:60151/load?file=https://patheuhmollabserv2.eushc.org/illumina_runs01/%s/Data/Intensities/BaseCalls/Archer_Run/%s.freebayes.ann.vcf.gz,https://patheuhmollabserv2.eushc.org/illumina_runs01/%s/Data/Intensities/BaseCalls/Archer_Run/%s.lofreq.ann.vcf.gz,https://patheuhmollabserv2.eushc.org/illumina_runs01/%s/Data/Intensities/BaseCalls/Archer_Run/%s.vision.ann.vcf.gz,https://patheuhmollabserv2.eushc.org/illumina_runs01/%s/Data/Intensities/BaseCalls/Archer_Run/%s.molbar.trimmed.deduped.merged.bam,https://patheuhmollabserv2.eushc.org/illumina_runs01/%s/Data/Intensities/BaseCalls/Archer_Run/VariantPlex_Myeloid_GSP5031.gtf&locus=%s:%s&genome=hg19&merge=false",
                         runName, sampleName,
                         runName, sampleName,
                         runName, sampleName,
                         runName, sampleName,
                         runName,
                         tsvRecord.chromosome, tsvRecord.position
                    ),
                    tsvRecord.chromosome,
                    tsvRecord.position,
                    tsvRecord.PreferredSymbol,
                    tsvRecord.reference,
                    tsvRecord.mutation,
                    tsvRecord.AF,
                    tsvRecord.getUniqueConsequences()
                ));
            }
            if(tsvRecords.size() == 0) {
                System.out.println(String.format("<tr><td colspan='9'><i>none</i></td></tr>"));
            }
            System.out.println(String.format("</table>"));
            
        }
        
        System.out.println(String.format("[Note 1] The full list of consequences includes:"));
        for(String consequenceFilter : consequenceFilters) {
            System.out.println(String.format("  %s", consequenceFilter));
        }
        
        System.out.println("</pre></body></html>");

    }
    
}
