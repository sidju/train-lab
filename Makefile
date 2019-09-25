all: src/*.java
	javac -sourcepath src -d bin src/Main.java

clean:
	rm -rf bin/*

run: all
	java -classpath bin Main Lab1.map -25 25 10

test: all
	java -classpath bin Main Lab1.map -25 25 5 &
	java -classpath bin Main Lab1.map -9 17 5 &
	java -classpath bin Main Lab1.map -17 9 5 &
	java -classpath bin Main Lab1.map -20 15 5 &
	java -classpath bin Main Lab1.map -15 20 5 &
	java -classpath bin Main Lab1.map -25 1 5 &
	java -classpath bin Main Lab1.map -1 25 5 &
	java -classpath bin Main Lab1.map -1 1 2 &
