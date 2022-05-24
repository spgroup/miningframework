#!/bin/bash
############################################################
# Help                                                     #
############################################################
helpText()
{
   # Display Help
   echo "Syntax: csdiff MYFILE OLDFILE YOURFILE [-options]"
   echo "options:"
   echo "-h                    Print this Help."
   echo "-s \"<separators>\"     Specify the list of separators, e.g. \"{ } ( ) ; ,\""
   echo
}

############################################################
# sed options used                                         #
############################################################
## Descriptions extracted from sed man page: https://linux.die.net/man/1/sed
# -e       - add the script to the commands to be executed
# :a       - Defines a label 'a'
# N        - Append the next line of input into the pattern space.
# $        - Match the last line.
# !        - After the address (or address-range), and before the command, a ! may be inserted, which specifies that the command shall only be executed if the address (or address-range) does not match.
# b[label] - Branch to [label]; if [label] is omitted, branch to end of script.
# s/       - Form: [s/regexp/replacement/] - Attempt to match regexp against the pattern space. If successful, replace that portion matched with replacement.

############################################################
############################################################
#######                   CSDiff                     #######
############################################################
############################################################
############################################################
# Process the input options. Add options as needed.        #
############################################################
while getopts s:h option
do
  case $option in
    h) # display Help
      helpText
      exit 0
      ;;
    s)
      set -f                 # turn off filename expansion
      separators=($OPTARG)   # variable is unquoted
      set +f                 # turn it back on
      ;;
   esac
done

shift $((OPTIND-1))

[ "${1:-}" = "--" ] && shift

############################################################
# Main logic                                               #
############################################################


parameters=("$@")
myFile=${parameters[0]}
oldFile=${parameters[1]}
yourFile=${parameters[2]}
parentFolder="$(dirname "${myFile}")"
myFileBaseName="$(basename "${myFile}")"
fileExt=$([[ "$myFileBaseName" = *.* ]] && echo ".${myFileBaseName##*.}" || echo '')

parentFolder=$(echo "${myFile%/*}")

sedCommandMyFile=""
sedCommandOldFile=""
sedCommandYourFile=""

