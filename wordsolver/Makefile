.PHONY: clean all

CC=gcc
CFLAGS=-I. -std=c99
BIN_DIR=bin
DEPS = common.h BoardTypes.h DawgArray.h WordFinder.h
_OBJ = BoardTypes.o DawgArray.o WordFinder.o main.o
OBJ = $(patsubst %,$(BIN_DIR)/%,$(_OBJ))

$(BIN_DIR)/%.o : %.c $(DEPS)
	$(CC) -c -o $@ $< $(CFLAGS)

all: clean $(BIN_DIR) wordsolver copy_assets

wordsolver: $(OBJ)
	gcc -o $(BIN_DIR)/$@ $^ $(CFLAGS)

$(BIN_DIR):
	mkdir -p $(BIN_DIR)

clean:
	rm -rf $(BIN_DIR)/

copy_assets:
	cp -rf dict $(BIN_DIR)/dict