all: main.exe

clean:
	rm -rf Debug/

main.exe: main.o
	gcc -O0 -g3 -Wall -std=c99 -o Debug/VisionC Debug/main.o Debug/DawgArray.o Debug/WordFinder.o

main.o:
	rm -rf Debug/
	gcc -O0 -g3 -Wall -std=c99 -c main.c DawgArray.c WordFinder.c
	mkdir -p Debug
	mv *.o Debug/
	