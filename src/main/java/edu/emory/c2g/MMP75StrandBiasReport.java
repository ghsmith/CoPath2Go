package edu.emory.c2g;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        public TsvRecord(Map<String, Integer> tsvColMap, String tsvLine) {
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
        public boolean isHighQuality() {
            if(
                (AO == null || AO.length() == 0 || Integer.valueOf(AO) >= 5)
                && (UAO == null || UAO.length() == 0 || Integer.valueOf(UAO) >= 3)
                && (gnomAD_AF == null || gnomAD_AF.length() == 0 || Float.valueOf(gnomAD_AF) <= 0.05f)
                && (AF == null || AF.length() == 0 || Float.valueOf(AF) >= 0.027f)
                && (HasSampleStrandBias == null || HasSampleStrandBias.length() == 0 || HasSampleStrandBias.equals("Yes"))
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
                Map<String, Integer> tsvColMap = new HashMap<>();
                int x = 0;
                for(String col : brTsv.readLine().split("\t")) {
                    tsvColMap.put(col, x++);
                }
                String tsvLine;
                while((tsvLine = brTsv.readLine()) != null) {
                    TsvRecord tsvRecord = new TsvRecord(tsvColMap, tsvLine);
                    if(tsvRecord.isHighQuality()) {
                        tsvRecords.add(tsvRecord);
                    }
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
            }
                
            System.out.println(String.format(""));
            System.out.println(String.format("Sample name: <b>%s</b> (med frag = %s / mean frag = %s)", sampleName, medianFragLength, meanFragLength));
            System.out.println(String.format("<table><tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>",
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
                System.out.println(String.format("<tr><td><a onclick='{ (new XMLHttpRequest()).(this.href); return false; }' href='%s'>[link]</a></td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>",
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
                System.out.println(String.format("<tr><td colspan='8'><i>none</i></td></tr>"));
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