# Dynamically builds the sed command pipeline based on the number of synctatic separators provided
for separator in "${separators[@]}";
  do
    # Treat some specific symbols that need to be escaped before including them into the regex
    escapedSeparator=$separator
    if [[ $separator = '\' || $separator = '[' || $separator = ']' || $separator = '+' || $separator = '.' || $separator = '*' || $separator = '?' || $separator = '^' || $separator = '$' ]]
    then
      escapedSeparator="\\${separator}"
    fi

    # Build the base substitution script to be passed to the sed command
    sedScript="s/$escapedSeparator/\n\$\$\$\$\$\$\$$escapedSeparator\n\$\$\$\$\$\$\$/g"

    # When the separator is the first one in the array of separators, call sed with the substitution script and with the file
    # When the separator is the last one in the array of separators, call the final sed with the substitution script (piping with the previous call) and output the result to a temp file
    # When none of the above, call sed with the substitution script, piping with the previous call.
    if [[ $separator = ${separators[0]} ]]
    then
      if [[ ${#separators[@]} = 1 ]]
      then
        sedCommandMyFile+="sed '${sedScript}' ${myFile} > ${myFile}_temp${fileExt}"
        sedCommandOldFile+="sed '${sedScript}' ${oldFile} > ${oldFile}_temp${fileExt}"
        sedCommandYourFile+="sed '${sedScript}' ${yourFile}  > ${yourFile}_temp${fileExt}"
      else
        sedCommandMyFile+="sed '${sedScript}' ${myFile}"
        sedCommandOldFile+="sed '${sedScript}' ${oldFile}"
        sedCommandYourFile+="sed '${sedScript}' ${yourFile}"
      fi
    elif [[ $separator = ${separators[-1]} ]]
    then
      sedCommandMyFile+=" | sed '${sedScript}' > ${myFile}_temp${fileExt}"
      sedCommandOldFile+=" | sed '${sedScript}' > ${oldFile}_temp${fileExt}"
      sedCommandYourFile+=" | sed '${sedScript}' > ${yourFile}_temp${fileExt}"
    else
      sedCommandMyFile+=" | sed '${sedScript}'"
      sedCommandOldFile+=" | sed '${sedScript}'"
      sedCommandYourFile+=" | sed '${sedScript}'"
    fi
  done

# Perform the tokenization of the input files based on the provided separators
eval ${sedCommandMyFile}
eval ${sedCommandOldFile}
eval ${sedCommandYourFile}

# Runs diff3 against the tokenized inputs, generating a tokenized merged file
midMergedFile="${parentFolder}/mid_merged${fileExt}"
diff3 -E -m "${myFile}_temp${fileExt}" "${oldFile}_temp${fileExt}" "${yourFile}_temp${fileExt}" > $midMergedFile

# Removes the tokenized input files
rm "${myFile}_temp${fileExt}"
rm "${oldFile}_temp${fileExt}"
rm "${yourFile}_temp${fileExt}"

# Removes the tokens from the merged file, generating the final merged file
mergedFile="${parentFolder}/merged${fileExt}"
sed ':a;N;$!ba;s/\n\$\$\$\$\$\$\$//g' $midMergedFile > $mergedFile

# Removes the tokenized merged file
rm $midMergedFile

# Get the names of left/base/right files
ESCAPED_LEFT=$(printf '%s\n' "${myFile}" | sed -e 's/[\/&]/\\&/g')
ESCAPED_BASE=$(printf '%s\n' "${oldFile}" | sed -e 's/[\/&]/\\&/g')
ESCAPED_RIGHT=$(printf '%s\n' "${yourFile}" | sed -e 's/[\/&]/\\&/g')

ESCAPED_TEMP_LEFT=$(printf '%s\n' "${myFile}_temp${fileExt}" | sed -e 's/[\/&]/\\&/g')
ESCAPED_TEMP_BASE=$(printf '%s\n' "${oldFile}_temp${fileExt}" | sed -e 's/[\/&]/\\&/g')
ESCAPED_TEMP_RIGHT=$(printf '%s\n' "${yourFile}_temp${fileExt}" | sed -e 's/[\/&]/\\&/g')

# Fix the merged file line breaks that got messed up by the CSDiff algorithm.
sed "s/\(<<<<<<< $ESCAPED_TEMP_LEFT\)\(.\+\)/\1\n\2/" $mergedFile \
| sed "s/\(<<<<<<< $ESCAPED_TEMP_BASE\)\(.\+\)/\1\n\2/" \
| sed "s/\(<<<<<<< $ESCAPED_TEMP_RIGHT\)\(.\+\)/\1\n\2/" \
| sed "s/\(||||||| $ESCAPED_TEMP_BASE\)\(.\+\)/\1\n\2/" \
| sed "s/\(||||||| $ESCAPED_TEMP_LEFT\)\(.\+\)/\1\n\2/" \
| sed "s/\(||||||| $ESCAPED_TEMP_RIGHT\)\(.\+\)/\1\n\2/" \
| sed "s/\(>>>>>>> $ESCAPED_TEMP_RIGHT\)\(.\+\)/\1\n\2/" \
| sed "s/\(>>>>>>> $ESCAPED_TEMP_LEFT\)\(.\+\)/\1\n\2/" \
| sed "s/\(>>>>>>> $ESCAPED_TEMP_BASE\)\(.\+\)/\1\n\2/" \
| sed "s/\(=======\)\(.\+\)/\1\n\2/" \
| sed "s/$ESCAPED_TEMP_LEFT/$ESCAPED_LEFT/g" \
| sed "s/$ESCAPED_TEMP_BASE/$ESCAPED_BASE/g" \
| sed "s/$ESCAPED_TEMP_RIGHT/$ESCAPED_RIGHT/g" > "${parentFolder}/csdiff${fileExt}"

# Outputs two other files that will be useful for the study: one generated by the diff3 merge
# and another one generated by the 'git merge-file' command, using the diff3.
diff3 -E -m ${myFile} ${oldFile} ${yourFile} > "${parentFolder}/diff3${fileExt}"
git merge-file -p --diff3 ${myFile} ${oldFile} ${yourFile} > "${parentFolder}/git_merge${fileExt}"

# Remove the merged file, since we already saved it
rm $mergedFile
