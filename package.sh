#!/usr/bin/env bash

function join_by {
   local d=${1-} f=${2-}
   if shift 2; then
     printf %s "$f" "${@/#/$d}"
   fi
}

mvn clean package
cd target
module_jars=(lib/*)
eligible_main_jars=("*.jar")
main_jar=(${eligible_main_jars[0]})
module_path=$(join_by ":" ${module_jars[@]})
module_path="$main_jar:$module_path"
echo $module_path
jpackage \
  --name "Jartotek" \
  --app-version "1.0.0" \
  --description "Note-taking program" \
  --module-path "$module_path" \
  --module jartotek/it.defmacro.kartotek.jartotek.Main \
  --add-modules jdk.crypto.cryptoki


