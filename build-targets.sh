#!/usr/bin/env bash
set -euo pipefail

root=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
# Target, Gradle JVM, Java toolchain.
matrix=(
    'forge 17 17'
    'fabric 17 17'
    'fabric-26.3 25 25'
    'neoforge-26.3 25 25'
    'neoforge-1.21.1 21 21'
    'neoforge-1.21.11 25 21'
    'neoforge-26.1.2 25 25'
)

selection=${1:-all}
if (( $# > 0 )); then
    shift
fi

if [[ "$selection" == --matrix ]]; then
    printf '{"include":['
    separator=
    for row in "${matrix[@]}"; do
        read -r target gradle_jdk toolchain_jdk <<< "$row"
        printf '%s{"target":"%s","gradle_jdk":%s,"toolchain_jdk":%s}' \
            "$separator" "$target" "$gradle_jdk" "$toolchain_jdk"
        separator=,
    done
    printf ']}\n'
    exit
fi

if (( $# == 0 )); then
    set -- build
fi

# Resolve every selected Gradle JVM before starting any build.
targets=()
java_homes=()
toolchain_homes=()
for row in "${matrix[@]}"; do
    read -r target gradle_jdk toolchain_jdk <<< "$row"
    if [[ "$selection" != all && "$selection" != "$target" ]]; then
        continue
    fi
    variable="AVARITIA_JAVA${gradle_jdk}_HOME"
    java_home=${!variable:-${JAVA_HOME:-}}
    java=java
    if [[ -n "$java_home" ]]; then
        java="$java_home/bin/java"
    fi
    version=$("$java" -version 2>&1) || version=
    if [[ "$version" != *\""$gradle_jdk"[.\"-]* ]]; then
        printf '%s requires JDK %s. Set %s to its installation directory.\n' "$target" "$gradle_jdk" "$variable" >&2
        exit 2
    fi
    if [[ -n "$java_home" ]]; then
        java_home=$(cd -- "$java_home" && pwd)
    fi
    toolchain_home=
    if [[ "$toolchain_jdk" != "$gradle_jdk" ]]; then
        variable="AVARITIA_JAVA${toolchain_jdk}_HOME"
        if [[ -n "${!variable:-}" ]]; then
            toolchain_home=$(cd -- "${!variable}" && pwd)
        fi
    fi
    targets+=("$target")
    java_homes+=("$java_home")
    toolchain_homes+=("$toolchain_home")
done

if (( ${#targets[@]} == 0 )); then
    printf 'Usage: %s [target|all] [Gradle arguments...]\n' "${0##*/}" >&2
    printf 'Targets:' >&2
    for row in "${matrix[@]}"; do
        printf ' %s' "${row%% *}" >&2
    done
    printf '\n' >&2
    exit 2
fi

for index in "${!targets[@]}"; do
    target=${targets[index]}
    printf '\nBuilding target %s\n' "$target"
    (
        export JAVA_HOME=${java_homes[index]}
        if [[ -n "$JAVA_HOME" ]]; then
            export PATH="$JAVA_HOME/bin:$PATH"
        fi
        toolchain_args=()
        if [[ -n "${toolchain_homes[index]}" ]]; then
            toolchain_args+=("-Porg.gradle.java.installations.paths=${toolchain_homes[index]},$JAVA_HOME")
        fi
        cd -- "$root/targets/$target"
        bash ./gradlew "${toolchain_args[@]}" "$@"
    )
done
