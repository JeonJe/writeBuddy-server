#!/bin/bash

echo "=== Testing ASYNC endpoint (비동기 방식) ==="
echo "Start time: $(date)"

curl -X POST http://localhost:7071/corrections/async \
  -H "Content-Type: application/json" \
  -d '{"originSentence": "I am go to the store yesterday and buy some food"}' \
  -w "\nTime: %{time_total}s\n"

echo "End time: $(date)"