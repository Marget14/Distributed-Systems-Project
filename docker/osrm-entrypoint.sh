#!/bin/sh
set -eu

# Configurable, with defaults
OSM_PBF="${OSM_PBF:-/data/greece-latest.osm.pbf}"
OSRM_BASE="${OSRM_BASE:-/data/greece-latest.osrm}"
OSRM_PROFILE="${OSRM_PROFILE:-/opt/car.lua}"
OSRM_ALGO="${OSRM_ALGO:-ch}"
OSRM_MAX_TABLE_SIZE="${OSRM_MAX_TABLE_SIZE:-1000}"

mkdir -p /data

log() {
  printf '%s\n' "$*"
}

wait_for_file() {
  file="$1"
  seconds="${2:-300}"
  i=0
  while [ $i -lt "$seconds" ]; do
    if [ -s "$file" ]; then
      return 0
    fi
    i=$((i + 1))
    sleep 1
  done
  return 1
}

# OSRM generates multiple artifact files.
# We check for .properties AND a key algorithmic file to ensure a successful previous build.
data_seems_complete() {
  if [ ! -f "${OSRM_BASE}.properties" ]; then
    return 1
  fi

  # Basic checks for core files
  if [ ! -f "${OSRM_BASE}.osrm" ]; then
     return 1
  fi

  if [ "$OSRM_ALGO" = "ch" ]; then
    # Contraction Hierarchies specific
    if [ ! -f "${OSRM_BASE}.hsgr" ]; then return 1; fi
  else
    # Multi-Level Dijkstra specific
    if [ ! -f "${OSRM_BASE}.partition" ]; then return 1; fi
  fi
  return 0
}

if ! data_seems_complete; then
  if [ -f "${OSRM_BASE}.properties" ]; then
    log "Detected incomplete OSRM data. Cleaning up partial files..."
    rm -f "${OSRM_BASE}".* "${OSRM_BASE}"
  fi

  if ! wait_for_file "$OSM_PBF" 600; then
    log "Missing or empty OSM file after waiting: $OSM_PBF" 1>&2
    log "Hint: run the osrm-data-downloader service first (it writes to the shared osrm-data volume)." 1>&2
    exit 2
  fi

  log "Extracting..."
  osrm-extract -p "$OSRM_PROFILE" "$OSM_PBF"

  if [ "$OSRM_ALGO" = "ch" ]; then
    log "Contracting (CH)..."
    osrm-contract "$OSRM_BASE"
  else
    log "Partitioning (MLD)..."
    osrm-partition "$OSRM_BASE"
    log "Customizing (MLD)..."
    osrm-customize "$OSRM_BASE"
  fi

  log "OSRM preprocessing complete."
fi

log "Starting OSRM router..."
exec osrm-routed --algorithm "$OSRM_ALGO" --max-table-size "$OSRM_MAX_TABLE_SIZE" "$OSRM_BASE"
