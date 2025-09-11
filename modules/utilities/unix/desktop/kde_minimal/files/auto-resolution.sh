#!/bin/bash

# Wait for the desktop environment to fully load
sleep 15

# Function to get the best resolution for an output
get_best_resolution() {
    local output="$1"
    local preferred_mode=""
    local best_hd_mode=""
    local best_width=0
    local best_height=0
    
    # Get available modes for this output
    local modes=$(xrandr --query | grep -A 1000 "^$output connected" | grep -E "^\s+[0-9]+x[0-9]+" | head -20)
    
    while IFS= read -r mode_line; do
        # Extract resolution from mode line (format: "   1920x1080     60.00*+")
        if [[ $mode_line =~ ([0-9]+)x([0-9]+) ]]; then
            local width=${BASH_REMATCH[1]}
            local height=${BASH_REMATCH[2]}
            local mode="${width}x${height}"
            
            # Check if this is the preferred mode (marked with +)
            if echo "$mode_line" | grep -q "+"; then
                preferred_mode="$mode"
            fi
            
            # Check if this resolution is HD or lower (1920x1080 or smaller)
            if [[ $width -le 1920 && $height -le 1080 ]]; then
                # Check if this is better than our current best HD mode
                if [[ $width -gt $best_width || ($width -eq $best_width && $height -gt $best_height) ]]; then
                    best_width=$width
                    best_height=$height
                    best_hd_mode="$mode"
                fi
            fi
        fi
    done <<< "$modes"
    
    # Priority: 1) Preferred mode if HD or lower, 2) Best HD mode, 3) Preferred mode even if higher than HD
    if [[ -n "$preferred_mode" ]]; then
        # Check if preferred mode is HD or lower
        if [[ $preferred_mode =~ ([0-9]+)x([0-9]+) ]]; then
            local pref_width=${BASH_REMATCH[1]}
            local pref_height=${BASH_REMATCH[2]}
            if [[ $pref_width -le 1920 && $pref_height -le 1080 ]]; then
                echo "$preferred_mode"
                return
            fi
        fi
    fi
    
    # If preferred mode is higher than HD, use best HD mode instead
    if [[ -n "$best_hd_mode" ]]; then
        echo "$best_hd_mode"
        return
    fi
    
    # Fallback to preferred mode even if higher than HD
    if [[ -n "$preferred_mode" ]]; then
        echo "$preferred_mode"
        return
    fi
    
    # Last resort: return empty to trigger auto mode
    echo ""
}

# Function to update resolutions
update_resolutions() {
    for output in $(xrandr | grep " connected" | cut -f1 -d " ")
    do
        local best_resolution=$(get_best_resolution "$output")
        if [[ -n "$best_resolution" ]]; then
            echo "Setting $output to $best_resolution"
            xrandr --output "$output" --mode "$best_resolution"
        else
            echo "No suitable resolution found for $output, using auto"
            xrandr --output "$output" --auto
        fi
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