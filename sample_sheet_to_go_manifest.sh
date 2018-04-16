#!/bin/bash
# This script must be run in the Illumina run directory that contains the sample sheet.
# example usage: ./sample_sheet_to_go_manifest.sh SampleSheet.csv > GoManifest.tsv
# GHS 4/9/2018

TS=`date +%Y%m%d%H%M`

platform="unknown"
if [[ `pwd` = *"M01382"* ]]; then
  platform="MiSeq"
fi
if [[ `pwd` = *"NS500796"* ]]; then
  platform="NextSeq"
fi

gawk \
-v TS="${TS}" \
-v platform="${platform}" \
-v go_mount="/mount/illumina_runs01" \
-v illumina_run_id=`basename \`pwd\`` \
-f <(cat - <<-'_EOF_'
BEGIN {
  FS=","
  OFS="\t"
  in_data_block="false"
  last_sample_id=""
  print "runs"
  print "run_id", "platform", "run_type", "run_data_location"
  print illumina_run_id "_" TS, platform, "Default", go_mount "/" illumina_run_id
  print "samples"
  print "run_id", "sample_category", "sample_id", "stabilization", "order_id", "order_date", "test", "disease_name", "mrn", "first_name", "last_name", "middle_initial", "gender", "dob", "ordering_physician", "ordering_physician_suffix", "ordering_physician_institute", "diagnosis", "specimen_type", "specimen_collected", "specimen_received", "emory_run_id", "emory_order_id"
}
in_data_block ~ /true/ {
  if(last_sample_id != $2) {
    print illumina_run_id "_" TS, "Patient Sample", $2, "default", $2 "_" TS, "", "Cancer Mutation Panel 26 (" platform ")", "Tumor of Unknown Origin", "", "", "", "", "", "", "", "", "", "", "", "", "", illumina_run_id, $2
  }
  last_sample_id=$2
}
/\[Data\]/ {
  in_data_block="true"
  getline
}
_EOF_
) ${1} \
| java -cp /u01/cp26/scripts/uber-CoPath2Go-1.0-SNAPSHOT.jar edu.emory.c2g.UpdateDemographicsInCmp26GoManifest
