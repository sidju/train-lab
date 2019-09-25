all: src/*.java
	javac -sourcepath src -d bin src/Main.java

clean:
	rm -rf bin/*

run: all
	java -classpath bin Main Lab1.map -25 25 10
