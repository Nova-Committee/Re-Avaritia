#!/usr/bin/env bash
set -euo pipefail

root=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
selection=${1:-all}
if (( $# > 0 )); then
    shift
fi
if (( $# == 0 )); then
    set -- build
fi

case "$selection" in
    forge|fabric) targets=("$selection") ;;
    all) targets=(forge fabric) ;;
    *)
        printf 'Usage: %s [forge|fabric|all] [Gradle arguments...]\n' "${0##*/}" >&2
        exit 2
        ;;
esac

for target in "${targets[@]}"; do
    printf '\nBuilding target %s\n' "$target"
    (
        cd -- "$root/targets/$target"
        bash ./gradlew "$@"
    )
done
