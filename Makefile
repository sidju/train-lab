all: src/*.java
	javac -sourcepath src -d bin src/Main.java

clean:
	rm -rf bin/*

run: all
	java -classpath bin Main Lab1.map -22 22 10

test: all
	java -classpath bin Main Lab1.map -22 22 5 &
	java -classpath bin Main Lab1.map -9 17 5 &
	java -classpath bin Main Lab1.map -17 9 5 &
	java -classpath bin Main Lab1.map -22 15 5 &
	java -classpath bin Main Lab1.map -15 22 5 &
	java -classpath bin Main Lab1.map -22 1 5 &
	java -classpath bin Main Lab1.map -1 22 5 &
	java -classpath bin Main Lab1.map -1 1 2 &
