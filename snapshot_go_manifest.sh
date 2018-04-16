#!/bin/bash
# This script must be run in the directory that contains the "base" SNaPShot.tsv file.
# example usage: ./snapshot_go_manifest.sh SampleSheet.csv > GoManifest.tsv
# GHS 4/16/2018

cat SNaPShot.tsv | java -cp /u01/cp26/scripts/uber-CoPath2Go-1.0-SNAPSHOT.jar edu.emory.c2g.AddPendingCasesToSNaPShotGoManifest
