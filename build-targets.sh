#!/usr/bin/env bash
set -euo pipefail

root=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
manifest="$root/gradle/targets.tsv"
selection=${1:-all}
if (( $# > 0 )); then
    shift
fi

if [[ "$selection" == --matrix ]]; then
    printf '{"include":['
    separator=
    while IFS=$'\t' read -r target _mc toolchain_jdk _catalog; do
        printf '%s{"target":"%s","toolchain_jdk":%s}' "$separator" "$target" "$toolchain_jdk"
        separator=,
    done < "$manifest"
    printf ']}\n'
    exit
fi

if [[ -n ${AVARITIA_JAVA25_HOME:-} ]]; then
    export JAVA_HOME="$AVARITIA_JAVA25_HOME"
fi
cd -- "$root"

if [[ "$selection" == all && $# == 0 ]]; then
    exec ./gradlew build
fi
if (( $# == 0 )); then
    set -- build
fi

matched=false
while IFS=$'\t' read -r -u 3 target _; do
    if [[ "$selection" == all || "$selection" == "$target" ]]; then
        matched=true
        ./gradlew "-Ptarget=$target" "$@" 3<&-
    fi
done 3< "$manifest"

if ! "$matched"; then
    printf 'Unknown target %s. See gradle/targets.tsv.\n' "$selection" >&2
    exit 2
fi
