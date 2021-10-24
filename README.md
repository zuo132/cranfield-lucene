> To run the program

1. Install dependencies

mvn install

2. Run CreateIndex

mvn exec:java -Dexec.mainClass="cranfield.CreateIndex"

3. Run QueryIndex

mvn exec:java -Dexec.mainClass="cranfield.QueryIndex"

4. Run trec_eval tests

./trec_eval/trec_eval cranfield-collection/QRelsCorrectedforTRECeval results.txt
