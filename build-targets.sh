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
    forge|fabric|fabric-26.3|neoforge-26.3) targets=("$selection") ;;
    all) targets=(forge fabric fabric-26.3 neoforge-26.3) ;;
    *)
        printf 'Usage: %s [forge|fabric|fabric-26.3|neoforge-26.3|all] [Gradle arguments...]\n' "${0##*/}" >&2
        exit 2
        ;;
esac

# Resolve every required JDK before starting any build.
java_homes=()
for target in "${targets[@]}"; do
    case "$target" in
        forge|fabric) require=17 ;;
        fabric-26.3|neoforge-26.3) require=25 ;;
    esac
    variable="AVARITIA_JAVA${require}_HOME"
    java_home=${!variable:-${JAVA_HOME:-}}
    java=java
    if [[ -n "$java_home" ]]; then
        java="$java_home/bin/java"
    fi
    version=$("$java" -version 2>&1) || version=
    if [[ "$version" != *\""$require"[.\"-]* ]]; then
        printf '%s requires JDK %s. Set %s to its installation directory.\n' "$target" "$require" "$variable" >&2
        exit 2
    fi
    if [[ -n "$java_home" ]]; then
        java_home=$(cd -- "$java_home" && pwd)
    fi
    java_homes+=("$java_home")
done

for index in "${!targets[@]}"; do
    target=${targets[index]}
    printf '\nBuilding target %s\n' "$target"
    (
        export JAVA_HOME=${java_homes[index]}
        if [[ -n "$JAVA_HOME" ]]; then
            export PATH="$JAVA_HOME/bin:$PATH"
        fi
        cd -- "$root/targets/$target"
        bash ./gradlew "$@"
    )
done
