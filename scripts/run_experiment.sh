#!/bin/bash

# Número de vezes que o script será executado
n=${1:-10}

# Loop para executar o script gradlew run n vezes
for ((i=1;i<=$n;i++))
do
    # Cria uma pasta com o número da execução
    folder_name="execution-$i"
    mkdir $folder_name

    # Executa o script gradlew run
    ./gradlew run -DmainClass="services.outputProcessors.soot.Main" --args="-icf -ioa -idfp -pdg -report -t 0"

    # Move os arquivos gerados pelo script para a pasta da execução atual
    mv out.txt outConsole.txt time.txt output/data/soot-results.csv output/data/results.pdf $folder_name
done

rm -r output/results
mkdir -p output/results
mv -f execution-* output/results

find . -name "results_*" -type f -delete
find . -name "resultTime*" -type f -delete
python3 scripts/generate_time.py $n
python3 scripts/generate_result_time.py $n

mv results_by_execution.jpg output/results
mv results_by_scenarios.jpg output/results
mv results_execution.pdf output/results
mv results_scenarios.pdf output/results

mkdir -p output/results/times
mv resultTime* output/results/times