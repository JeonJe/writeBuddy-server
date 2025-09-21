#!/bin/bash

echo "=== Testing SYNC endpoint (기존 방식) ==="
echo "Start time: $(date)"

curl -X POST http://localhost:7071/corrections \
  -H "Content-Type: application/json" \
  -d '{"originSentence": "I am go to the store yesterday and buy some food"}' \
  -w "\nTime: %{time_total}s\n"

echo "End time: $(date)"