antlrjar = antlr-4.13.2-complete.jar

###### FOR LINUX AND MAC -- comment the following line if you use Windows:
classpath = '$(antlrjar):.'

###### FOR WINDOWS -- uncomment the following line if you use Windows:
#classpath = '$(antlrjar);.'

antlr4 = java -cp $(classpath) org.antlr.v4.Tool
grun = java -cp $(classpath) org.antlr.v4.gui.TestRig
SRCFILES = main.java
GENERATED = tobiaListener.java tobiaBaseListener.java tobiaParser.java tobiaLexer.java

all:	
	make grun

tobiaLexer.java:	tobia.g4
	$(antlr4) tobia.g4 -visitor

tobiaLexer.class:	tobiaLexer.java
	javac -cp $(classpath) $(GENERATED)

grun:	tobiaLexer.class examples/
	$(grun) tobia start -gui -tokens examples/*.hw

example1:	tobiaLexer.class examples/
	$(grun) tobia start -gui -tokens examples/01-hello-world.hw

example2:	tobiaLexer.class examples/
	$(grun) tobia start -gui -tokens examples/01b-hello-world-withdef.hw
	
example3:	tobiaLexer.class examples/
	$(grun) tobia start -gui -tokens examples/02-trafiklys-minimal.hw

example4:	tobiaLexer.class examples/
	$(grun) tobia start -gui -tokens examples/03-trafiklys.hw

example5:	tobiaLexer.class examples/
	$(grun) tobia start -gui -tokens examples/04-von-Neumann.hw

test:	tobiaLexer.class testcases/
	$(grun) tobia start -gui -tokens testcases/01_test-exp-ambiguity.hv

vtest:	Main.java
	 javac -cp Main.java testcases/01_test-exp-ambiguity.hv

clean:
	rm *.tokens *.interp tobiaListener.java tobiaBaseListener.java tobiaParser.java tobiaLexer.java *.class