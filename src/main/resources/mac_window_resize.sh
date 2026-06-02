#!/bin/bash
set -euo pipefail

SCREEN_WIDTH=$(osascript -e 'tell application "Finder" to get bounds of window of desktop' | cut -d ',' -f 3 | tr -d ' ')
SCREEN_HEIGHT=$(osascript -e 'tell application "Finder" to get bounds of window of desktop' | cut -d ',' -f 4 | tr -d ' ')

SCREEN_WIDTH=${SCREEN_WIDTH:-1470}
SCREEN_HEIGHT=${SCREEN_HEIGHT:-956}

SCALE_FACTOR=$(osascript -e 'tell application "Main" to return backing scale factor of window 1' 2>/dev/null || echo "1.0")

if [[ "$SCALE_FACTOR" == "2.0" ]]; then
  DPI=8192
else
  DPI=16384
fi

# Configure Dimensions here
# Assumes that you have Boundless Window mod.
# For HiDPI you want a height of 8192, for LoDPI you want 16384
TALL_SIZE=(- - 384  "$DPI" )
BASE_SIZE=(0 0 "$SCREEN_WIDTH" "$SCREEN_HEIGHT")
THIN_SIZE=(- - 384 "$SCREEN_HEIGHT")
WIDE_SIZE=(- - "$SCREEN_WIDTH" 300)
# PrismLauncher or MultiMC, you can do any specific instance for another launcher if you locate "boundless_port.txt"
# By default this will target the active instance in MultiMC/Prism, and not work with multiple open instances.
LAUNCHER=PrismLauncher
BOUNDLESS_PORT_FILE=(~/Library/Application\ Support/"$LAUNCHER"/instances/*/natives/../minecraft/boundless_port.txt)
# Uncomment for vanilla launcher:
# BOUNDLESS_PORT_FILE=(~/Library/Application\ Support/minecraft/boundless_port.txt)

usage() {
  cat <<EOF
Usage: $0 [options]

Options:
  -b, --base     Set the window to the BASE size
  -t, --target   Set the window to the TARGET size
  -n, --thin     Set the window to the THIN size
  -w, --wide     Set the window to the WIDE size
  -h, --help     Show this help message

If no option is provided the script will toggle between BASE and TARGET based on current size.
EOF
  exit 1
}

# Parse arguments
CHOICE="auto"
while [[ $# -gt 0 ]]; do
  case "$1" in
    -b|--base)
      CHOICE="base"; shift ;;
    -t|--target)
      CHOICE="tall"; shift ;;
    -n|--thin)
      CHOICE="thin"; shift ;;
    -w|--wide)
      CHOICE="wide"; shift ;;
    -h|--help)
      usage ;;
    *)
      echo "Unknown arg: $1" >&2
      usage ;;
  esac
done

# script, you should not edit this.
BOUNDLESS_PORT="$(cat "$BOUNDLESS_PORT_FILE")"
CURR_SIZE=($(echo get | nc localhost "$BOUNDLESS_PORT"))

# Determine which size to apply: explicit choice or toggle
if [ "${CHOICE-}" = "base" ]; then
  SELECTED_SIZE=("${BASE_SIZE[@]}")
elif [ "${CHOICE-}" = "tall" ]; then
  SELECTED_SIZE=("${TALL_SIZE[@]}")
elif [ "${CHOICE-}" = "thin" ]; then
  SELECTED_SIZE=("${THIN_SIZE[@]}")
elif [ "${CHOICE-}" = "wide" ]; then
  SELECTED_SIZE=("${WIDE_SIZE[@]}")
else
  if [ "${CURR_SIZE[3]}" -eq "${TALL_SIZE[3]}" ]; then
    SELECTED_SIZE=("${BASE_SIZE[@]}")
  else
    SELECTED_SIZE=("${TALL_SIZE[@]}")
  fi
fi

echo set "${SELECTED_SIZE[@]}" | nc localhost "$BOUNDLESS_PORT" >/dev/null