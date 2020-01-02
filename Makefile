all: 
	javac DNSlookup.java
	jar cvfe DNSlookup.jar DNSlookup *.class

run: DNSlookup.jar
	java -jar DNSlookup.jar   199.7.83.42 www.cs.ubc.ca   -t

run-ma: DNSlookup.jar
	java -jar DNSlookup.jar   199.7.83.42 www.stanford.edu

run-ma6: DNSlookup.jar
	java -jar DNSlookup.jar   199.7.83.42 www.microsoft.com   -6

run-mat: DNSlookup.jar
	java -jar DNSlookup.jar   199.7.83.42 www.stanford.edu   -t

run-ma6t: DNSlookup.jar
	java -jar DNSlookup.jar   199.7.83.42 www.microsoft.com   -t6

run-a: DNSlookup.jar
	java -jar DNSlookup.jar 198.162.35.1 www.cs.ubc.ca -t

run-ns: DNSlookup.jar
	java -jar DNSlookup.jar 199.7.83.42 prep.ai.mit.edu -t6

run-cn: DNSlookup.jar	
	java -jar DNSlookup.jar 199.7.83.42 finance.google.ca -t

run-cn6: DNSlookup.jar
	java -jar DNSlookup.jar 199.7.83.42 finance.google.ca -t6

run-nohost: DNSlookup.jar
	java -jar DNSlookup.jar 199.7.83.42 oregonegrape.cs.ubc.ca -t

run-int: DNSlookup.jar
	java -jar DNSlookup.jar 199.7.83.42 groups.yahoo.com -t

run-loop: DNSlookup.jar
	java -jar DNSlookup.jar 199.7.83.42 loop1.csproject.org -t

run-to: DNSlookup.jar
	java -jar DNSlookup.jar 10.122.11.11 loop1.csproject.org -t

clean:
	rm -f *.class
	rm -f DNSlookup.jar
cr: clean all run