#!/usr/bin/env bash

export COVERAGE=`awk -F"," '{ lines += $8 + $9; covered += $9 } \
    END { print ((lines + 0 != 0) ? 100*covered/lines :0) }' \
    build/reports/jacoco/jacocoRootTestReport/jacocoRootTestReport.csv`

echo "Code Coverage Pct: $COVERAGE %"

perl -E '$ENV{COVERAGE} >= 80 ? 0: \
    do { warn "Code coverage % ($ENV{COVERAGE}) is less than the required threshold: 80\n"; exit(1); }'