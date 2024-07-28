#!/bin/bash

# Wait for the desktop environment to fully load
sleep 15

# Function to update resolutions
update_resolutions() {
    for output in $(xrandr | grep " connected" | cut -f1 -d " ")
    do
        xrandr --output "$output" --auto
    done
}

# Ensure the display is initialized
export DISPLAY=:0

# Initial update
update_resolutions

# Monitor for changes
kscreen-console monitor | while read -r line
do
    if echo "$line" | grep -q "Priorities changed"; then
        update_resolutions
    fi
done