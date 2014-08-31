require 'mkmf'
$CFLAGS += " " unless $CFLAGS.empty?
$CFLAGS += "-std=c99"
create_makefile('matchingsolver/matchingsolver')
