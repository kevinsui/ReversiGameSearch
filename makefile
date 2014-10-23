default: Main.class Search.class

agent: Main.class Search.class

Main.class: Main.java
	javac Main.java

Search.class: Search.java
	javac Search.java

run:
	java Main

clean:
	rm *.class